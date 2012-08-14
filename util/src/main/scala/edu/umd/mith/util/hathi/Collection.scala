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
package edu.umd.mith.util.hathi

import java.io.File
import scala.io.Source

import edu.umd.mith.util.{ RichFile, ZipReader }

case class TextInfo(
  val id: String,
  val metsFile: File,
  val zipFile: File
)

class Collection(private val base: String) {
  def escape(id: String): (String, String) = {
    val first = id.indexOf(".")
    val collection = id.substring(0, first)
    val remainder = id.substring(first + 1)
    val dirName = remainder.replaceAll("\\.", ",")
                           .replaceAll("\\:", "+")
                           .replaceAll("\\/", "=")
    (collection, dirName)
  }

  def unescape(dirName: String) =
    dirName.replaceAll("\\,", ".")
           .replaceAll("\\+", ":")
           .replaceAll("\\=", "/")

  def texts: Iterator[TextInfo] = {
    new File(this.base).listFiles.sorted.toIterator.flatMap { collection =>
      new RichFile(new File(collection, "pairtree_root")).leaves.map { path =>
        val metsFile = new File(path, path.getName + ".mets.xml")
        val zipFile = new File(path, path.getName + ".zip")
        assert(metsFile.exists)
        assert(zipFile.exists) 
        TextInfo(
          collection.getName + "." + this.unescape(path.getName),
          metsFile,
          zipFile
        )
      }
    }
  }

  def findTextInfo(id: String): Option[TextInfo] = {
    val (collection, name) = this.escape(id)
    val parts = collection +: "pairtree_root" +: name.grouped(2).toList :+ name
    val path = new File(this.base, parts.mkString(File.separator))
    if (path.exists) {
      val metsFile = new File(path, path.getName + ".mets.xml")
      val zipFile = new File(path, path.getName + ".zip")
      if (metsFile.exists && zipFile.exists) {
        Some(TextInfo(id, metsFile, zipFile))
      } else None
    } else {
      System.err.println("ERROR: no such file: " + path)
      None
    }
  }

  def extractPages(text: TextInfo): Iterator[(Int, String)] = {
    val reader = new ZipReader(text.zipFile)
    reader.iterator.drop(1).flatMap {
      case (path, source) => {
        val Array(_, name) = path.split("\\/")
        val Array(number, _) = name.split("\\.")
        try Some(number.toInt, source.mkString)
          catch { case e: NumberFormatException => None }
      }
    }
  }

  def malletFormat(id: String): Option[Iterator[String]] = {
    this.findTextInfo(id).map { 
      this.extractPages(_).map {
        case (number, content) =>
          "%s.%04d _ %s".format(id, number, content.replaceAll("\n", " "))
      }
    }
  }
}

/**
 * Simple example of how to extract texts from the Hathi collection, in this
 * case to export nineteenth-century texts for topic modeling with MALLET.
 */
object MalletConverter {
  def main(args: Array[String]) {
    import edu.umd.mith.util.SimpleDateCleaner.parseYearField

    val metadata = new MetadataParser(args(0))
    val collection = new Collection(args(1))
    val out = new java.io.PrintWriter(args(2))

    val pages = metadata.par.flatMap {
      case (id, fields) =>
        fields.get("date").flatMap(
          _.headOption.flatMap(parseYearField(_))
        ).flatMap {
          case year if year.start >= 1800 && year.end < 1900 =>
            println(id)
            collection.malletFormat(id)
          case _ => None
        }.flatten
    }

    pages.foreach(out.println)
    out.close()
  }
}

