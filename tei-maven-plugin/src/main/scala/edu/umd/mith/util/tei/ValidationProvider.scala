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

//import org.apache.maven.surefire.junitcore.JUnitCoreProvider
import org.apache.maven.surefire.providerapi.AbstractProvider
import org.apache.maven.surefire.providerapi.ProviderParameters
import org.apache.maven.surefire.providerapi.SurefireProvider
import org.apache.maven.surefire.report.SimpleReportEntry
import scala.collection.JavaConversions._

class ValidationProvider(parameters: ProviderParameters) extends AbstractProvider {
  override def getSuites: java.util.Iterator[_] = List(1).toIterator

  override def invoke(thing: Object) = {
    val reporterFactory = parameters.getReporterFactory
    val reporter = reporterFactory.createReporter
    val set = new SimpleReportEntry("some.xml", "set")
    val s = this.getProject.getPluginManagement.getPlugins.map(_.asInstanceOf[org.apache.maven.model.Plugin].getKey).mkString(",")
    val entry = new SimpleReportEntry("another.xml", s)
    reporter.testSetStarting(set)
    reporter.testStarting(entry)
    reporter.testFailed(entry)
    reporter.testSetCompleted(set)
    reporterFactory.close()
  }
}

