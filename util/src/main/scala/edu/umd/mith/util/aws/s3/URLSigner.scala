/*
 * #%L
 * MITH General Utilities
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
package edu.umd.mith.util.aws.s3

import javax.crypto.spec.{ SecretKeySpec => SKS }
import org.apache.commons.codec.binary.{ Base64 => B64 }

case class Params(ps: (String, String)*) {
  override def toString = "?" + this.ps.map {
    case (k, v) => "%s=%s".format(k, java.net.URLEncoder.encode(v, "UTF-8"))
  }.mkString("&")
}

case class Key(id: String, secret: String)
case class Loc(bucket: String, base: String) {
  override def toString = bucket + base
}

trait URLSigner {
  def key: Key
  private val m = javax.crypto.Mac.getInstance("HmacSHA1")
  this.m.init(new SKS(this.key.secret.getBytes("UTF-8"), this.m.getAlgorithm))
  protected def sign(message: String) = new String(B64.encodeBase64(
    this.m.doFinal(message.getBytes("UTF-8"))
  ))
}

case class DirectorySigner(
  key: Key,
  loc: Loc,
  expires: java.util.Date
) extends URLSigner {
  implicit def toFile(s: String) = new java.io.File(s)
  implicit def toDate(s: String) = new java.text.SimpleDateFormat().parse(s)
  implicit def toEpoch(d: java.util.Date) = (d.getTime / 1000).toString

  val req = "GET\n\n\n%d\n/%s".format(this.expires.getTime / 1000, this.loc)
  def apply(dir: java.io.File) = for {
    f <- dir.listFiles.sorted
    if f.isFile && f.getName.endsWith("tif")
  } yield (
    f.getName, "https://s3.amazonaws.com/" + this.loc + f.getName + Params(
      "AWSAccessKeyId" -> this.key.id,
      "Expires" -> this.expires,
      "Signature" -> this.sign(
        "GET\n\n\n%s\n/%s%s".format(this.expires: String, this.loc, f.getName)
      )
    )
  ) 
}

