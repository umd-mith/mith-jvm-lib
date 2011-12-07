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

import java.io.Closeable
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import scala.collection.JavaConverters._
import scala.io.Source

/** Provides a convenient way of iterating over entries in a zipped file.
  *
  * @author Travis Brown <travisrobertbrown@gmail.com>
  * @note I'm currently sorting the files by name before iterating, since I'm
  *       not sure exactly what [[java.util.zip.ZipFile]] offers in the way of
  *       guarantees about order. It'd be slightly nicer not to do this.
  * @param file the file to be unzipped
  */
class ZipReader(file: File)
  extends Iterable[(String, Source)] with Closeable {
  def this(path: String) = this(new File(path)) 

  private val zipped = new ZipFile(this.file)
  private val entries = this.zipped.entries.asScala.toIndexedSeq

  implicit val zipEntryOrdering: Ordering[ZipEntry] = Ordering.by(_.getName)

  def iterator = this.entries.sorted.iterator.map {
    e => (e.getName, Source.fromInputStream(this.zipped.getInputStream(e)))
  }

  def close() = this.zipped.close()
}

