package edu.umd.mith.mining.analysis

trait Reduced {
  def data: Array[Array[Double]]
  def dims: Int = this.data.headOption.map(_.length).getOrElse(0)
}

trait Reducer[B <: Reduced] {
  def reduce(data: Array[Array[Double]], dims: Int): B
  def reduce(data: Array[Array[Double]]): B = this.reduce(
    data,
    data.headOption.map(_.length).getOrElse(0)
  )
}

