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

import javax.xml.transform.Source
import javax.xml.transform.Templates
import javax.xml.transform.TransformerConfigurationException
import javax.xml.transform.TransformerFactory

import java.lang.reflect.InvocationTargetException

import java.io.File
import org.codehaus.mojo.xml.Resolver
import org.codehaus.mojo.xml.TransformMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException

abstract class OddSchemaGoal extends TransformingGoal {
  def getOdd: File
  def getOutputDir: File
  def getRngOutputDir = this.getOutputDir
  def getSchOutputDir = this.getOutputDir
  def getForceCreation: Boolean

  override def execute() {
    Option(this.getOdd) match {
      case None => throw new MojoFailureException("No ODD source configured.")
      case Some(source) => {
        val oldProxySettings = this.activateProxy()
        try {
          val resolver = this.getResolver()
        
        } catch {
          case e => throw e
        } finally this.passivateProxy(oldProxySettings)
      }
    }
  }

  private def getOdd2Odd = this.getSource("/tei/xsl/odds2/odd2odd.xsl")
  private def getOdd2Rng = this.getSource("/tei/xsl/odds2/odd2relax.xsl")
  private def getOdd2Sch = this.getSource("/tei/xsl/odds2/extract-isosch.xsl")
}

