/*
 * #%L
 * MITH Data Mining Utilities
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
package edu.umd.mith.mining.analysis

import scala.io.Source

import org.specs2.mutable._
import org.specs2.specification.Scope

import edu.umd.mith.mining.DoubleSeqMatchers

class PCAReducerTest extends SpecificationWithJUnit with PCAExperiment {
  "the output of PCA on the covariance matrix" should {
    "have 50 data points" in {
      this.cov.data.size must_== 50
    }
    "have 4 dimensions" in {
      this.cov.dims must_== 4
    }
    "approximately match the variance output of R" in {
      this.cov.variance.toSeq must ~= (
        0.9655342, 0.02781734, 0.005799535, 0.000848907
      )
    }
    "approximately match the loadings output of R" in {
      // TODO: Determine why the sign is the opposite of R's.
      this.cov.loadings(0).toSeq must ~= (
        0.04170432, 0.99522128, 0.04633575, 0.07515550
      )
      this.cov.loadings(1).toSeq must ~= (
        -0.04482166, -0.05876003, 0.97685748, 0.20071807
      ) 
    }
  }
  "the output of PCA on the correlation matrix" should {
    "have 50 data points" in {
      this.cor.data.size must_== 50
    }
    "have 4 dimensions" in {
      this.cor.dims must_== 4
    }
    "approximately match the variance output of R" in {
      this.cor.variance.toSeq must ~= (
        0.6200604, 0.2474413, 0.0891408, 0.04335752
      )
    }
    "approximately match the loadings output of R" in {
      // TODO: Determine why the sign is the opposite of R's.
      this.cor.loadings(0).toSeq must ~= (
        0.5358995, 0.5831836, 0.2781909, 0.5434321
      )
      this.cor.loadings(1).toSeq must ~= (
        -0.4181809, -0.1879856, 0.8728062, 0.1673186
      ) 
    }
  }
}

trait PCAExperiment extends Scope with DoubleSeqMatchers {
  val eps = 1.0E-6

  val data: Array[Array[Double]] = Source.fromFile(
    this.getClass.getResource("USArrests.dat").toURI
  ).getLines.drop(1).map(
    _.drop(16).split("\\s+").drop(1).map(_.toDouble).toArray
  ).toArray

  val covReducer = new PCAReducer(false)
  val corReducer = new PCAReducer(true)

  val cov = this.covReducer.reduce(this.data)
  val cor = this.corReducer.reduce(this.data)
}

