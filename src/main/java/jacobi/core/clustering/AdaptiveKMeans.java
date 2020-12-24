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
package jacobi.core.clustering;

import java.util.Collections;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.ToDoubleBiFunction;
import java.util.stream.IntStream;

import jacobi.api.Matrix;
import jacobi.core.util.Weighted;

/**
 * Implementation the adaptive k-means algorithm.
 * 
 * <p>A major drawback of the k-means algorithm is that it takes manual effort to specify the
 * value of k, which often resorts to random guessing. Adaptive K-means runs for a range of values
 * of k and pick the one that best fit the data based on a measurement.</p>
 * 
 * @author Y.K. Chan
 *
 */
public class AdaptiveKMeans implements Clustering {
	
	/**
	 * Constructor.
	 * @param clusteringFactory  Function that for a given k, returns the clustering implementation
	 * @param measureFn  Measurement of fitness
	 * @param minK  Minimum number of clusters
	 * @param maxK  Maximum number of clusters
	 */
	public AdaptiveKMeans(IntFunction<Clustering> clusteringFactory, 
			ToDoubleBiFunction<Matrix, List<int[]>> measureFn,
			int minK, int maxK) {
		this.clusteringFactory = clusteringFactory;
		this.measureFn = measureFn;
		this.minK = minK;
		this.maxK = maxK;
	}
	
	@Override
	public List<int[]> compute(Matrix matrix) {
		if(this.minK > this.maxK || this.maxK < 1){
			return Collections.emptyList();
		}
		
		if(this.maxK < 2){
			return Collections.singletonList(IntStream
				.range(0, matrix.getRowCount())
				.toArray()
			);
		}
		
		Weighted<List<int[]>> optima = null;
		int kMin = this.minK;
		int kMax = this.maxK + 1;
		
		double[] measurement = new double[kMax - kMin];
		
		for(int k = kMin; k < kMax; k++){
			Clustering clusteringFn = this.clusteringFactory.apply(k);
			List<int[]> clusters = clusteringFn.compute(matrix);
			double fitness = this.measureFn.applyAsDouble(matrix, clusters);
			
			System.out.println("k = " + k + ", fit = " + fitness);
			
			if(optima != null && optima.weight > fitness){
				break;
			}
			
			int i = k - kMin;
			measurement[i] = fitness;
			optima = new Weighted<>(clusters, fitness);
		}
		
		return optima.item;
	}

	private IntFunction<Clustering> clusteringFactory;
	private ToDoubleBiFunction<Matrix, List<int[]>> measureFn;
	private int minK, maxK;
}
