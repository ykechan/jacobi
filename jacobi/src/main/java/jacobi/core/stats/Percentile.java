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

import java.util.function.Function;

import jacobi.api.Matrix;
import jacobi.core.prop.Transpose;
import jacobi.core.stats.select.DualFixedPointSelect;
import jacobi.core.stats.select.Select;
import jacobi.core.util.Throw;

/**
 * Implementation of computing percentiles.
 * 
 * @author Y.K. Chan
 *
 */
public class Percentile {
	
	/**
	 * Constructor.
	 */
	public Percentile() {
		this(
			new Transpose(), 
			DualFixedPointSelect.getInstance(), 
			new RowReduce.Min(), 
			new RowReduce.Max()
		);
	}
	
	/**
	 * Constructor.
	 * @param transpose  Matrix transpose operation
	 * @param selector  Selector of order statistics
	 * @param min  Select minimum 
	 * @param max  Select maximum
	 */
	protected Percentile(Transpose transpose, Select selector, RowReduce min, RowReduce max) {
		this.transpose = transpose;
		this.selector = selector;
		this.min = min;
		this.max = max;
	}

	/**
	 * Compute the median of each columns, a.k.a. the 50th percentile.
	 * @param input  Input matrix
	 * @return  Medians of every column
	 */
	public double[] median(Matrix input) {
		Throw.when().isNull(() -> input, () -> "No input matrix.");
		
		int k = (input.getRowCount() / 2) - (input.getRowCount() + 1) % 2;
		
		return this.ofColumns(input, r -> {
			selector.select(r, 0, r.length, k);
			return r.length % 2 == 0 
				? (r[k] + r[k + 1]) / 2
				: r[k];
		});
	}
	
	/**
	 * Compute the k-th percentile of each columns
	 * @param input  Input matrix
	 * @param k  Percentile
	 * @return  k-th percentile of every column
	 */
	public double[] compute(Matrix input, int k) {
		if(k < 0 || k > 100){
			throw new IllegalArgumentException(k + "-th percentile doesn't exist.");
		}
		
		if(k == 0 || k == 100){
			return (k == 0 ? this.min : this.max).compute(input);
		}
		
		if(k == 50) {
			return this.median(input);
		}
		
		return this.computeNonTrivial(input, k);
	}
	
	/**
	 * Compute the k-th percentile of each columns when k is valid and not 0, 50 or 100.
	 * @param input  Input matrix
	 * @param k  Percentile
	 * @return  k-th percentile of every column
	 */
	protected double[] computeNonTrivial(Matrix input, int k) {
		long p = (input.getRowCount() - 1) * k;
		if(p % 100 == 0){
			int target = (int) (p / 100);
			return this.ofColumns(input, r -> {
				this.selector.select(r, 0, r.length, target);
				return r[target];
			});
		}
		
		double x = p / 100.0;
		int target = (int) Math.floor(x);
		double xmod1 = x - target;
		
		return this.ofColumns(input, r -> {
			this.selector.select(r, 0, r.length, target);
			return r[target] 
				+ (r[target + (target + 1 < r.length ? 1 : 0)] - r[target]) * xmod1;
		});
	}
	
	/**
	 * Compute the value for each columns of an input matrix
	 * @param input  Input matrix
	 * @param func  Function to derive a value
	 * @return  Values for every column
	 */
	protected double[] ofColumns(Matrix input, Function<double[], Double> func) {
		return this.transpose.compute(input, func)
			.stream().mapToDouble(Double::doubleValue).toArray();
	}

	private Transpose transpose;
	private Select selector;
	private RowReduce min, max;
}
