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

import net.liftweb.common.{ Box, Empty, Failure, Full }
import net.liftweb.http.{
  GetRequest,
  InMemoryResponse,
  LiftResponse,
  LiftRules,
  Req
}
import net.liftweb.mapper.{ By }
import org.imgscalr.Scalr._

class ImageCache(path: List[String]) {
  def matcher: LiftRules.DispatchPF = {
    case req @ Req(reqPath, _, GetRequest) if reqPath.startsWith(this.path) =>
      () => this.serve(reqPath.drop(path.size).headOption, req.params)
  }

  def image(
    source: SourceImage,
    rw: Int,
    rh: Option[Int],
    selection: Option[((Int, Int), (Int, Int))]
  ): Box[DerivativeImage] = {
    val selectionItem: Box[ImageSelection] = Box.option2Box(selection).flatMap {
      case ((x, y), (w, h)) => ImageSelection.find(
        By(ImageSelection.x, x),
        By(ImageSelection.y, y),
        By(ImageSelection.w, w),
        By(ImageSelection.h, h)
      )
    }

    val qs = Seq(
      By(DerivativeImage.source, source),
      By(DerivativeImage.selection, selectionItem),
      By(DerivativeImage.rw, rw)
    ) ++ rh.map(By(DerivativeImage.rh, _))

    DerivativeImage.find(qs: _*).orElse {
      import javax.imageio.ImageIO
      import org.imgscalr.Scalr

      try {
        val bio = ImageIO.read(new java.net.URL(source.url.is))
        
        val bic = selection.map {
          case ((x, y), (w, h)) => Scalr.crop(bio, x, y, w, h)
        }.getOrElse(bio)

        val bir = rh.map { h =>
          Scalr.resize(bic, Scalr.Method.BALANCED, Scalr.Mode.FIT_EXACT, rw, h)
        }.getOrElse {
          Scalr.resize(bic, Scalr.Method.BALANCED, Scalr.Mode.FIT_TO_WIDTH, rw)
        }

        val out = new java.io.ByteArrayOutputStream()
        ImageIO.write(bir, "png", out)
        val bytes = out.toByteArray

        val newSelectionItem: Box[ImageSelection] = selectionItem.orElse {
          Box.option2Box(selection).map {
            case ((x, y), (w, h)) =>
              val sel = ImageSelection.create.x(x).y(y).w(w).h(h)
              if (!sel.save()) {
                return Failure("Could not create image selection.")
              }
              sel
          }
        }

        val derivative = DerivativeImage.create
          .source(source)
          .selection(newSelectionItem)
          .rw(bir.getWidth)
          .rh(bir.getHeight)
          .content(bytes)

        if (!derivative.save())
          Failure("Could not create image derivative.")
        else
          Full(derivative)
      } catch {
        case exIo: java.io.IOException =>
          new Failure("Cannot access source image.", Full(exIo), Empty)
        case exIm: java.awt.image.ImagingOpException =>
          new Failure("Imaging operation failed.", Full(exIm), Empty)
        case exIa: IllegalArgumentException =>
          new Failure("Invalid cropping coordinates.", Full(exIa), Empty)
        case ex: Exception =>
          new Failure("Derivative creation failed.", Full(ex), Empty)
      }
    } 
  }

  def parseInt(s: String) = try Some(s.toInt) catch {
    case _: NumberFormatException => None
  }

  def md5(s: String) = new java.math.BigInteger(
    1, java.security.MessageDigest.getInstance("MD5").digest(s.getBytes)
  ).toString(16)

  def serve(name: Option[String], params: Map[String, List[String]]):
    Box[LiftResponse] = {
    val url = params.get("url").flatMap(_.lastOption)
    val x = params.get("x").flatMap(_.lastOption).flatMap(parseInt)
    val y = params.get("y").flatMap(_.lastOption).flatMap(parseInt)
    val w = params.get("w").flatMap(_.lastOption).flatMap(parseInt)
    val h = params.get("h").flatMap(_.lastOption).flatMap(parseInt)

    val selection = for {
      xv <- x
      yv <- y
      wv <- w
      hv <- h
    } yield ((xv, yv), (wv, hv))

    val rw = params.get("rw").flatMap(_.lastOption).map(parseInt)
    val rh = params.get("rh").flatMap(_.lastOption).map(parseInt)

    val qs = (
      name.map(v => By(SourceImage.name, v)) ++ 
      url.map(v => By(SourceImage.url, v))
    ).toSeq

    if (qs.isEmpty)
      Failure("No image specified.")
    else {
      val source = SourceImage.find(qs: _*).orElse {
        Box.option2Box(url).map { u =>
          val newSource = SourceImage.create.url(u).name(
            name.getOrElse(md5(u))
          )

          if (newSource.save)
            Full(newSource)
          else
            Failure("Could not create source image.")
        }.getOrElse {
          Failure("No URL specified.")
        }
      }

      source.flatMap(this.image(_, rw.get, rh, selection)).map { derivative =>
        InMemoryResponse(
          derivative.content.is,
          List(
            "Content-Type" -> "image/png",
            "Content-Length" -> derivative.content.is.length.toString
          ),
          Nil,
          200
        )
      }
    }
  }
}

