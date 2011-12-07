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

object RomanNumeral {
  private val numerals = List(
    ("M", 1000), ("CM", 900), ("D",  500), ("CD", 400), ("C",  100),
    ("XC",  90), ("L",   50), ("XL",  40), ("X",   10), ("IX",   9),
    ("V",    5), ("IV",   4), ("I",    1)
  )

  def unapply(s: String): Option[Int] = s.toUpperCase match {
    case "" => Some(0)
    case s: String => {
      numerals.filter { case (n, _) => s.startsWith(n) } match {
        case (n: String, i: Int) :: _ =>
          RomanNumeral.unapply(s.substring(n.length)).map(_ + i)
        case Nil => None
      }
    }
  }
}

