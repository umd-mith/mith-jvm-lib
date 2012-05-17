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
import org.w3c.dom.bootstrap.DOMImplementationRegistry

class SchematronValidator(
  private val source: Source,
  private val resolver: Resolver
) {
  def this(source: Source) = this(source, null)

  lazy val transformer = {
    val factoryInstance = TransformerFactory.newInstance

    if (factoryInstance.getFeature(SAXTransformerFactory.FEATURE)) {
      val factory = factoryInstance.asInstanceOf[SAXTransformerFactory]
      if (this.resolver != null) factory.setURIResolver(resolver)

      val dsdlInclude = factory.newTransformerHandler(
        new StreamSource(this.dsdlIncludeUri)
      )

      val abstractExpand = factory.newTransformerHandler(
        new StreamSource(this.abstractExpandUri)
      )

      val svrl = factory.newTransformerHandler(
        new StreamSource(this.svrlUri)
      )

      val schema = new DOMResult()

      dsdlInclude.setResult(new SAXResult(abstractExpand))
      abstractExpand.setResult(new SAXResult(svrl))
      svrl.setResult(schema)

      factory.newTransformer.transform(source, new SAXResult(dsdlInclude))

      factory.newTemplates(new DOMSource(schema.getNode)).newTransformer
    } else {
      null 
    }
  }

  def validate(source: Source, result: Result) {
    this.transformer.transform(source, result)
  }

  private val dsdlIncludeUri = this.getClass.getResource(
    "/com/schematron/stylesheets/iso_dsdl_include.xsl"
  ).toExternalForm

  private val abstractExpandUri = this.getClass.getResource(
    "/com/schematron/stylesheets/iso_abstract_expand.xsl"
  ).toExternalForm

  private val svrlUri = this.getClass.getResource(
    "/com/schematron/stylesheets/iso_svrl_for_xslt2.xsl"
  ).toExternalForm
}

