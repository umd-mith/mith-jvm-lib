package edu.umd.mith.mining.analysis

import scala.io.Source

import org.specs2.mutable._
import org.specs2.specification.Scope

class PCAReducerTest extends SpecificationWithJUnit with PCAExperiment {
  "the output of PCA on the covariance matrix" should {
    "have 50 data points" in {
      this.cov.data.size must_== 50
    }
    "have 4 dimensions" in {
      this.cov.data.size must_== 50
    }
    "approximately match the output of R" in {
      this.cov.variance(0) must be ~(0.9655342 +/- this.eps) 
    }
    "approximately match the output of R" in {
      this.cov.variance(1) must be ~(0.0278173 +/- this.eps) 
    }
  }
  "the output of PCA on the covariance matrix" should {
    "have 50 data points" in {
      this.cor.data.size must_== 50
    }
    "have 4 dimensions" in {
      this.cor.data.size must_== 50
    }
    "approximately match the output of R" in {
      this.cor.variance(0) must be ~(0.6200604 +/- this.eps) 
    }
    "approximately match the output of R" in {
      this.cor.variance(1) must be ~(0.2474413 +/- this.eps) 
    }
  }
}

trait PCAExperiment extends Scope {
  val eps = 0.000001

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

