/*
 * The MIT License
 *
 * Copyright 2019 Y.K. Chan
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
package jacobi.core.stats;

import java.util.Arrays;

import jacobi.api.Matrix;
import jacobi.core.impl.ColumnVector;
import jacobi.core.stats.select.AdaptiveSelect;
import jacobi.core.stats.select.Select;

/**
 * Computation of order statistics.
 * 
 * <p>The order statistics of order k is the k-th smallest number in a distribution. To align
 * with the culture in computer science, k ranges from 0 to n - 1 with n numbers.</p>
 * 
 * @author Y.K. Chan
 *
 */
public class OrderStats {
	
	/**
	 * Limit to use heap select
	 */
	public static final int DEFAULT_USE_HEAP = 32;
	
	/**
	 * Limit to use median-of-medians
	 */
	public static final double DEFAULT_CONST_FACTOR = 2.0;
	
	/**
	 * Constructor.
	 */
	public OrderStats() {
		this(AdaptiveSelect.of(DEFAULT_USE_HEAP, DEFAULT_CONST_FACTOR));
	}

	/**
	 * Constructor.
	 * @param selector  Selector implementation
	 */
	protected OrderStats(Select selector) {
		this(selector, new RowReduce.Min(), new RowReduce.Max());
	}

	/**
	 * Constructor.
	 * @param selector  Selector implementation
	 * @param min  Implementation for selecting minimum
	 * @param max  Implementation for selecting maximum
	 */
	protected OrderStats(Select selector, RowReduce min, RowReduce max) {
		this.selector = selector;
		this.min = min;
		this.max = max;
	}

	/**
	 * Compute the order statistics for each columns of an input matrix
	 * @param input  Input matrix
	 * @param order  Order ranging from 0 to n - 1
	 * @return  Order statistics for each columns
	 */
	public double[] compute(Matrix input, int order) {
		if(order < 0 || order >= input.getRowCount()) {
			throw new IllegalArgumentException(
				"Invalid order " + order + " with " + input.getRowCount() + " rows."
			);
		}
		
		if(order == 0) {
			return this.min.compute(input);
		}
		
		if(order == input.getRowCount() - 1) {
			return this.max.compute(input);
		}
		
		if(input instanceof ColumnVector) {
			double[] temp = Arrays.copyOf(((ColumnVector) input).getVector(), input.getRowCount());
			int target = this.selector.select(temp, 0, temp.length, order);
			return new double[] {temp[target]};
		}
		
		double[][] buffer = this.createBuffer(input);
		double[] ans = new double[input.getColCount()];
		for(int j = 0; j < input.getColCount(); j += buffer.length){
			int span = Math.min(buffer.length, input.getColCount() - j);
			
			for(int i = 0; i < span; i++) {
				double[] buf = buffer[i];				
				ans[j + i] = this.select(buf, order);
			}
		}
		return ans;
	}
	
	/**
	 * Select the value of target order in array buffer 
	 * @param buf  Array buffer
	 * @param target  Target order
	 * @return  Value in target order
	 */
	protected double select(double[] buf, int target) {
		int index = this.selector.select(buf, 0, buf.length, target);
		return buf[index];
	}
	
	/**
	 * Get a range of columns
	 * @param input  Input matrix
	 * @param begin  Begin index of columns
	 * @param cols  Array buffer for column values
	 * @return  Column values in array buffer
	 */
	protected double[][] getColumns(Matrix input, int begin, double[][] cols) {
		int span = Math.min(cols.length, input.getColCount() - begin);
		
		for(int i = 0; i < input.getRowCount(); i++) {
			double[] row = input.getRow(i);
			for(int j = 0; j < span; j++) {
				cols[j][i] = row[j];
			}
		}
		return cols;
	}
	
	/**
	 * Create buffer for selection
	 * @param input  Input matrix
	 * @return  Array buffer for selection
	 */
	protected double[][] createBuffer(Matrix input) {
		int m = input.getRowCount();
		int n = input.getColCount();
		return new double[Math.min(DEFAULT_WORD_SIZE, n)][m];
	}

	private Select selector;
	private RowReduce min, max;
	
	protected static final int DEFAULT_WORD_SIZE = 8;
}
