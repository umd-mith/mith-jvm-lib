package edu.umd.mith.mining

import org.specs2.matcher.{ Matcher, MatchersImplicits, NumericMatchers }

trait DoubleSeqMatchers extends MatchersImplicits with NumericMatchers {
  def eps: Double

  def ~=(other: Double*): Matcher[Seq[Double]] =
    { v: Double => this.beCloseTo(v, this.eps) }.toSeq.apply(other)
}

