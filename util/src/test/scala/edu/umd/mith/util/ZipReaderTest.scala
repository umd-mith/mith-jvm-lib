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

import org.specs2.mutable._
import java.io.File

class ZipReaderTest extends SpecificationWithJUnit {
  "the test zip file" should {
    "have 341 entries" in new Entries {
      this.entries.size must_== 341
    }

    "start with the directory entry" in new Entries {
      this.entries(0)._1 must_== testName + "/"
    }

    "contain correctly named files in order" in new Entries {
      this.entries.drop(1).zipWithIndex.foreach { case ((name, _), i) =>
        name must_== "%s/%08d.txt".format(testName, i + 1)
      }
    }

    "have the correct page as its third entry" in new Entries {
      val lines = this.entries(2)._2.getLines.toIndexedSeq
      lines.size must_== 10
      lines(0) must_== ""
      lines(1) must_== " "
      lines(2) must_== " Columbia ©nitJem'tp "
      lines(3) must_== " "
      lines(4) must_== " THE LIBRARIES "
      lines(5) must_== " "
      lines(6) must_== " Bequest of "
      lines(7) must_== " Frederic Bancroft "
      lines(8) must_== " "
      lines(9) must_== " 1860-1945"
    }
  }
}

trait Entries extends After {
  val testName = "ark+=13960=t00z79g41"
  val reader = new ZipReader(
    new File(this.getClass.getResource(this.testName + ".zip").toURI)
  )
  val entries = this.reader.iterator.toIndexedSeq

  def after = this.reader.close() 
}

