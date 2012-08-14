/*
 * #%L
 * MITH General Utilities
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
package edu.umd.mith.util.pairtree

import scala.reflect.BeanProperty

object Pairtree {
  val HexIndicator = "^"
}

class Pairtree(
  @BeanProperty var separator: Char,
  @BeanProperty var shortyLength: Int
) {
  def this() = this(java.io.File.separatorChar, 2)

  def mapToPPath(id: String): String = {
    require(id != null)
    this.concat(this.cleanId(id).grouped(this.shortyLength).toSeq: _*)
  }
  
  def mapToPPath(basePath: String, id: String, encapsulatingDirName: String): String =
    this.concat(basePath, this.mapToPPath(id), encapsulatingDirName)

  def mapToId(basepath: String, ppath: String): String = 
    this.mapToId(this.removeBasepath(basepath, ppath))

  def mapToId(path: String): String = this.uncleanId {
    val encapsulatingDir = this.extractEncapsulatingDirFromPpath(path)
    val id = if (path.endsWith(this.separator.toString)) path.init else path
    (if (encapsulatingDir != null) id.substring(0, id.length - encapsulatingDir.length) else id).replace(this.separator.toString, "")
  }

  def extractEncapsulatingDirFromPpath(basepath: String, ppath: String): String =
    this.extractEncapsulatingDirFromPpath(this.removeBasepath(basepath, ppath))
  
  def extractEncapsulatingDirFromPpath(ppath: String): String = {
    require(ppath != null)
    
    //Walk the ppath looking for first non-shorty
    val ppathParts = ppath.split("\\" + this.separator)
    
    //If there is only 1 part
    if (ppathParts.length == 1) {
      //If part <= shorty length then no encapsulating dir
      if (ppathParts(0).length <= this.shortyLength) null
      //Else no ppath
      else
        throw new InvalidPpathException("Ppath (%s) contains no shorties" format ppath)
    } else {
      //All parts up to next to last and last should have shorty length
      ppathParts.take(ppathParts.size - 2).find(_.size != this.shortyLength).foreach {
        _ => throw new InvalidPpathException(
          "Ppath (%s) has parts of incorrect length" format ppath
        )
      }

      val nextToLastPart = ppathParts(ppathParts.length - 2)
      val lastPart = ppathParts(ppathParts.length - 1)
      //Next to last should have shorty length or less
      if (nextToLastPart.length > this.shortyLength)
        throw new InvalidPpathException("Ppath (%s) has parts of incorrect length" format ppath)

      //If next to last has shorty length
      if (nextToLastPart.length == this.shortyLength)
        //If last has length > shorty length then encapsulating dir
        if (lastPart.length > this.shortyLength) lastPart else null
      else lastPart
    }      
  }

  private def concat(paths: String*): String = if (paths.isEmpty) null else
    paths.filterNot(_ == null).map(
      path => if (path.last == this.separator) path.init else path
    ).mkString(this.separator.toString)

  def removeBasepath(basePath: String, path: String) = {
    require(basePath != null)
    require(path != null)

    var newPath = path    
    if (path.startsWith(basePath)) {
      newPath = newPath.substring(basePath.length)
      if (newPath.startsWith(this.separator.toString))
        newPath = newPath.substring(1)
    }
    newPath
  }
  
  def cleanId(id: String) = {
    require(id != null)
    //First pass
    val bytes = try id.getBytes("utf-8") catch {
      case e: java.io.UnsupportedEncodingException => 
        throw new RuntimeException("Error getting UTF-8 for path", e)
    }

    val buffer = new StringBuffer
    bytes.foreach { byte =>
      val i = (byte & 0xff).toInt
      if (
        i < 0x21  || i > 0x7e  || i == 0x22 || i == 0x2a || i == 0x2b ||
        i == 0x2c  || i == 0x3c || i == 0x3d || i == 0x3e || i == 0x3f ||
        i == 0x5c  || i == 0x5e || i == 0x7c
      ) {
        //Encode
        buffer.append(Pairtree.HexIndicator)
        buffer.append(Integer.toHexString(i))
      } else {
        //Don't encode
        val chars = Character.toChars(i)
        assert(chars.length == 1)
        buffer.append(chars(0))
      }
    }

    buffer.toString.replace('/', '=').replace(':', '+').replace('.', ',')
  }
  
  def uncleanId(id: String) = {
    require(id != null)
    val buffer = new StringBuffer
    var c = 0
    while (c < id.length) {
      id.charAt(c) match {
        case '=' => buffer.append('/')
        case '+' => buffer.append(':')
        case ',' => buffer.append('.')
        case '^' => {
          val chars = Character.toChars(Integer.parseInt(id.substring(c + 1, c + 3), 16))
          assert(chars.length == 1)
          buffer.append(chars(0))
          c += 2
        }
        case chr => buffer.append(chr)
      }
      c += 1
    }
    buffer.toString
  }
 
  @SerialVersionUID(1L)
  class InvalidPpathException(msg: String) extends Exception(msg)
}

