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

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import scala.collection.JavaConversions._

class SpreadsheetConverter(file: File) {
  val maxWords = 1000
  val model = new TopicModel(file, 0.01)

  def createBook = new SXSSFWorkbook(100)

  def createDefaultSheets(book: Workbook) {
    this.createDTDSheet(book)
    this.createTWDSheet(book)
    this.createTTESheet(book)
    this.createDDESheet(book)
    this.createDTESheet(book)
  }

  def writeBook(book: Workbook, out: File) {
    val stream = new FileOutputStream(out)
    book.write(stream)
    stream.close()
  }

  def createDTDSheet(book: Workbook) {
    val sheet = book.createSheet("Document topic dists")
    val header = sheet.createRow(0)
    header.createCell(0).setCellValue("Document identifier")
    
    (0 until this.model.topics.size).foreach { i =>
      header.createCell(i + 1).setCellValue("topic-%02d".format(i))
    }

    this.model.documents.iterator.zipWithIndex.foreach {
      case ((document, probs), i) =>
        val row = sheet.createRow(i + 1)
        row.createCell(0).setCellValue(document)
        probs.zipWithIndex.foreach { case (prob, j) =>
          row.createCell(j + 1).setCellValue(prob)
        }
    }

    sheet.setColumnWidth(0, 256 * 24)
  }

  def createTWDSheet(book: Workbook) {
    val formSheet = book.createSheet("Topic word dists (forms)")
    val probSheet = book.createSheet("Topic word dists (probs)")
    this.model.topics.zipWithIndex.foreach { case (topic, i) =>
      val formRow = formSheet.createRow(i)
      val probRow = probSheet.createRow(i)

      formRow.createCell(0).setCellValue("topic-%02d".format(i))
      probRow.createCell(0).setCellValue("topic-%02d".format(i))

      topic.toSeq.sortBy(-_._2.count).take(this.maxWords).zipWithIndex.foreach {
        case ((word, count), j) =>
          formRow.createCell(j + 1).setCellValue(word)
          probRow.createCell(j + 1).setCellValue(count.prob)
      } 
    }
  }

  def createTTESheet(book: Workbook, threshhold: Double = Double.PositiveInfinity) {
    val sheet = book.createSheet("Topic-topic edges")
    val header = sheet.createRow(0)
    header.createCell(0).setCellValue("First topic ID")
    header.createCell(1).setCellValue("Second topic ID")
    header.createCell(2).setCellValue("Symmetrized KL-divergence")

    this.model.ttTable.view.zipWithIndex.flatMap { case (ttRow, i) =>
      ttRow.view.zipWithIndex.drop(i + 1).map {
        case (d, j) => (d, (i, j))
      }
    }.filter(_._1 <= threshhold).zipWithIndex.foreach {
      case ((d, (i, j)), k) =>
        val row = sheet.createRow(k + 1)
        row.createCell(0).setCellValue("topic-%02d".format(i))
        row.createCell(1).setCellValue("topic-%02d".format(j))
        row.createCell(2).setCellValue(d)
    }
  }

  def createDDESheet(book: Workbook, size: Int = 2048) {
    val sheet = book.createSheet("Document-document edges")
    val header = sheet.createRow(0)
    header.createCell(0).setCellValue("First document ID")
    header.createCell(1).setCellValue("Second document ID")
    header.createCell(2).setCellValue("Symmetrized KL-divergence")

    this.model.ddTable/*Fixed(size)*/.zipWithIndex.foreach {
      case (((xi, yi), p), k) =>
        val row = sheet.createRow(k + 1)
        row.createCell(0).setCellValue(xi)
        row.createCell(1).setCellValue(yi)
        row.createCell(2).setCellValue(p)
    }
  }

  def createDTESheet(book: Workbook, threshhold: Double = 0.1) {
    val sheet = book.createSheet("Document-topic edges")
    val header = sheet.createRow(0)
    header.createCell(0).setCellValue("Document ID")
    header.createCell(1).setCellValue("Topic ID")
    header.createCell(2).setCellValue("Proportion")
    
    this.model.documents.iterator.flatMap { case (di, topics) =>
      topics.zipWithIndex.filter(_._1 >= threshhold).map {
        case (p, j) => (p, (di, j))
      }
    }.filter(_._1 >= threshhold).zipWithIndex.foreach {
      case ((p, (di, j)), k) =>
        val row = sheet.createRow(k + 1)
        row.createCell(0).setCellValue(di)
        row.createCell(1).setCellValue(j)
        row.createCell(2).setCellValue(p)
    }
  }
}

object SpreadsheetConverter extends App {
  val converter = new SpreadsheetConverter(new File(args(0)))
  val book = converter.createBook
  converter.createDefaultSheets(book)
  converter.writeBook(book, new File(args(1)))
}

