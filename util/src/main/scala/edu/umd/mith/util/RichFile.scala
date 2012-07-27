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

import java.io.File

class RichFile(private val file: File) {
  def leaves: Iterator[File] = {
    val children = this.file.listFiles.sorted
    if (children.forall(_.isDirectory)) {
      children.toIterator.flatMap(new RichFile(_).leaves)
    } else {
      Iterator(this.file)
    }
  }
}

