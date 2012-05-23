/*
 * #%L
 * MITH General Utilities
 * %%
 * Copyright (C) 2011 - 2012 Maryland Institute for Technology in the Humanities
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package edu.umd.mith.util.mallet

import cc.mallet.topics.ParallelTopicModel
import com.google.common.collect.MinMaxPriorityQueue
import java.io.File
import scala.collection.JavaConversions._
import scala.collection.SortedMap

class TopicModel(val model: ParallelTopicModel, val delta: Double) {
  def this(model: ParallelTopicModel) = this(model, 1.0)
  def this(file: File, delta: Double) = this(ParallelTopicModel.read(file), delta)
  def this(file: File) = this(file, 1.0)

  val vocabulary = this.model.getAlphabet.toArray.map(_.asInstanceOf[String])

  case class TopicWord(count: Int, prob: Double, smoothed: Double)

  val topics: IndexedSeq[Map[String, TopicWord]] =
    this.model.getSortedWords.map { topic =>
      val words = topic.iterator.map { word =>
        this.vocabulary(word.getID) -> word.getWeight.round.toInt
      }.toSeq

      val total = words.map(_._2).sum.toDouble
      val smoothedTotal = total + (this.vocabulary.size * this.delta)
      val default = TopicWord(0, 0.0, this.delta / smoothedTotal)

      words.map { case (word, count) =>
        word -> TopicWord(
          count, count / total, (count + this.delta) / smoothedTotal
        )
      }.toMap.withDefaultValue(default)
    }.toIndexedSeq

  val documents: SortedMap[String, IndexedSeq[Double]] =
    this.model.getData.zipWithIndex.foldLeft(
      SortedMap.empty[String, IndexedSeq[Double]]
    ) { case (map, (document, i)) => map.updated(
        document.instance.getName.asInstanceOf[String],
        this.model.getTopicProbabilities(i)
      )
    }

  def skld[A](ps: Iterable[(Double, Double)]) = ps.map {
    case (x, y) => x * math.log(x / y) + y * math.log(y / x)
  }.sum / 2.0

  def ttTable = IndexedSeq.tabulate(this.topics.size, this.topics.size) {
    (i, j) => this.skld(this.vocabulary.map {
      word => (this.topics(i)(word).smoothed, this.topics(j)(word).smoothed)
    })
  }

  def ddTable: Iterator[((String, String), Double)] =
    this.documents.iterator.zipWithIndex.flatMap { case ((xi, xd), i) =>
      this.documents.iterator.drop(i + 1).map { case (yi, yd) =>
        (xi, yi) -> this.skld(xd.zip(yd)) 
      }
    }

  def ddTableFixed(size: Int): IndexedSeq[((String, String), Double)] = {
    val queue = MinMaxPriorityQueue.orderedBy(
      Ordering.by((p: ((String, String), Double)) => p._2)
    ).maximumSize(size).create[((String, String), Double)]

    this.ddTable.foreach(i => queue.add(i))
    IndexedSeq.fill(queue.size)(queue.pollFirst)
  }
}

