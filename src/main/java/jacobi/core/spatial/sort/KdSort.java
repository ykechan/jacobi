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
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.IntStream;

import jacobi.core.util.IntStack;

/**
 * Implementation of spatial sorting in the order of a kd-tree.
 * 
 * <p>Fractal sort utilize all of the dimensions which in turn generates good result,
 * but it does not scale well with the number of dimensions (especially Hilbert sort).
 * Only sorting in a few dimensions may miss out a lot of information from the missing
 * dimensions. </p>
 * 
 * <p>K-d sort is provided for sorting data in high dimension. As in a kd-tree, the spatial 
 * data are divided in half by a single dimension, then in turn sorting the two halves
 * in other dimensions in the same manner.</p>
 * 
 * @author Y.K. Chan
 *
 */
public class KdSort implements SpatialSort {

	@Override
	public int[] sort(List<double[]> vectors) {
		int[] order = IntStream.range(0, vectors.size()).toArray();
		return order;
	}
	
	protected List<IntStack> groupBy(VectorList vectorList, int[] dims, double[] mean) {
		if(dims.length > DEFAULT_MAX_DIM) {
			throw new UnsupportedOperationException("Number of dimensions "
				+ dims.length + " in a batch is too large.");
		}
		
		IntStack[] groups = new IntStack[1 << dims.length];
		for(int i = vectorList.begin; i < vectorList.end; i++) {
			int k = vectorList.order[i];
			double[] vector = vectorList.vectors.get(k);
			int parity = 0;
			for(int j = 0; j < dims.length; j++) {
				int dim = dims[j];
				parity += vector[dim] < mean[dim] ? 0 : 1;
				parity *= 2;
			}
			
			if(groups[parity] == null){
				groups[parity] = new IntStack(4);
			}
			
			groups[parity].push(k);
		}
		return Arrays.asList(groups);
	}
	
	protected int[] chooseDims(List<double[]> vectors, double[] mean) {
		double[] var = this.varFn.apply(vectors, mean);
		double totalVar = 0.0;
		
		int max0 = -1, max1 = -1, max2 = -1;
		for(int i = 0; i < var.length; i++) {
			totalVar += var[i];
			
			if(max0 < 0 || var[max0] < var[i]){
				max2 = max1; max1 = max0; max0 = i;
				continue;
			}
			
			if(max1 < 0 || var[max1] < var[i]){
				max2 = max1; max1 = i;
				continue;
			}
			
			if(max2 < 0 || var[max2] < var[i]){
				max2 = i;
			}						
		}
		
		if(var[max0] + var[max1] + var[max2] > this.rSquare * totalVar){
			return new int[] {max0, max1, max2} ;
		}
		
		return new int[] {};
	}
	
	protected int[] chooseDims(double[] mean, double[] var) {
		
		return null;
	}
	
	private Function<List<double[]>, double[]> meanFn;
	private BiFunction<List<double[]>, double[], double[]> varFn;	
	private Function<int[], SpatialSort> lowDim;
	private double rSquare;
	
	protected static class VectorList {
		
		public final List<double[]> vectors;
		
		public final int begin, end;
		
		public final int[] order;

		public VectorList(List<double[]> vectors, int begin, int end, int[] order) {
			this.vectors = vectors;
			this.begin = begin;
			this.end = end;
			this.order = order;
		}
		
		public List<double[]> toList() {
			return new AbstractList<double[]>() {

				@Override
				public double[] get(int index) {
					return vectors.get(order[index + begin]);
				}

				@Override
				public int size() {
					return end - begin;
				}
				
			};
		}		
	}
	
	protected static final int DEFAULT_MAX_DIM = 8;
	
}
