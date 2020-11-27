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
import java.util.function.Function;
import java.util.stream.IntStream;

import jacobi.api.Matrix;
import jacobi.core.util.MinHeap;
import jacobi.core.util.Throw;
import jacobi.core.util.Weighted;

/**
 * Implementation of spatial sort in Z-Curve order.
 * 
 * <p>Z-curve is a space filing curve that can easily generalized to apply in higher dimension. In
 * 2-D z-curve sub-divide the space into 4 quadrants, and visit them in Z order as show below:
 * 
 * <pre>
 * +-----+-----+
 * |  0  |  1  |
 * +-----+-----+
 * |  2  |  3  |
 * +-----+-----+
 * </pre>
 * 
 * This is equivalent to sorting them by (&lt; middle in y, &lt; middle in x ), (&lt; middle in y, &gt; middle in x ),
 * (&gt; middle in y, &lt; middle in x ), (&gt; middle in y, &gt; middle in x ), and then further sub-divide the 
 * division until only one elements remains in the division.
 * </p>
 * 
 * <p>In higher dimension it works similarly, with &lt; and &gt; in all dimensions. Thus this creating a coding of
 * parity, i.e. this &lt; and &gt; in each dimension, and sort them further if the code is the same.</p>
 * 
 * <p>Unlike the Hilbert curve, the Z-curve is susceptible to bring far way points together when the dimension change,
 * the it may sweep through the whole span in that dimension. Thus large dimensions should be sorted first to minimize
 * the effect of this behaviour.</p>
 * 
 * <p>In ultra high dimension, e.g. &gt; 32, the integer parity overflows and dimension sorted in lower priority
 * unlikely provide much effect since the number of data won't be enough to place one in each division. Thus this
 * implementation divide the data by n largest dimensions first, then further sub-divide the divisions.</p>
 * 
 * <p>When only a handful of dimensions are large, this implementation falls back to sorting with these
 * dimensions only.</p>
 *
 * @author Y.K. Chan
 *
 */
public class ZSort implements SpatialSort {
	
	/**
	 * Default maximum number of dimensions to select in a round
	 */
	public static final int DEFAULT_MAX_DIM = 8;

	/**
	 * Constructor.
	 * @param statsFn  Statistics function that computes [mean, variance]
	 * @param sortingFactory  Sorting function factory given the indices of dimensions
	 * @param rSquare  Ratio of variance that is considered significant
	 */
	public ZSort(Function<List<double[]>, Matrix> statsFn, 
			Function<int[], SpatialSort> sortingFactory, 
			double rSquare) {
		this.statsFn = statsFn;
		this.sortingFactory = sortingFactory;
		this.rSquare = rSquare;
		this.maxDim = DEFAULT_MAX_DIM;
	}
	
	public void setMaxDim(int maxDim) {
		Throw.when().isTrue(() -> maxDim < 2, () -> "Too few dimensions");
		this.maxDim = maxDim;
	}

	@Override
	public int[] sort(List<double[]> vectors) {
		Context context = new Context(vectors, 
			IntStream.range(0, vectors.size()).toArray(), 
			new int[vectors.size()]
		);
		
		Deque<int[]> stack = new ArrayDeque<>();
		stack.push(new int[]{0, vectors.size()});
		
		while(!stack.isEmpty()){
			int[] divs = stack.pop();
			for(int i = 1; i < divs.length; i++){
				int[] subDivs = this.sort(context, divs[i - 1], divs[i]);
				if(subDivs != null && subDivs.length > 0){
					stack.push(subDivs);
				}
			}
		}
		return context.seq;
	}
	
