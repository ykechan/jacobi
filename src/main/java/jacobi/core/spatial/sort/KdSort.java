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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;

import jacobi.api.Matrix;
import jacobi.core.impl.ImmutableMatrix;
import jacobi.core.util.IntStack;
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
	
	/**
	 * Constructor
	 * @param baseFactory
	 * @param statsFn
	 * @param rSquare
	 */
	public KdSort(Function<int[], SpatialSort> baseFactory, 
			UnaryOperator<Matrix> statsFn, double rSquare) {
		this.baseFactory = baseFactory;
		this.statsFn = statsFn;
		this.rSquare = rSquare;
	}
	
	@Override
	public int[] sort(List<double[]> vectors) {
		int[] seq = IntStream.range(0, vectors.size()).toArray();
		
		Deque<Div> stack = new ArrayDeque<>();
		stack.push(new Div(seq, 0, seq.length));
		
		while(!stack.isEmpty()) {
			Div div = stack.pop();
			this.sortDiv(vectors, div)
				.stream()
				.filter(v -> v != null && v.end - v.begin > 1)
				.forEach(stack::push);
		}
		
		return seq;
	}
	
	protected List<Div> sortDiv(List<double[]> vectors, Div div) {
		Matrix stats = this.statsFn.apply(this.toMatrix(vectors, div));
		double[] mean = stats.getRow(0);
		double[] var = stats.getRow(1);
		
		int[] dims = this.selectDims(var, 8);
		
		SpatialSort ssort = this.baseFactory.apply(dims);
		if(ssort != null){
			int[] order = ssort.sort(this.subList(vectors, div));
			for(int i = 0; i < order.length; i++) {
				int k = order[i];
				order[i] = div.seq[div.begin + k];
			}
			
			System.arraycopy(order, 0, div.seq, div.begin, order.length);
			return Collections.emptyList();
		}
		
		List<IntStack> groups = this.sortDiv(vectors, div, mean, dims);
		
		List<Div> divList = new ArrayList<>();
		
		int begin = div.begin;
		for(IntStack group : groups){
			if(group == null){
				continue;
			}
			
			if(group.size() >= div.end - div.begin) {
				return Collections.emptyList();
			}
			
			group.toArray(div.seq, begin);
			begin += group.size();
		}
		
		if(begin != div.end){
			throw new IllegalStateException();
		}
		return divList;
	}
	
	protected List<IntStack> sortDiv(List<double[]> vectors, Div div, double[] mean, int[] dims) {		
		
		IntStack[] groups = new IntStack[1 << dims.length];
		for(int i = div.begin; i < div.end; i++){
			int k = div.seq[i];
			double[] vector = vectors.get(k);
			
			int parity = 0;
			for(int d : dims){
				parity *= 2;
				parity += vector[d] < mean[d] ? 0 : 1;
			}
			
			if(groups[parity] == null){
				groups[parity] = new IntStack(4);
			}
			
			groups[parity].push(k);
		}
		return Arrays.asList(groups);
	}
	
	protected int[] selectDims(double[] vars, int limit){
		Queue<Weighted<Integer>> heap = new PriorityQueue<>(limit);
		double total = 0.0;
		
		for(int i = 0; i < vars.length; i++){
			total += vars[i];
			if(heap.size() >= limit && vars[i] < heap.peek().weight){
				continue;
			}
						
			heap.add(new Weighted<>(i, vars[i]));
			while(heap.size() > limit){
				heap.remove();
			}
		}		
		
		int[] dims = new int[heap.size()];
		int k = dims.length;
		while(!heap.isEmpty()){
			dims[--k] = heap.remove().item;
		}
		
		double minCov = this.rSquare * total;
		double cov = 0.0;
		for(int i = 0; i < dims.length; i++) {
			if(cov > minCov){
				return Arrays.copyOfRange(dims, 0, i);
			}
			
			cov += vars[dims[i]];
		}
		return dims;
	}
	
	protected Matrix toMatrix(List<double[]> vectors, Div div) {
		int dim = vectors.get(0).length;
		return new ImmutableMatrix() {

			@Override
			public int getRowCount() {
				return div.end - div.begin;
			}

			@Override
			public int getColCount() {
				return dim;
			}

			@Override
			public double[] getRow(int index) {
				return vectors.get(index);
			}
			
		};
	}
	
	protected List<double[]> subList(List<double[]> list, Div div) {
		return new AbstractList<double[]>() {

			@Override
			public double[] get(int index) {
				return list.get(index);
			}

			@Override
			public int size() {
				return div.end - div.begin;
			}
			
		};
	}
		
	private Function<int[], SpatialSort> baseFactory;
	private UnaryOperator<Matrix> statsFn;
	private double rSquare;	
	
	protected static class Div {
		
		public final int[] seq;
		
		public final int begin, end;

		public Div(int[] seq, int begin, int end) {
			this.seq = seq;
			this.begin = begin;
			this.end = end;
		}
		
	}
	
}
