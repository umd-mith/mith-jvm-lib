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

