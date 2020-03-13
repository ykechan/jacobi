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
package jacobi.core.spatial.rtree;

import java.util.List;
import java.util.stream.IntStream;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.core.spatial.sort.Fractal2D;
import jacobi.core.spatial.sort.FractalSort2D;
import jacobi.core.spatial.sort.HilbertSort3D;
import jacobi.core.spatial.sort.KdSort;
import jacobi.core.spatial.sort.SpatialSort;
import jacobi.core.stats.RowReduce;
import jacobi.core.util.Ranking;

/**
 * Default implementation of spatial sort for R-Tree construction.
 * 
 * @author Y.K. Chan
 *
 */
public class RSort implements SpatialSort {
	
	/**
	 * Get the default instance
	 * @return  Default instance
	 */
	public static SpatialSort getInstance() {
		return DEFAULT_INSTANCE;
	}
	
	/**
	 * Constructor.
	 * @param kdSort  Implementation of sorting in high dimensions
	 */
	protected RSort(SpatialSort kdSort) {
		this.kdSort = kdSort;
	}

	@Override
	public int[] sort(List<double[]> vectors) {
		if(vectors == null || vectors.isEmpty()){
			return new int[0];
		}
		
		int numDim = vectors.get(0).length;
		SpatialSort impl = numDim < DEFAULT_HIGH_DIM
				? sortLD(IntStream.range(0, numDim).toArray())
				: this.kdSort;
				
		return (impl == null ? this.kdSort : impl).sort(vectors);
	}
	
	private SpatialSort kdSort;

	/**
	 * Get implementation when there are only a few dimensions
	 * @param dims  List of dimensions
	 * @return  Implementation instance, or null if number of dimension is too large.
	 */
	protected static SpatialSort sortLD(int[] dims) {
		int numDim = dims == null ? 0 : dims.length;
		switch(numDim){
			case 0 :
				throw new IllegalArgumentException("No spatial dimension.");
				
			case 1 :
				int dim = dims[0];
				return ls -> Ranking.of(ls.size()).init(k -> ls.get(k)[dim]).sort();
				
			case 2 :
				return new FractalSort2D(dims[0], dims[1], Fractal2D.HILBERT);
				
			case 3 :
				return new HilbertSort3D(dims[0], dims[1], dims[2]);
		
			default :
				break;
		}
		
		return null;
	}
	
	/**
	 * Find the mean and variance of the given matrix. "Variance" is a non-negative measure
	 * of data disparity, and not necessarily the statistical variance.
	 * @param input  Input matrix
	 * @return  Output matrix with the first 2 rows as mean and variance
	 */
	protected static Matrix statsOf(Matrix input) {
		double[] u = new RowReduce.Mean().compute(input);
		double[] var = new RowReduce((a, b) -> {
			for(int i = 0; i < a.length; i++){
				a[i] += Math.abs(b[i] - u[i]);
			}
		}).compute(input);
		
		for(int i = 0; i < var.length; i++){
			var[i] /= input.getRowCount();
		}
		return Matrices.wrap(new double[][] {u, var});
	}
	
	/**
	 * Number of dimension that is considered too high.
	 */
	protected static final int DEFAULT_HIGH_DIM = 4;
	
	/**
	 * Proportion of variance to consider in a round of k-d sort.
	 */
	protected static final double PHI = (Math.sqrt(5.0) - 1.0) / 2.0;
	
	/**
	 * Default instance
	 */
	protected static final RSort DEFAULT_INSTANCE = 
		new RSort(new KdSort(RSort::sortLD, RSort::statsOf, RSort.PHI));
}
