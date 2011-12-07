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

abstract class TransformingGoal extends TransformMojo {
  def getTransformerFactoryClassName: String

  protected def getSource(path: String) = new org.xml.sax.InputSource(
    this.getResource(path).openStream
  )

  protected def getTemplate(r: Resolver, s: Source) = {
    val tf = this.getTransformerFactory
    tf.setURIResolver(r)
    try tf.newTemplates(s)
    catch {
      case e: TransformerConfigurationException =>
        throw new MojoExecutionException("Failed to parse stylesheet " + s + ": " + e.getMessage, e)
    }
  }

  private def getTransformerFactory = Option(this.getTransformerFactoryClassName) match {
    case None => TransformerFactory.newInstance
    case Some(className: String) => try {
      val classLoader: ClassLoader = Thread.currentThread.getContextClassLoader
      val method = classOf[TransformerFactory].getDeclaredMethod( "newInstance", classOf[String], classOf[ClassLoader])
      method.invoke(null, className, classLoader).asInstanceOf[TransformerFactory]
    } catch {
      case _: NoSuchMethodException =>
        throw new MojoFailureException("JDK6 required when using transformerFactory parameter.")
      case e: IllegalAccessException =>
        throw new MojoExecutionException("Cannot instantiate transformer factory.", e)
      case e: InvocationTargetException =>
        throw new MojoExecutionException("Cannot instantiate transformer factory.", e)
    }
  }
}

