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
package jacobi.core.spatial;

import java.util.function.IntBinaryOperator;

import jacobi.api.Matrix;

/**
 * Factory class for building a vantage-point tree.
 * 
 * @author Y.K. Chan
 *
 */
public class VPTreeFactory {
	
	
	
	protected double[] pairwiseDistances(Matrix matrix, int vp) {
		double[] u = matrix.getRow(vp);
		double[] dists = new double[matrix.getRowCount()];
		for(int i = 0; i < dists.length; i++){
			if(i == vp){
				continue;
			}
			
			double[] v = matrix.getRow(i);
			dists[i] = DirectQuery.sqDist(u, v);
		}
		return dists;
	}
	
	protected double groupBy(Context context, double pivot, int begin, int end) {
		int[] seq = context.index;
		double[] dists = context.buf;
		
		int i = begin;
		int j = end;
		
		while(i < j){
			int s = seq[i];
			if(dists[s] < pivot){
				i++;
				continue;
			}
			
			int tmp = seq[i]; 
			seq[i] = seq[--j]; 
			seq[j] = tmp;
		}
		
		return this.adjustPivot(context, begin, end, i);
	}
	
	protected double adjustPivot(Context context, int begin, int end, int pivot) {
		int skew = pivot - (begin + end) / 2;
		if(skew == 0){
			int s = context.index[pivot];
			return context.buf[s];
		}
		
		int[] seq = context.index;
		double[] dists = context.buf;
		
		if(pivot == begin){
			// is it degenerate?
		}
		return 0.0;
	}

	private IntBinaryOperator rand;
	
	protected static class Context {
		
		public int[] index;
		
		public double[] buf;
		
	}
}
