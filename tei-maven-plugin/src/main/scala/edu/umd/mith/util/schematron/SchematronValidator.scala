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
package edu.umd.mith.util.schematron

import java.io.File
import java.lang.reflect.InvocationTargetException
import javax.xml.transform.Result
import javax.xml.transform.Source
import javax.xml.transform.Templates
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.URIResolver
import javax.xml.transform.dom.DOMResult
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.sax.SAXResult
import javax.xml.transform.sax.SAXTransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource
import org.codehaus.mojo.xml.Resolver

class SchematronValidator(resolver: Resolver, source: Source) {
  private val factoryInstance = TransformerFactory.newInstance

  // Should provide a fallback here. 
  assert(this.factoryInstance.getFeature(SAXTransformerFactory.FEATURE))
  private val factory = factoryInstance.asInstanceOf[SAXTransformerFactory]
  this.factory.setURIResolver(resolver)

  private val dsdlInclude = this.factory.newTransformerHandler(
    this.factory.newTemplates(new StreamSource(
      this.getClass.getResource("/com/schematron/stylesheets/iso_dsdl_include.xsl").toExternalForm
    ))
  )

  private val abstractExpand = this.factory.newTransformerHandler(
    this.factory.newTemplates(new StreamSource(
      this.getClass.getResource("/com/schematron/stylesheets/iso_abstract_expand.xsl").toExternalForm
    ))  
  )

  private val svrl = this.factory.newTransformerHandler(
    this.factory.newTemplates(new StreamSource(
      this.getClass.getResource("/com/schematron/stylesheets/iso_svrl_for_xslt2.xsl").toExternalForm
    ))
  )

  private val s = new java.io.StringWriter()
  private val schema = new StreamResult(s)
  //private val schema = new DOMResult()

  this.dsdlInclude.setResult(new SAXResult(this.abstractExpand))
  this.abstractExpand.setResult(new SAXResult(this.svrl))
  this.svrl.setResult(this.schema)

  private val preparer = this.factory.newTransformer
  this.preparer.transform(source, new SAXResult(this.dsdlInclude))

  //val transformer = this.factory.newTemplates(new DOMSource(this.schema.getNode)).newTransformer 
  val transformer = this.factory.newTemplates(new StreamSource(new java.io.StringReader(this.s.toString))).newTransformer 
  this.transformer.setURIResolver(resolver)

  def validate(source: Source, result: Result) {
    this.transformer.transform(source, result)
  }
}

