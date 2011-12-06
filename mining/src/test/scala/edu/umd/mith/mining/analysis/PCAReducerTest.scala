package edu.umd.mith.mining.analysis

import scala.io.Source

import org.specs2.mutable._
import org.specs2.specification.Scope

class PCAReducerTest extends SpecificationWithJUnit {
  "the reduced output" should {
    "have 4 data points" in new Experiment {
      this.reduce(true).data.size must_== 50
    }
    "have 4 data points" in new Experiment {
      this.reduce(false).data.size must_== 50
    }
  }
}

trait Experiment extends Scope {
  val data: Array[Array[Double]] = Source.fromFile(
    this.getClass.getResource("USArrests.dat").toURI
  ).getLines.drop(1).map(_.drop(16).split("\\s+").drop(1).map(_.toDouble).toArray).toArray

  def reduce(corr: Boolean) = {
    val r = new PCAReducer(corr).reduce(data)
    r.variance.foreach(println)
    r
  }
}

