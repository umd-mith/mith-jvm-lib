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


class SchematronValidator(
  private val source: Source,
  private val resolver: URIResolver
) {
  def this(source: Source) = this(source, null)

  lazy val transformer = {
    val factory = TransformerFactory.newInstance
    if (this.resolver != null) factory.setURIResolver(this.resolver)

    val schema = new DOMResult()

    if (factory.getFeature(SAXTransformerFactory.FEATURE)) {
      // If we can cast appropriately we can avoid creating intermediate DOM
      // representions.
      val saxFactory = factory.asInstanceOf[SAXTransformerFactory]

      val include = saxFactory.newTransformerHandler(
        new StreamSource(this.includeUri)
      )

      val expand = saxFactory.newTransformerHandler(
        new StreamSource(this.expandUri)
      )

      val svrl = saxFactory.newTransformerHandler(
        new StreamSource(this.svrlUri)
      )

      include.setResult(new SAXResult(expand))
      expand.setResult(new SAXResult(svrl))
      svrl.setResult(schema)

      saxFactory.newTransformer.transform(this.source, new SAXResult(include))
    } else {
      val include = new DOMResult()
      val expand = new DOMResult()

      factory.newTemplates(
        new StreamSource(this.includeUri)
      ).newTransformer.transform(this.source, include)

      factory.newTemplates(
        new StreamSource(this.expandUri)
      ).newTransformer.transform(new DOMSource(include.getNode), expand)

      factory.newTemplates(
        new StreamSource(this.svrlUri)
      ).newTransformer.transform(new DOMSource(expand.getNode), schema)
    }
    factory.newTemplates(new DOMSource(schema.getNode)).newTransformer
  }

  def validate(source: Source, result: Result) {
    this.transformer.transform(source, result)
  }

  private val includeUri = this.getClass.getResource(
    "/com/schematron/stylesheets/iso_dsdl_include.xsl"
  ).toExternalForm

  private val expandUri = this.getClass.getResource(
    "/com/schematron/stylesheets/iso_abstract_expand.xsl"
  ).toExternalForm

  private val svrlUri = this.getClass.getResource(
    "/com/schematron/stylesheets/iso_svrl_for_xslt2.xsl"
  ).toExternalForm
}

