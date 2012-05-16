/*
 * #%L
 * MITH TEI Maven Plugin
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
package edu.umd.mith.util.tei

import java.io.File
import org.apache.maven.plugin.MojoFailureException

abstract class ValidateGoal extends TransformingGoal {
  def perform() {
    val resolver = this.getResolver()
    this.getOddSpecs.foreach { spec =>
      Option(spec.getSource).map { source =>
        val base = this.removeExtension(source.getName)
        val rng = new File(spec.getRngOutputDir(this.getProject), base + ".rng")
        val sch = new File(spec.getSchOutputDir(this.getProject), base + ".isosch")
        Option(spec.getTeiDirs).foreach { _ => ()

        }
      }.getOrElse(throw new MojoFailureException("No ODD source configured."))
    }
  }
}

