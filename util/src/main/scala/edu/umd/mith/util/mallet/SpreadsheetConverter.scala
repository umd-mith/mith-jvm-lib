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
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import scala.collection.JavaConversions._

class SpreadsheetConverter(file: File) {
  val maxWords = 1000
  private val model = ParallelTopicModel.read(file)
  private val alphabet = this.model.getAlphabet

  val words: IndexedSeq[IndexedSeq[(String, Double, Double)]] =
    this.model.getSortedWords.map { t =>
      val words = t.iterator.map { w =>
        alphabet.lookupObject(w.getID).asInstanceOf[String] -> w.getWeight
      }.toIndexedSeq
      val total = words.map(_._2).sum
      words.map { case (token, count) => (token, count, count / total) }
    }.toIndexedSeq

  val documents: IndexedSeq[(String, IndexedSeq[Double])] =
    this.model.getData.zipWithIndex.map {
      case (doc, i) =>
        doc.instance.getName.asInstanceOf[String] ->
          this.model.getTopicProbabilities(i).toIndexedSeq
    }.sortBy(_._1).toIndexedSeq

  def createSpreadsheet(out: File) {
    val book = new SXSSFWorkbook(100)
    val docSheet = book.createSheet("Document topic dists")
    val header = docSheet.createRow(0)
    header.createCell(0).setCellValue("Document identifier")
    
    (0 until this.documents.head._2.size).foreach { i =>
      header.createCell(i + 1).setCellValue("Topic " + i)
    }

    this.documents.zipWithIndex.foreach { case ((doc, probs), i) =>
      val row = docSheet.createRow(i + 1)
      row.createCell(0).setCellValue(doc)
      probs.zipWithIndex.foreach { case (prob, j) =>
        row.createCell(j + 1).setCellValue(prob)
      }
    }

    docSheet.setColumnWidth(0, 256 * 24)

    val topicFormSheet = book.createSheet("Topic word dists (forms)")
    val topicProbSheet = book.createSheet("Topic word dists (probs)")
    this.words.zipWithIndex.foreach { case (topic, i) =>
      val formRow = topicFormSheet.createRow(i)
      val probRow = topicProbSheet.createRow(i)

      formRow.createCell(0).setCellValue("Topic " + i)
      probRow.createCell(0).setCellValue("Topic " + i)

      topic.take(this.maxWords).zipWithIndex.foreach {
        case ((form, _, prob), j) =>
          formRow.createCell(j + 1).setCellValue(form)
          probRow.createCell(j + 1).setCellValue(prob)
      } 
    }

    val stream = new FileOutputStream(out)
    book.write(stream)
    stream.close()
  }
}

object SpreadsheetConverter extends App {
  val converter = new SpreadsheetConverter(new File(args(0)))
  converter.createSpreadsheet(new File(args(1)))
}

