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
import javax.xml.transform.Source
import javax.xml.transform.stream.StreamSource
import org.codehaus.mojo.xml.AbstractXmlMojo

trait AbstractTeiGoal { this: AbstractXmlMojo =>
  def getOddSpecs: Array[OddSpec]
  def perform(): Unit

  override def execute() {
    val oldProxySettings = this.activateProxy()
    try this.perform() catch {
      case e => throw e
    } finally this.passivateProxy(oldProxySettings)
  }

  def getSource(path: String): Source =
    new StreamSource(this.getClass.getResource(path).toExternalForm)

  protected val removeExtension: String => String = {
    case s: String if s.endsWith(".odd") => s.slice(0, s.length - 4)
    case s: String if s.endsWith(".odd.xml") => s.slice(0, s.length - 8)
    case s: String => s
  }

  protected def getOdd2Odd = this.getSource("/org/tei_c/stylesheets/odds2/odd2odd.xsl")
  protected def getOdd2Rng = this.getSource("/org/tei_c/stylesheets/odds2/odd2relax.xsl")
  protected def getOdd2Sch = this.getSource("/org/tei_c/stylesheets/odds2/extract-isosch.xsl")
}

