/*
 * #%L
 * MITH General Utilities
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
package edu.umd.mith.util

trait YearValue {
  def start: Int
  def end: Int = this.start
}

case class Year(start: Int) extends YearValue
case class YearRange(start: Int, override val end: Int) extends YearValue

trait DateCleaner {
  def parseYearField(s: String): Option[YearValue]
}

object SimpleDateCleaner extends DateCleaner {
  private[this] val abbrevYearRange = """[^\\d]*c?(\d{4})\-c?(\d{2})[^\\d]*""".r
  private[this] val yearRange = """[\\d]*c?(\d{4})[\\d]*(\d{4})[^\\d]*""".r
  private[this] val year = """[\\d]*c?(\d{4})[\\d]*""".r
  private[this] val decade = """[\\d]*c?(\d{3})\-[\\d]*""".r
  private[this] val century = """[\\d]*c?(\d{2})\-[\\d]*""".r

  private[this] val parse: PartialFunction[String, YearValue] = {
    case abbrevYearRange(start, end) => YearRange(
      start.toInt, (start.substring(0, 2) + end).toInt
    )
    case yearRange(start, end) => YearRange(start.toInt, end.toInt)
    case year(start) => Year(start.toInt)
    case decade(known) => {
      val decade = known.toInt * 10
      YearRange(decade, decade + 9)
    }
    case century(known) => {
      val century = known.toInt * 100
      YearRange(century, century + 99)
    }
    case RomanNumeral(start) => Year(start)
  }

  def parseYearField(s: String) =
    this.parse.lift(s.replaceAll("""[\.\[\], <>]""", ""))
}

