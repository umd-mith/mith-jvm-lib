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

import java.io.FileInputStream
import java.io.InputStream
import scala.collection.mutable.HashMap
import scala.io.Source
import scala.xml.pull._

class MetadataParser(val in: InputStream)
  extends Iterable[(String, Map[String, Seq[String]])] {
  // These type aliases are only here to allow more concise definitions below.
  type FieldMap = Map[String, Seq[String]]
  type Record = (String, FieldMap)

  def this(path: String) = this(new FileInputStream(path))

  // This method reads consecutive text and entity events and concatenates
  // them.
  private def readText(reader: Iterator[XMLEvent]): String =
    reader.takeWhile {
      case _: EvText      => true
      case _: EvEntityRef => true
      case _              => false
    }.map {
      case EvText(text)       => text
      case EvEntityRef("amp") => "&"
      case EvEntityRef("lt")  => "<"
      case EvEntityRef("gt")  => ">"
    }.mkString

  // This method reads and returns a single record.
  private def readRecord(reader: Iterator[XMLEvent]): FieldMap = {
    val record = new HashMap[String, Seq[String]].withDefaultValue(Seq.empty)
    var current = reader.next
    while (current match {
      case EvElemStart(_, name, _, _) => {
        // Read the text content of this element.
        val value = this.readText(reader)
        // Append this value.
        record(name) = record(name) :+ value
        true
      }
      case _ => false
    }) current = reader.next
    // Make the map immutable.
    record.toMap
  }

  def iterator: Iterator[Record] = {
    // We only need element start and end events and non-empty text events.
    val reader = new XMLEventReader(Source.fromInputStream(this.in)).filter {
      case _: EvElemStart => true
      case _: EvElemEnd   => true
      case _: EvEntityRef => true
      case EvText(text)   => text.trim.nonEmpty
      case _              => false
    }

    // Burn the document start event.
    reader.next

    // Start parsing in earnest.
    var current = reader.next

    new Iterator[Record] {
      def hasNext: Boolean = current match {
        case EvElemStart(_, "record", attrs, _) => true
        case _                                  => false
      }

      def next: Record = {
        val EvElemStart(_, "record", attrs, _) = current
        val record = MetadataParser.this.readRecord(reader)
        current = reader.next
        attrs.asAttrMap("id") -> record
      }
    }
  }
}

