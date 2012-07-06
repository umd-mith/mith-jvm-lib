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

case class PCAReduced(
  data: Array[Array[Double]],
  variance: Array[Double],
  loadings: Array[Array[Double]]
) extends Reduced

trait PCAReducer extends Reducer[PCAReduced] {
  def corr: Boolean
}

object MahoutPCAReducer {
  import org.apache.mahout.math.decomposer.lanczos.LanczosSolver
  val solver = new LanczosSolver
}

class MahoutPCAReducer(val corr: Boolean) extends PCAReducer {
  import org.apache.mahout.math.{ ConstantVector, DenseMatrix, DenseVector, Matrix, Vector }
  import org.apache.mahout.math.decomposer.lanczos.LanczosState
  import org.apache.mahout.math.function.Functions

  def this() = this(false)

  def mToArray(m: Matrix) = {
    var i = m.rowSize
    var j = m.columnSize
    val a = new Array[Array[Double]](i)

    while (i > 0) {
      i -= 1
      j = m.columnSize
      a(i) = new Array[Double](j)
      while (j > 0) {
        j -= 1
        a(i)(j) = m.getQuick(i, j)
      }
    }
    a
  }
      
  def vToArray(v: Vector) = {
    var i = v.size
    val a = new Array[Double](i)
    while (i > 0) {
      i -= 1
      a(i) = v.getQuick(i)
    }
    a
  }

  def reduce(data: Array[Array[Double]], dims: Int): PCAReduced = {
    val matrix = new DenseMatrix(data)
    var sums = Array.ofDim[Double](matrix.columnSize)

    /* First to subtract out the empirical mean. */
    val rows = matrix.rowSize
    for (i <- 0 until matrix.columnSize) {
      val col = matrix.viewColumn(i)
      val avg = col.zSum / rows
      col.assign(Functions.minus(avg))
      sums(i) = col.zSum
    }

    /* Next the eigenvalue decomposition of the covariance matrix. */
    val cov = matrix.like(matrix.columnSize, matrix.columnSize)
    for {
      i <- 0 until matrix.columnSize
      j <- 0 to i
    } {
      val sumOfProducts = matrix.viewColumn(i).dot(matrix.viewColumn(j))
      val c = (sumOfProducts - sums(i) * sums(j) / rows) / rows
      cov.setQuick(i, j, c)
      cov.setQuick(j, i, c)
    }
 
    if (this.corr)
      for {
        i <- 0 until matrix.columnSize
        j <- 0 to i
      } if (i == j) cov.setQuick(i, i, 1.0)
        else {
          val stdDev1 = math.sqrt(cov.getQuick(i, i))
          val stdDev2 = math.sqrt(cov.getQuick(j, j))
          val v = cov.getQuick(i, j) / (stdDev1 * stdDev2)
          cov.setQuick(i, j, v)
          cov.setQuick(j, i, v)
        }

    val initial = new ConstantVector(1 / math.sqrt(cov.columnSize), cov.columnSize)
    val state = new LanczosState(cov, dims * 2, initial)
    MahoutPCAReducer.solver.solve(state, dims * 2, true)

    val v = new DenseMatrix(matrix.columnSize, dims * 2)
    val evs = new DenseVector(dims * 2)
    for (i <- 0 until dims * 2) {
      v.assignColumn(i, state.getRightSingularVector(i))
      evs.setQuick(i, state.getSingularValue(dims * 2 - 1 - i))
   }

    /* Finally we take the projection of the points onto the new basis. */
    val projection = matrix.times(v)
    //val projection: DoubleMatrix2D = this.algebra.mult(matrix, evd.getV).assign(Functions.abs).viewColumnFlip

    val colsSelected = math.min(dims, projection.columnSize)
    val dataView = projection.viewPart(0, projection.rowSize, 0, colsSelected)

    val trace = cov.viewDiagonal.zSum

    val varianceView = evs.assign(Functions.div(trace)).viewPart(0, colsSelected)
    val loadingsView = v.transpose.viewPart(0, colsSelected, 0, v.rowSize)

    PCAReduced(mToArray(dataView), vToArray(varianceView), mToArray(loadingsView))
  }
}

class ColtPCAReducer(val corr: Boolean) extends PCAReducer {
  import cern.colt.matrix.impl.DenseDoubleMatrix2D
  import cern.colt.matrix.doublealgo.Statistic
  import cern.colt.matrix.linalg.EigenvalueDecomposition
  import cern.colt.matrix.linalg.Algebra
  import cern.jet.math.Functions

  def this() = this(false)

  private val algebra = Algebra.DEFAULT

  def reduce(data: Array[Array[Double]], dims: Int): PCAReduced = {
    val matrix = new DenseDoubleMatrix2D(data)

    /* First to subtract out the empirical mean. */
    val rows = matrix.rows
    for (i <- 0 until matrix.columns) {
      val col = matrix.viewColumn(i)
      val avg = col.zSum / rows
      col.assign(Functions.minus(avg))
    }
    
    /* Next the eigenvalue decomposition of the covariance matrix. */
    val cov = Statistic.covariance(matrix)
    if (this.corr) Statistic.correlation(cov)

    val evd = new EigenvalueDecomposition(cov)

    val v = evd.getV.viewColumnFlip

    /* Finally we take the projection of the points onto the new basis. */
    val projection = this.algebra.mult(matrix, v)
    //val projection: DoubleMatrix2D = this.algebra.mult(matrix, evd.getV).assign(Functions.abs).viewColumnFlip

    val colsSelected = math.min(dims, projection.columns)
    val dataView = projection.viewPart(0, 0, projection.rows, colsSelected)

    val evs = evd.getRealEigenvalues.viewFlip

    val varianceView = evs.assign(Functions.div(evs.zSum)).viewPart(0, colsSelected)
    val loadingsView = v.viewDice.viewPart(0, 0, colsSelected, v.rows)

    PCAReduced(dataView.toArray, varianceView.toArray, loadingsView.toArray)
  }
}

