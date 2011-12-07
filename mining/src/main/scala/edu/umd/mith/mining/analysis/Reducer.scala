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
package edu.umd.mith.mining.analysis

trait Reduced {
  def data: Array[Array[Double]]
  def dims: Int = this.data.headOption.map(_.length).getOrElse(0)
}

trait Reducer[B <: Reduced] {
  def reduce(data: Array[Array[Double]], dims: Int): B
  def reduce(data: Array[Array[Double]]): B = this.reduce(
    data,
    data.headOption.map(_.length).getOrElse(0)
  )
}

