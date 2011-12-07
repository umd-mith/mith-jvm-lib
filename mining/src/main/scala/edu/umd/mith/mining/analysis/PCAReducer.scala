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

import cern.colt.matrix.impl.DenseDoubleMatrix2D
import cern.colt.matrix.doublealgo.Statistic
import cern.colt.matrix.linalg.EigenvalueDecomposition
import cern.colt.matrix.linalg.Algebra
import cern.jet.math.Functions

case class PCAReduced(
  data: Array[Array[Double]],
  variance: Array[Double],
  loadings: Array[Array[Double]]
) extends Reduced

class PCAReducer(val corr: Boolean) extends Reducer[PCAReduced] {
  def this() = this(false)

  private val algebra = new Algebra

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

