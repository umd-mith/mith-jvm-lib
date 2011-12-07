/*
 * #%L
 * MITH Data Mining Utilities
 * %%
 * Copyright (C) 2011 Maryland Institute for Technology in the Humanities
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
package edu.umd.mith.tm.util

import java.io.File
import java.util.TreeSet
import scala.collection.JavaConverters._

import cc.mallet.topics.ParallelTopicModel
import cc.mallet.types.IDSorter

import scala.io.Source

trait Domain[A] {
  def items: Set[A]

  trait Dist extends (A => Double)

  object Dist {
    def apply(f: A => Double) = new Dist { def apply(a: A) = f(a) }
  }

  /* Normalize a set of counts, possibly with additive smoothing. delta should
   * usually be 1.0 (or 0.0 for no smoothing). */
  def convertCounts(counts: Map[A, Int], delta: Double = 1.0): Dist = {
    val total = counts.values.sum.toDouble + delta * counts.size
    Dist({ a: A => (counts.getOrElse(a, 0).toDouble + delta) / total })
  }

  /*def kldZ(p: Dist, q: Dist): Option[Double] = this.items.toSeq.map { item =>
    (p(item), q(item)) match {
      case (0.0, y) => Some(0.0)
      case (x, 0.0) => None
      case (x, y) => Some(x * scala.math.log(x / y))
    }
  }.sequence.map(_.sum)

  def skldZ(p: Dist, q: Dist): Option[Double] =
    (kldZ(p, q), kldZ(q, p)) match {
      case (None, _) => None
      case (_, None) => None
      case (Some(x), Some(y)) => Some(x + y / 2.0)
    }*/

  def kld(p: Dist, q: Dist): Double = this.items.toSeq.map { item =>
    val x = p(item)
    val y = q(item)
    x * scala.math.log(x / y)
  }.sum
}

class TopicModelDomain[A](
  private val modelCounts: IndexedSeq[IndexedSeq[Map[A, Int]]]
) extends Domain[A] {
  lazy val items: Set[A] = {
    { for {
      model <- this.modelCounts
      topic <- model
      item <- topic.keys
    } yield item
    }.toSet
  }

  lazy val models: IndexedSeq[IndexedSeq[Dist]] = {
    this.modelCounts.map { model =>
      model.map { topic =>
        this.convertCounts(topic)
      }
    }
  }

  def table(i: Int, j: Int): IndexedSeq[IndexedSeq[Double]] = {
    this.models(i).map { ti =>
      this.models(j).map { tj =>
        this.kld(ti, tj)
      }
    }
  }

  def matches(i: Int, j: Int): (Seq[(Int, Int)], IndexedSeq[IndexedSeq[Double]]) = {
    val t = this.table(i, j)
    val m = this.models(i).size
    val n = this.models(j).size
    var fI = Range(0, m).toBuffer
    var fJ = Range(0, n).toBuffer
    val answer = scala.collection.mutable.Buffer[(Int, Int)]()
    while (fI.size > 0) {
      val left = { for { fi <- fI; fj <- fJ } yield ((fi, fj), t(fi)(fj)) }.sortBy(_._2)
      answer += left(0)._1
      fI = fI - left(0)._1._1
      fJ = fJ - left(0)._1._2
    }
    (answer.toSeq, t)
  }
}

trait TopicModelReader[A] {
  def counts: IndexedSeq[Map[A, Int]]
}

class MalletReader(file: File) extends TopicModelReader[String] {
  def this(file: String) = this(new File(file))
  def counts = {
    val model = ParallelTopicModel.read(file)
    model.getSortedWords.asScala.map { topic =>
      topic.asInstanceOf[TreeSet[IDSorter]].iterator.asScala.map { word =>
        (model.getAlphabet.lookupObject(word.getID).asInstanceOf[String],
          scala.math.round(word.getWeight).toInt)
      }.toMap
    }.toIndexedSeq
  }
}

class MLSLDAReader(file: File) extends TopicModelReader[String] {
  def this(file: String) = this(new File(file))

  import scala.collection.mutable.Buffer

  def counts = {
    val lines = Source.fromFile(this.file).getLines
    var topics = Buffer[Map[String, Int]]()
    while (lines.hasNext) {
      val topic = this.readTopic(lines)
      if (topic.size > 0) topics += topic
    }
    //topics.foreach(println)
    topics.toIndexedSeq    
  }

  def readTopic(lines: Iterator[String]): Map[String, Int] = {
    lines.dropWhile(line => !line.startsWith("Ontology multinomial"))
      .drop(1).takeWhile(line => !line.startsWith("   > Top ontology terms <")).map { line =>
      val fields = line.split(" ")
      (fields(3), fields(2).toInt)
    }.toMap
  }
}

object TopicManager {
  def pretty(m: Seq[(Int, Int)], t: IndexedSeq[IndexedSeq[Double]]) {
    val iI = m.map(_._1)
    val iJ = m.map(_._2)

    print("       ")
    iJ.foreach { j => print("%6d ".format(j)) }
    print("\n")
    iI.foreach { i =>
      print("%6d ".format(i))
      iJ.foreach { j =>
        print("%.04f ".format(t(i)(j)))
      }
      print("\n")
    }

    println("Total:    %.06f".format(t.map(_.sum).sum))
    println("Diagonal: %.06f".format(t.zipWithIndex.map { case (x, i) => x(i) }.sum))
  }

  def main(args: Array[String]) {
    val a = Source.fromFile(args(0))
    val b = Source.fromFile(args(1))

    val sa = try { a.take(4).toSeq.mkString } catch { case (e: Exception) => "" }
    val sb = try { b.take(4).toSeq.mkString } catch { case (e: Exception) => "" }

    val ma = if (sa == "Num ") {
      new MLSLDAReader(args(0))
    } else {
      new MalletReader(args(0))
    }

    val mb = if (sb == "Num ") {
      new MLSLDAReader(args(1))
    } else {
      new MalletReader(args(1))
    }


    //val m = new MLSLDAReader(args(0))
    //val c = m.counts 
    //val (x, y) = (new MLSLDAReader(args(0)), new MalletReader(args(1)))
    val u = new TopicModelDomain(IndexedSeq(ma.counts, mb.counts))
    val (ms, t) = u.matches(0, 1)
    pretty(ms, t)

    /*println(ms)
    print("       ")
    t.zipWithIndex.foreach { case (_, i) => print("%6d ".format(i)) }
    print("\n")
    t.zipWithIndex.foreach { case (p, i) =>
      print("%6d ".format(i))
      p.zipWithIndex.foreach { case (q, j) =>
        print("%.04f ".format(q))
      }
      print("\n")
    }*/
  }
}

