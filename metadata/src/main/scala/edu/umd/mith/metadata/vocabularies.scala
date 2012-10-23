/*
 * #%L
 * MITH Metadata Definitions
 * %%
 * Copyright (C) 2011 - 2012 University of Maryland
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
package edu.umd.mith.metadata

import org.scardf._

object DCTypes extends Vocabulary(
  "http://purl.org/dc/dcmitype/"
) {
  val Collection = DCTypes\"Collection"
  val Dataset = DCTypes\"Dataset"
  val Event = DCTypes\"Event"
  val Image = DCTypes\"Image"
  val InteractiveResource = DCTypes\"InteractiveResource"
  val MovingImage = DCTypes\"MovingImage"
  val PhysicalObject = DCTypes\"PhysicalObject"
  val Service = DCTypes\"Service"
  val Software = DCTypes\"Software"
  val Sound = DCTypes\"Sound"
  val StillImage = DCTypes\"StillImage"
  val Text = DCTypes\"Text"
}

object DC11 extends Vocabulary(
  "http://purl.org/dc/elements/1.1/"
) {
  val contributor = prop("contributor")
  val coverage = prop("coverage")
  val creator = prop("creator")
  val date = prop("date")
  val description = prop("description")
  val format = prop("format")
  val identifier = prop("identifier")
  val language = prop("language")
  val publisher = prop("publisher")
  val relation = prop("relation")
  val rights = prop("rights")
  val source = prop("source")
  val subject = prop("subject")
  val title = prop("title")
  val `type` = prop("type")
}

object ORE extends Vocabulary(
  "http://www.openarchives.org/ore/terms/"
) {
  val Aggregation = ORE\"Aggregation"
  val ResourceMap = ORE\"ResourceMap"
  val describes = prop("describes")
  val isDescribedBy = prop("isDescribedBy")
  val aggregates = prop("aggregates")
}

object EXIF extends Vocabulary(
  "http://www.w3.org/2003/12/exif/ns#"
) {
  val width = prop("width")
  val height = prop("height")
}

object OA extends Vocabulary(
  "http://www.w3.org/ns/openannotation/core/"
) {
  val Annotation = OA\"Annotation"
  val SpecificResource = OA\"SpecificResource"
  val hasBody = prop("hasBody")
  val hasTarget = prop("hasTarget")
  val hasSource = prop("hasSource")
  val hasSelector = prop("hasSelector")
}

object OAX extends Vocabulary(
  "http://www.w3.org/ns/openannotation/extension/"
) {
  val TextOffsetSelector = OAX\"TextOffsetSelector"
  val begin = prop("begin")
  val end = prop("end")
}

object SC extends Vocabulary(
  "http://www.shared-canvas.org/ns/"
) {
  val ContentAnnotation = SC\"ContentAnnotation"
  val AnnotationList = SC\"AnnotationList"
  val Canvas = SC\"Canvas"
  val Manifest = SC\"Manifest"
  val Sequence = SC\"Sequence"
  val Zone = SC\"Zone"
}

object TEI extends Vocabulary(
  "http://www.tei-c.org/ns/1.0/"
)

object SGA extends Vocabulary(
  "http://www.shelleygodwinarchive.org/ns1#"
)

