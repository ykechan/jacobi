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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.DoubleSupplier;
import java.util.function.ToDoubleBiFunction;
import java.util.stream.IntStream;

import jacobi.api.Matrix;
import jacobi.core.clustering.AdaptiveKMeans;
import jacobi.core.clustering.Clustering;
import jacobi.core.clustering.EuclideanCluster;
import jacobi.core.clustering.ExpectationMaximization;
import jacobi.core.clustering.IterativeClustering;
import jacobi.core.clustering.KMeansPP;
import jacobi.core.clustering.PseudoSilhouetteCoeff;
import jacobi.core.clustering.WithinClusterDistance;
import jacobi.core.util.Throw;

/**
 * Comprehensive implementation to K-means clustering in various flavours.
 * 
 * <p>This class uses default settings, which in most cases works generally well, for the ease
 * of developers.</p>
 * 
 * @author Y.K. Chan
 */
public class KMeans {
	
	/**
	 * Default number of FLOPs to start parallelizing
	 */
	public static final long DEFAULT_FLOP_THRESHOLD = 4096L;
	
	/**
	 * Sampling factor for k-means++ initialization
	 */
	public static final int DEFAULT_KMEANS_PP_SAMPLING = 4;
	
	/**
	 * Default number of epochs for k-means
	 */
	public static final int DEFAULT_NUM_EPOCHS = 8;
	
	/**
	 * Constructor.
	 */
	public KMeans() {
		this.randFn = () -> ThreadLocalRandom.current().nextDouble();
		this.measureIntra = WithinClusterDistance.of(EuclideanCluster.getInstance());
		this.measureInter = new PseudoSilhouetteCoeff(EuclideanCluster.getInstance(), DEFAULT_FLOP_THRESHOLD);
	}

	/**
	 * K-means clustering with number of clusters specified. 
	 * @param matrix  Input matrix as collection of row vectors
	 * @param k  Number of clusters
	 * @return  Row indices for each cluster
	 */
	public List<int[]> compute(Matrix matrix, int k) {
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
		
		long flop = DEFAULT_FLOP_THRESHOLD;
		int minRank = k * DEFAULT_KMEANS_PP_SAMPLING;
		
		KMeansPP init = new KMeansPP(EuclideanCluster.getInstance(),
			n -> (int) Math.floor(n * this.randFn.getAsDouble()),
			k, flop
		);
		
		Clustering kmeans = new ExpectationMaximization<>(init, EuclideanCluster.getInstance(), flop);
		
		int numIter = DEFAULT_NUM_EPOCHS;
		return new IterativeClustering(this.measureIntra, Math.max(3, numIter), kmeans).compute(matrix);
	}
	
	/**
	 * K-means clustering with minimum and maximum number of clusters specified
	 * @param matrix  Input matrix as collection of row vectors
	 * @param kMin  Minimum number of clusters to consider
	 * @param kMax  Maximum number of clusters to consider
	 * @return  Row indices for each cluster
	 */
	public List<int[]> compute(Matrix matrix, int kMin, int kMax) {
		if(kMin < 1 || kMax < 1){
			throw new IllegalArgumentException("Invalid number of clusters [" + kMin + "," + kMax + "].");
		}
		
		Clustering clusteringFn = new AdaptiveKMeans(
			k -> m -> this.compute(m, k),
			this.measureInter,
			kMin, kMax
		);
		
		return clusteringFn.compute(matrix);
	}
	
	/**
	 * Wrap a matrix into a list
	 * @param matrix  Input matrix
	 * @return  List of row vectors
	 */
	protected List<double[]> toList(Matrix matrix) {
		return new AbstractList<double[]>() {

			@Override
			public double[] get(int index) {
				return matrix.getRow(index);
			}

			@Override
			public int size() {
				return matrix.getRowCount();
			}
			
		};
	}

	private DoubleSupplier randFn;
	private ToDoubleBiFunction<Matrix, List<int[]>> measureIntra, measureInter;
}
