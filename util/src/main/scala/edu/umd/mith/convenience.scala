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
package edu.umd.mith.util

package object convenience { outer =>
  import scala.collection.GenTraversableOnce

  def groupPartsBy[A, K, R](xs: GenTraversableOnce[A])(f: (A) => (K, R)):
    Seq[(K, Seq[R])] = {
    import scala.collection.mutable.{ ArrayBuffer, Map }
    val b = ArrayBuffer.empty[(K, ArrayBuffer[R])]
    val m = Map.empty[K, ArrayBuffer[R]]
    xs.foreach { x =>
      val (k, r) = f(x)
      m.get(k) match {
        case Some(rs) => rs += r: Unit
        case None =>
          val p = k -> ArrayBuffer(r)
          b += p
          m += p
      }
    }
    b
  }

  class GenTraversableOnceWrapper[A](wrapped: GenTraversableOnce[A]) {
    def groupPartsBy[K, R](f: (A) => (K, R)) =
      outer.groupPartsBy(this.wrapped)(f)
  }

  implicit def wrapGenTraversableOnce[A](xs: GenTraversableOnce[A]) =
    new GenTraversableOnceWrapper(xs)
}

