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
    "approximately match the variance output of R" in {
      this.cov.variance(0) must be ~(0.9655342 +/- this.eps) 
      this.cov.variance(1) must be ~(0.02781734 +/- this.eps) 
      this.cov.variance(2) must be ~(0.005799535 +/- this.eps) 
      this.cov.variance(3) must be ~(0.000848907 +/- this.eps) 
    }
    "approximately match the loadings output of R" in {
      // TODO: Determine why the sign is the opposite of R's.
      this.cov.loadings(0)(0) must be ~(0.04170432 +/- this.eps)
      this.cov.loadings(0)(1) must be ~(0.99522128 +/- this.eps) 
      this.cov.loadings(0)(2) must be ~(0.04633575 +/- this.eps) 
      this.cov.loadings(0)(3) must be ~(0.07515550 +/- this.eps) 
      this.cov.loadings(1)(0) must be ~(-0.04482166 +/- this.eps) 
      this.cov.loadings(1)(1) must be ~(-0.05876003 +/- this.eps) 
      this.cov.loadings(1)(2) must be ~(0.97685748 +/- this.eps) 
      this.cov.loadings(1)(3) must be ~(0.20071807 +/- this.eps) 
    }
  }
  "the output of PCA on the covariance matrix" should {
    "have 50 data points" in {
      this.cor.data.size must_== 50
    }
    "have 4 dimensions" in {
      this.cor.data.size must_== 50
    }
    "approximately match the variance output of R" in {
      this.cor.variance(0) must be ~(0.6200604 +/- this.eps) 
      this.cor.variance(1) must be ~(0.2474413 +/- this.eps) 
      this.cor.variance(2) must be ~(0.0891408 +/- this.eps) 
      this.cor.variance(3) must be ~(0.04335752 +/- this.eps) 
    }
    "approximately match the loadings output of R" in {
      // TODO: Determine why the sign is the opposite of R's.
      this.cor.loadings(0)(0) must be ~(0.5358995 +/- this.eps) 
      this.cor.loadings(0)(1) must be ~(0.5831836 +/- this.eps) 
      this.cor.loadings(0)(2) must be ~(0.2781909 +/- this.eps) 
      this.cor.loadings(0)(3) must be ~(0.5434321 +/- this.eps) 
      this.cor.loadings(1)(0) must be ~(-0.4181809 +/- this.eps) 
      this.cor.loadings(1)(1) must be ~(-0.1879856 +/- this.eps) 
      this.cor.loadings(1)(2) must be ~(0.8728062 +/- this.eps) 
      this.cor.loadings(1)(3) must be ~(0.1673186 +/- this.eps) 
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

