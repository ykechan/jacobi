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

import java.util.List;
import java.util.function.ToDoubleBiFunction;

import jacobi.api.Matrix;
import jacobi.core.util.Weighted;

/**
 * Implementation of iterative clustering.
 * 
 * <p>Most clustering algorithms are not deterministic. This class repeats the same algorithms
 * a number of times and pick the best result by some fitness measurement function.</p>
 * 
 * @author Y.K. Chan
 */
public class IterativeClustering implements Clustering {
	
	/**
	 * Constructor.
	 * @param measureFn  Measurement function of the fitness of clusters
	 * @param epochs  Maximum number of iteration
	 * @param clusteringFn  K-mean implementation. Can be other non-deterministic algorithms.
	 */
	public IterativeClustering(ToDoubleBiFunction<Matrix, List<int[]>> measureFn, int epochs, Clustering clusteringFn) {
		this.measureFn = measureFn;
		this.epochs = epochs;
		this.clusteringFn = clusteringFn;
	}

	@Override
	public List<int[]> compute(Matrix matrix) {
		return this.run(matrix).item;
	}
	
	/**
	 * Run the epochs of clustering on the given matrix, and returns the best result
	 * @param matrix  Input matrix
	 * @return  Clustering result and measurement
	 */
	public Weighted<List<int[]>> run(Matrix matrix) {
		Weighted<List<int[]>> min = null;
		int num = this.epochs;
		
		for(int i = 0; i < num; i++){
			List<int[]> clusters = this.clusteringFn.compute(matrix);
			double dist = this.measureFn.applyAsDouble(matrix, clusters);
			
			if(min == null || min.weight < dist){
				min = new Weighted<>(clusters, dist);
			}
		}
		
		if(min == null){
			throw new UnsupportedOperationException("No iteration");
		}
		return min;
	}

	private ToDoubleBiFunction<Matrix, List<int[]>> measureFn;
	private int epochs;
	private Clustering clusteringFn;
}
