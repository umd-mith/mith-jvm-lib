/*
 * #%L
 * MITH Lift Web Application Utilities
 * %%
 * Copyright (C) 2011 - 2012 Maryland Institute for Technology in the Humanities
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
package edu.umd.mith.util.lift.image

import net.liftweb.mapper._

class SourceImage extends LongKeyedMapper[SourceImage]
  with IdPK with OneToMany[Long, SourceImage] {
  def getSingleton = SourceImage

  object url extends MappedString(this, 512)
  object name extends MappedString(this, 40)
  object w extends MappedInt(this)
  object h extends MappedInt(this)

  object derivatives extends MappedOneToMany(
    DerivativeImage,
    DerivativeImage.source
  )
}

object SourceImage
  extends SourceImage
  with LongKeyedMetaMapper[SourceImage] {
  override def dbTableName = "_mith_lift_util_source_images"
  override def dbIndexes =
    Index(IndexField(url)) :: Index(IndexField(name)) :: Nil
}

class DerivativeImage extends LongKeyedMapper[DerivativeImage] with IdPK {
  def getSingleton = DerivativeImage

  object source extends MappedLongForeignKey(this, SourceImage)
  object selection extends MappedLongForeignKey(this, ImageSelection)
  object rw extends MappedInt(this)
  object rh extends MappedInt(this)
  object content extends MappedBinary(this)
}

object DerivativeImage
  extends DerivativeImage
  with LongKeyedMetaMapper[DerivativeImage] {
  override def dbTableName = "_mith_lift_util_derivative_images"
  override def dbIndexes = Index(
    IndexField(source),
    IndexField(selection),
    IndexField(rw),
    IndexField(rh)
  ) :: Nil
}

class ImageSelection extends LongKeyedMapper[ImageSelection] with IdPK {
  def getSingleton = ImageSelection

  object x extends MappedInt(this)
  object y extends MappedInt(this)
  object w extends MappedInt(this)
  object h extends MappedInt(this)
}

object ImageSelection
  extends ImageSelection
  with LongKeyedMetaMapper[ImageSelection] {
  override def dbTableName = "_mith_lift_util_image_selections"
  override def dbIndexes =
    Index(IndexField(x), IndexField(y), IndexField(w), IndexField(h)) :: Nil
}

