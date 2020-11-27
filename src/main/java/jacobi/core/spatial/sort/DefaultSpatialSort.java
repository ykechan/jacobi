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
package jacobi.core.spatial.sort;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.DoubleSupplier;
import java.util.stream.IntStream;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.core.util.MinHeap;
import jacobi.core.util.Ranking;

/**
 * Implementation for default spatial sorting algorithm.
 * 
 * <p>This is the go-to implementation of spatial sorting for the general case</p>
 * 
 * <p>For 1-D case, it uses a sample scalar sort. For 2 and 3-D cases it uses the Hilbert sort.</p>
 * 
 * <p>For higher dimension it uses a Z-sort, with sampling median and absolute variance.</p>
 * 
 * @author Y.K. Chan
 *
 */
public class DefaultSpatialSort implements SpatialSort {
	
	/**
	 * Default R-Square value for Z-sort
	 */
	public static final double DEFAULT_R_SQUARE = 0.75;
	
	/**
	 * Default sampling size
	 */
	public static final int DEFAULT_SAMPLING_SIZE = 13;
	
	public static DefaultSpatialSort getInstance() {
		return INST;
	}
	
	/**
	 * Constructor
	 * @param rand  Random function
	 * @param sampleSize  Sampling size for sample median / variance
	 */
	public DefaultSpatialSort(DoubleSupplier rand, int sampleSize, double rSquare) {
		this.rand = rand;
		this.sampleSize = sampleSize;
		this.zSort = new ZSort(
			this::sampleMedianAndVar,
			this::getSortingFunction,
			DEFAULT_R_SQUARE
		);
	}

	@Override
	public int[] sort(List<double[]> vectors) {
		if(vectors.isEmpty()){
			return new int[0];
		}
		
		int dim = vectors.get(0).length;
		switch(dim){
			case 0:
				return new int[0];
				
			case 1:
				// not a spatial sort, but ok
				return Ranking.of(vectors.size()).init(i -> vectors.get(i)[0]).sort();
			
			case 2:
				return DEFAULT_SORT_2D.sort(vectors);
				
			case 3:
				return DEFAULT_SORT_3D.sort(vectors);
				
			default:
				break;
		}
		
		// high dimension
		return this.zSort.sort(vectors);
	}
	
	protected SpatialSort getSortingFunction(int[] dim) {
		switch(dim.length){
			case 0:
				throw new IllegalArgumentException("No dimension to sort");
				
			case 1:
				int d = dim[0];
				return ls -> Ranking.of(ls.size()).init(i -> ls.get(i)[d]).sort();
				
			case 2:
				return new FractalSort2D(dim[0], dim[1], Fractal2D.HILBERT);
				
			case 3:
				return new HilbertSort3D(dim[0], dim[1], dim[2]);
				
			default:
				break;
		}
		return null;
	}
	
	/**
	 * Compute the median and absolute variance by sampling
	 * @param vectors  Input vectors 
	 * @return  Statistics matrix
	 */
	protected Matrix sampleMedianAndVar(List<double[]> vectors) {
		if(vectors.isEmpty()){
			return Matrices.zeros(0);
		}
		
		int dim = vectors.get(0).length;
		
		if(vectors.size() == 1){
			return Matrices.wrap(new double[][]{
				Arrays.copyOf(vectors.get(0), dim),
				new double[dim]
			});
		}
		
		if(vectors.size() == 2){
			double[] lower = vectors.get(0);
			double[] upper = vectors.get(1);
			
			return Matrices.wrap(new double[][]{
				IntStream.range(0, dim).mapToDouble(i -> (upper[i] + lower[i]) / 2).toArray(),
				IntStream.range(0, dim).mapToDouble(i -> Math.abs(upper[i] - lower[i])).toArray()
			});
		}
		
		if(vectors.size() <= this.sampleSize){
			return this.actualMedianAndVar(vectors);
		}
		
		int[] samples = IntStream.range(0, this.sampleSize)
				.map(i -> this.sample(vectors.size()))
				.toArray();
		
		return this.actualMedianAndVar(new AbstractList<double[]>(){

			@Override
			public double[] get(int index) {
				return vectors.get(samples[index]);
			}

			@Override
			public int size() {
				return samples.length;
			}
			
		});
	}
	
	/**
	 * Compute the actual median and absolute variance
	 * @param vectors  Input vectors 
	 * @return  Statistics matrix
	 */
	protected Matrix actualMedianAndVar(List<double[]> vectors) {
		int dim = vectors.get(0).length;
		
		double[] sum = new double[dim];
		MinHeap[] heaps = new MinHeap[dim];
		
		for(int i = 0; i < heaps.length; i++){
			heaps[i] = MinHeap.ofMax(vectors.size());
		}
		
		int k = 0;
		for(double[] vector : vectors){
			for(int i = 0; i < vector.length; i++){
				sum[i] += vector[i];
				heaps[i].push(k++, vector[i]);
			}
		}
		
		double[] medians = Arrays.stream(heaps).mapToDouble(h -> h.peek().weight).toArray();
		double[] vars = new double[dim];
		
		for(double[] vector : vectors){
			for(int i = 0; i < vector.length; i++){
				vars[i] += Math.abs(vector[i] - sum[i] / vectors.size());
			}
		}
		return Matrices.wrap(new double[][]{medians, vars});
	}
	
	protected int sample(int n) {
		return (int) Math.floor(this.rand.getAsDouble() * n);
	}

	private DoubleSupplier rand;
	private int sampleSize;
	private ZSort zSort;
	
	private static final SpatialSort DEFAULT_SORT_2D = new FractalSort2D(0, 1, Fractal2D.HILBERT);
	
	private static final SpatialSort DEFAULT_SORT_3D = new HilbertSort3D(0, 1, 2);

	private static final DefaultSpatialSort INST = new DefaultSpatialSort(
		() -> ThreadLocalRandom.current().nextDouble(),
		DEFAULT_SAMPLING_SIZE,
		DEFAULT_R_SQUARE
	);
}
