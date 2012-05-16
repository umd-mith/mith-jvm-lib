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
import javax.xml.XMLConstants
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.SchemaFactory
import javax.xml.validation.Validator
import org.apache.maven.plugin.MojoFailureException

abstract class ValidateGoal extends TransformingGoal {
  def perform() {
    val resolver = this.getResolver()
    this.getOddSpecs.foreach { spec =>
      Option(spec.getSource).map { source =>
        val base = this.removeExtension(source.getName)
        val rng = new File(spec.getRngOutputDir(this.getProject), base + ".rng")
        val sch = new File(spec.getSchOutputDir(this.getProject), base + ".isosch")

        val rngValidator = SchemaFactory.newInstance(XMLConstants.RELAXNG_NS_URI)
          .newSchema(new StreamSource(rng))
          .newValidator

        this.getTeiFiles(Option(spec.getTeiDirs)).foreach {
          println(_)
        }
      }.getOrElse(throw new MojoFailureException("No ODD source configured."))
    }
  }
  /*override def setUp() {
    System.setProperty(classOf[SchemaFactory].getName() + ":" + XMLConstants.RELAXNG_NS_URI,
      "com.thaiopensource.relaxng.jaxp.XMLSyntaxSchemaFactory")
    XMLUnit.setControlParser("org.apache.xerces.jaxp.DocumentBuilderFactoryImpl")
    XMLUnit.setSAXParserFactory("org.apache.xerces.jaxp.SAXParserFactoryImpl")
    XMLUnit.setTransformerFactory("net.sf.saxon.TransformerFactoryImpl")
    //this.validator.addSchemaSource(new StreamSource(this.getClass.getResourceAsStream(this.schema)))
  }

  def testValidate() {
    this.docs.foreach { doc =>
      val result = try {
        Right(this.validator.validate(new StreamSource(this.getClass.getResourceAsStream(doc))))
      } catch {
        case e: SAXException => Left(e)
      }
      assertTrue(
        ".%s is not valid: %s".format(doc, result.left.toOption.map(_.getMessage).getOrElse("")),
        result.isRight
      )
    }
  }*/
}

