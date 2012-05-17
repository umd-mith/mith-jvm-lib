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

import edu.umd.mith.util.schematron.SchematronValidator
import java.io.File
import javax.xml.XMLConstants
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.SchemaFactory
import javax.xml.validation.Validator
import org.apache.maven.plugin.MojoFailureException
import org.xml.sax.ErrorHandler
import org.xml.sax.SAXParseException
import scala.collection.mutable.Buffer

abstract class ValidateGoal extends TransformingGoal {
  def perform() {
    System.setProperty(
      classOf[SchemaFactory].getName() + ":" + XMLConstants.RELAXNG_NS_URI,
      "com.thaiopensource.relaxng.jaxp.XMLSyntaxSchemaFactory"
    )

    this.getOddSpecs.foreach { spec =>
      Option(spec.getSource).map { source =>
        val base = this.removeExtension(source.getName)
        val rng = new File(spec.getRngOutputDir(this.getProject), base + ".rng")
        val sch = new File(spec.getSchOutputDir(this.getProject), base + ".isosch")

        val rngValidator = SchemaFactory.newInstance(XMLConstants.RELAXNG_NS_URI)
          .newSchema(new StreamSource(rng))
          .newValidator

        val schValidator = new SchematronValidator(new StreamSource(sch), this.getResolver)

        val errors = new ValidationErrorHandler
        rngValidator.setErrorHandler(errors)

        this.getTeiFiles(Option(spec.getTeiDirs)).foreach(_.foreach { file =>
          println("Validating " + file)
          try {
            rngValidator.validate(new StreamSource(file))
            schValidator.validate(new StreamSource(file), new StreamResult(new File("so/" + file.getName)))
          } catch {
            case e: Exception => throw(e)
          }
        })
        errors.getAll.foreach { case e: SAXParseException =>
          println(e.getSystemId + " " + e.getColumnNumber.toString + " " + e.getLineNumber.toString + " " + e.getMessage)
        }
      }.getOrElse(throw new MojoFailureException("No ODD source configured."))
    }
  }
}

class ValidationErrorHandler extends ErrorHandler {
  private val errors = Buffer.empty[SAXParseException]
  private val fatalErrors = Buffer.empty[SAXParseException]
  private val warnings = Buffer.empty[SAXParseException]

  def getAll = this.getFatalErrors ++ this.getErrors ++ this.getWarnings
  def getErrors: Seq[SAXParseException] = this.errors
  def getFatalErrors: Seq[SAXParseException] = this.fatalErrors
  def getWarnings: Seq[SAXParseException] = this.warnings

  def error(e: SAXParseException) {
    this.errors += e
  }

  def fatalError(e: SAXParseException) {
    this.fatalErrors += e
  }

  def warning(e: SAXParseException) {
    this.warnings += e
  }
}

