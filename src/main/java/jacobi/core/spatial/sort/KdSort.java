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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.IntStream;

import jacobi.core.util.IntStack;
import jacobi.core.util.Real;
import jacobi.core.util.Weighted;

/**
 * Implementation of spatial sorting in the order of a kd-tree.
 * 
 * <p>Fractal sort utilizes all of the dimensions which in turn generates good result,
 * but it does not scale well with the number of dimensions (especially Hilbert sort).
 * Only sorting in a few dimensions may miss out a lot of information from the missing
 * dimensions. </p>
 * 
 * <p>K-d sort is provided for sorting data in high dimension. As in a kd-tree, the spatial 
 * data are divided in half by a few dimensions, then in turn sorting the two halves
 * in other dimensions in the same manner.</p>
 * 
 * @author Y.K. Chan
 *
 */
public class KdSort implements SpatialSort {
	
	public KdSort(Function<List<double[]>, double[]> meanFn, 
			BiFunction<List<double[]>, double[], double[]> varFn) {
		this.meanFn = meanFn;
		this.varFn = varFn;
	}

	@Override
	public int[] sort(List<double[]> vectors) {
		
		return null;
	}
	
	protected List<Division> divide(List<double[]> vectors, Division div) {
		List<double[]> subList = div.of(vectors);
		
		double[] mean = this.meanFn.apply(subList);
		int[] dims = this.selectDims(subList, mean, 8);
		
		List<IntStack> groups = this.groupBy(vectors, div, mean, dims);
		List<Division> divs = new ArrayList<>(groups.size()); 
		
		int begin = div.begin;
		for(IntStack group : groups) {
			if(group == null) {
				continue;
			}
			
			group.toArray(div.seq, begin);
			
			if(group.size() > 1){
				divs.add(new Division(div.seq, begin, begin + group.size()));
			}
			
			if(group.size() >= div.end - div.begin){
				// all are degenerated
				return Collections.emptyList();
			}
			
			begin += group.size();		
		}
		
		if(begin != div.end) {
			throw new IllegalStateException("Division count not match. Expected "
				+ div.end + ", actual " + begin);
		}
		return divs;
	}
	
	protected List<IntStack> groupBy(List<double[]> vectors, Division div, double[] mean, int[] dims) {
		IntStack[] groups = new IntStack[1 << dims.length];
		for(int i = div.begin; i < div.end; i++) {
			int k = div.seq[i];
			double[] v = vectors.get(k);
			
			int parity = 0;
			for(int d : dims) {
				parity *= 2;				
				parity += v[d] < mean[d] ? 0 : 1;
			}
			
			if(groups[parity] == null) {
				groups[parity] = new IntStack(4);
			}
			
			groups[parity].push(k);
		}
		return Arrays.asList(groups);
	}
	
	protected int[] selectDims(List<double[]> vectors, double[] mean, int maxDim) {
		double[] vars = this.varFn.apply(vectors, mean);
		Queue<Weighted<Integer>> heap = new PriorityQueue<>();
		heap.add(new Weighted<>(0, vars[0]));
		
		for(int i = 1; i < vars.length; i++) {
			if(heap.size() >= maxDim && vars[i] < heap.peek().weight){
				continue;
			}
			
			heap.add(new Weighted<>(i, vars[i]));
			
			while(heap.size() > maxDim) {
				heap.remove();
			}			
		}
		
		int[] dims = new int[heap.size()];
		int k = dims.length;
		
		while(!heap.isEmpty()) {
			dims[--k] = heap.remove().item;
		}
		return dims;
	}
	
	private Function<List<double[]>, double[]> meanFn;
	private BiFunction<List<double[]>, double[], double[]> varFn;
	
	protected static class Division {
		
		public final int[] seq;
		
		public final int begin, end;

		public Division(int[] seq, int begin, int end) {
			this.seq = seq;
			this.begin = begin;
			this.end = end;
		}
		
		public <T> List<T> of(List<T> list) {
			return new AbstractList<T>() {

				@Override
				public T get(int index) {
					return list.get(begin + index);
				}

				@Override
				public int size() {
					return end - begin;
				}
				
			};
		}
		
	}
}
