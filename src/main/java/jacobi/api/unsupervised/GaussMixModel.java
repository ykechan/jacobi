/* 
 * The MIT License
 *
 * Copyright 2020 Y.K. Chan
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package jacobi.api.unsupervised;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.DoubleSupplier;
import java.util.function.Function;
import java.util.stream.IntStream;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.core.clustering.AbstractEMClustering;
import jacobi.core.clustering.DiagGaussMixModel;
import jacobi.core.clustering.FullGaussMixModel;
import jacobi.core.clustering.IterativeClustering;
import jacobi.core.clustering.KMeansPP;
import jacobi.core.util.ParallelSupplier;
import jacobi.core.util.Throw;

/**
 * Comprehensive implementation to clustering by Gaussian Mixture Model.
 * 
 * @author Y.K. Chan
 *
 */
public class GaussMixModel {
	
	/**
	 * Default number of epochs for k-means
	 */
	public static final int DEFAULT_NUM_EPOCHS = 4;
	
	/**
	 * Sampling factor for k-means++ initialization
	 */
	public static final int DEFAULT_KMEANS_PP_SAMPLING = 4;
	
	/**
     * Suggested maximum number of flop to parallelize.
     */
	public static final int DEFAULT_FLOP_THRESHOLD = ParallelSupplier.DEFAULT_FLOP_THRESHOLD;
	
	/**
	 * Default random function using ThreadLocalRandom
	 */
	public static final DoubleSupplier DEFAULT_RAND_FUNC = () -> ThreadLocalRandom.current().nextDouble();
	
	/**
	 * Constructor.
	 */
	public GaussMixModel() {
		this(DEFAULT_RAND_FUNC);
	}

	/**
	 * Constructor.
	 * @param randFn  Random function
	 */
	public GaussMixModel(DoubleSupplier randFn) {
		this.randFn = randFn;
	}

	/**
	 * Clustering using Gaussian mixture model. If number of data is insufficient,
	 * the co-variance matrix is deflated to a diagonal matrix.
	 * @param matrix  Input matrix
	 * @param k  Number of clusters
	 * @return  Row indices of members of each clusters
	 */
	public List<int[]> compute(Matrix matrix, int k) {
		Throw.when()
			.isNull(() -> matrix, () -> "No data to cluster");
		
		int numCols = matrix.getColCount();
		long numParam = numCols + (numCols * numCols) / 2;
		return this.compute(matrix, k, numParam < Math.log(matrix.getRowCount()));
	}
	
	/**
	 * Clustering using Gaussian mixture model.
	 * @param matrix  Input matrix
	 * @param k  Number of clusters
	 * @param full  True for consider full co-variance matrix, deflate to diagonal matrix otherwise
	 * @return  Row indices of members of each clusters
	 */
	public List<int[]> compute(Matrix matrix, int k, boolean full) {
		Throw.when()
			.isNull(() -> matrix, () -> "No data to cluster")
			.isTrue(() -> k < 1, () -> "Invalid number of clusters " + k)
			.isTrue(
				() -> matrix.getRowCount() < k, 
				() -> "Unable to find " + k + " clusters within " + matrix.getRowCount() + " instances."
			);
	
		if(k < 2){
			return Collections.singletonList(IntStream.range(0, matrix.getRowCount()).toArray());
		}
		
		AbstractEMClustering<?> em = this.initialize(k, full);
		int epochs = DEFAULT_NUM_EPOCHS;
		
		return new IterativeClustering(em, epochs, em).compute(matrix);
	}
	
	protected AbstractEMClustering<?> initialize(int k, boolean full) {
		long flop = DEFAULT_FLOP_THRESHOLD;
		
		Function<Matrix, Matrix> initFn = new KMeansPP(
			randFn, k, DEFAULT_KMEANS_PP_SAMPLING, flop
		);
		
		if(full){
			return new FullGaussMixModel(initFn.andThen(m -> Arrays.asList(m.toArray())), flop);
		}
		
		return new DiagGaussMixModel(initFn.andThen(m -> new AbstractList<Matrix>(){

			@Override
			public Matrix get(int index) {
				double[] centroid = m.getRow(index);
				double[] var = new double[centroid.length];
				
				Arrays.fill(var, 1.0);
				return Matrices.wrap(centroid, var);
			}

			@Override
			public int size() {
				return m.getRowCount();
			}
			
		}), flop);
	}
	
	private DoubleSupplier randFn;
}
