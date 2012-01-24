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
package edu.umd.mith.util.aws.s3

import scala.collection.JavaConversions._
import scala.io.Source
import org.jets3t.service._
import org.jets3t.service.impl.rest.httpclient.RestS3Service

// This is a very messy one-off script at this point, but we may need it
// again.
object HathiCleanup extends App {
  def escape(id: String): (String, String) = {
    val first = id.indexOf('.')
    val collection = id.substring(0, first)
    val remainder = id.substring(first + 1)
    val dirName = remainder.replace('.', ',')
                           .replace(':', '+')
                           .replace('/', '=')
    (collection, dirName)
  }

  def unescape(dirName: String) =
    dirName.replace(',', '.')
           .replace('+', ':')
           .replace('=', '/')

  def path(id: (String, String)) =
    "non_google_pd_pdus/" + id._1 + "/pairtree_root/" +
    id._2.grouped(2).mkString("/") + "/" + id._2

  def ids = Source.fromFile(args(2)).getLines

  def printFileScript {
    ids.foreach(l => println("rm -r " + path(escape(l))))
  }

  def deleteS3Files {
    val cred = new security.AWSCredentials(args(0), args(1))
    val service = new RestS3Service(cred)
    val bucket = service.getBucket("hathi")
    this.ids.map(
      id => service.listObjects("hathi", path(escape(id)), null)
    ).foreach(_.foreach {
      o => println("deleting " + o.getKey); service.deleteObject(bucket, o.getKey)
    })

    //this.ids.map(
    //  id => service.listObjects("hathi", path(escape(id)), null)
    //).foreach(_.foreach(println(_)))
  }
}

