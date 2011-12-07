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
package edu.umd.mith.mining

import org.specs2.matcher.{ Matcher, MatchersImplicits, NumericMatchers }

trait DoubleSeqMatchers extends MatchersImplicits with NumericMatchers {
  def eps: Double

  def ~=(other: Double*): Matcher[Seq[Double]] =
    { v: Double => this.beCloseTo(v, this.eps) }.toSeq.apply(other)
}