	/**
	 * Sort the vectors in the range
	 * @param context  Sorting context
	 * @param begin  Begin index of interest
	 * @param end  End index of interest
	 * @return  Sorted sequence
	 */
	protected int[] sort(Context context, int begin, int end) {
		if(end - begin < 2){
			return new int[0];
		}
		
		List<double[]> vectors = context.vectors;
		int[] seq = context.seq;
		int[] buf = context.buf;
		
		Matrix stats = this.statsFn.apply(this.subList(context, begin, end));
		
		double[] mean = stats.getRow(0);
		double[] vars = stats.getRow(1);
		
		int[] dims = this.selectDims(vars, this.maxDim);
		
		SpatialSort sortFn = this.sortingFactory.apply(dims);
		if(sortFn != null){
			this.sortBy(context, begin, end, sortFn);
			return new int[0];
		}
		
		int[] divLen = new int[(1 << dims.length) + 1];
		Arrays.fill(divLen, 0);
		
		int[] temp = new int[end - begin];
		for(int i = begin; i < end; i++){
			double[] vector = vectors.get(seq[i]);
			int parity = this.zParity(vector, mean, dims);
			temp[i - begin] = parity;
			divLen[parity]++;
		}
		
		divLen[0] += begin;
		for(int i = 1; i < divLen.length; i++){
			divLen[i] += divLen[i - 1];
		}
		
		for(int i = end - 1; i >= begin; i--){
			int parity = temp[i - begin];
			int at = --divLen[parity];
			
			buf[at] = seq[i];
		}
		
		System.arraycopy(buf, begin, seq, begin, end - begin);
		divLen[1 << dims.length] = end;
		return divLen;
	}
	
	/**
	 * Sort the data by sorting function
	 * @param context  Sorting context
	 * @param begin  Begin index of interest
	 * @param end  End index of interest
	 * @param sortFn  Sorting function
	 */
	protected void sortBy(Context context, int begin, int end, SpatialSort sortFn) {
		List<double[]> div = this.subList(context, begin, end);
		int[] order = sortFn.sort(div);
		
		int[] seq = context.seq;
		int[] buf = context.buf;
		
		for(int i = begin; i < end; i++){
			buf[i] = seq[order[i]];
		}
		
		System.arraycopy(buf, begin, seq, begin, end);
	}
	
	/**
	 * Compute the parity of a vector given the mean and indices of the dimensions
	 * @param vector  Input vector
	 * @param mean  Mean vector
	 * @param dims  Indices of the dimensions
	 * @return
	 */
	protected int zParity(double[] vector, double[] mean, int[] dims) {
		int parity = 0;
		for(int i = 0; i < dims.length; i++){
			int d = dims[i];
			parity *= 2;
			if(vector[d] > mean[d]){
				parity++;
			}
		}
		return parity;
	}
	
	/**
	 * Select the most significant dimensions to be sorted
	 * @param vars  Variance in each dimensions
	 * @param limit  Max. number of dimension to be selected
	 * @return  
	 */
	protected int[] selectDims(double[] vars, int limit) {
		MinHeap heap = MinHeap.ofMax(limit);
		
		double sum = 0.0;
		for(int i = 0; i < vars.length; i++){
			sum += vars[i];
			heap.push(i, vars[i]);
		}
		
		double minCov = this.rSquare * sum;	
		List<Weighted<Integer>> list = new ArrayList<>();
		while(!heap.isEmpty()){
			list.add(heap.pop());
		}
		Collections.reverse(list);
		
		int len = list.size();
		double cov = 0.0;
		for(int i = 0; i < list.size(); i++){
			cov += list.get(i).weight;
			if(cov > minCov){
				len = i + 1;
				break;
			}
		}
		
		return list.stream().limit(len).mapToInt(w -> w.item).toArray();
	}
	
	/**
	 * Wrap the vectors into a sub-list in order of the sequence in sorting context
	 * @param context  Sorting context
	 * @param begin  Begin index of sub-list
	 * @param end  End index of sub-list
	 * @return  Sub-list in order of the sequence in sorting context
	 */
	protected List<double[]> subList(Context context, int begin, int end) {
		List<double[]> list = context.vectors;
		int[] seq = context.seq;
		int len = end - begin;
		return new AbstractList<double[]>() {

			@Override
			public double[] get(int index) {
				return list.get(seq[begin + index]);
			}

			@Override
			public int size() {
				return len;
			}
			
		};
	}
	
	private Function<List<double[]>, Matrix> statsFn;
	private Function<int[], SpatialSort> sortingFactory;
	private double rSquare;
	private int maxDim;
	
	/**
	 * Sorting context
	 * @author Y.K. Chan
	 */
	protected static class Context {
		
		/**
		 * Data vectors
		 */
		public final List<double[]> vectors;
		
		/**
		 * Sorted sequence and buffer array
		 */
		public final int[] seq, buf;

		/**
		 * Constructor
		 * @param vectors  Data vectors
		 * @param seq  Sorted sequence
		 * @param buf  Buffer array
		 */
		public Context(List<double[]> vectors, int[] seq, int[] buf) {
			this.vectors = vectors;
			this.seq = seq;
			this.buf = buf;
		}
		
	}
	
}
