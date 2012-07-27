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

object DateCleaner {
  def parseYearField(value: String): Option[(Int, Option[Int])] = {
    val AbbrevYearRange = """.*(\d{4})\-(\d{2}).*""".r
    val YearRange = """.*(\d{4}).*(\d{4}).*""".r
    val Year = """.*(\d{4}).*""".r
    val Decade = """.*(\d{3})\-.*""".r
    val Century = """.*(\d{2})\-.*""".r
    value.replaceAll("""[\.\[\], ]""", "") match {
      case AbbrevYearRange(start, end) =>
        Some((start.toInt, Some((start.substring(0, 2) + end).toInt)))
      case YearRange(start, end) => Some((start.toInt, Some(end.toInt)))
      case Year(start) => Some((start.toInt, None))
      case Decade(known) => {
        val decade = known.toInt
        Some((decade * 10, Some(decade * 10 + 9)))
      }
      case Century(known) => {
        val century = known.toInt
        Some((century * 100, Some(century * 100 + 99)))
      }
      case RomanNumeral(start) => Some((start, None))
      case _ => None 
    }
  }
}

