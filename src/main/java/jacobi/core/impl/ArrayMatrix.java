/* 
 * The MIT License
 *
 * Copyright 2017 Y.K. Chan
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
package jacobi.core.impl;

import java.util.Arrays;

import jacobi.api.Matrix;
import jacobi.core.facade.FacadeProxy;
import jacobi.core.util.Throw;

/**
 * Implementation of a dense matrix by a 1-D array.
 * 
 * @author Y.K. Chan
 */
public class ArrayMatrix implements Matrix {
	
	/**
	 * Factory method for wrapping an array of elements into a matrix
	 * @param n  Number of columns
	 * @param elements  Array of matrix elements
	 * @return  New instance of an ArrayMatrix with elements as backing array
	 */
	public static Matrix wrap(int n, double... elements) {
		Throw.when()
			.isTrue(
				() -> elements == null || elements.length < 1, 
				() -> "No matrix elements"
			)
			.isTrue(() -> n < 1, () -> "Invalid number of column " + n)
			.isTrue(
				() -> elements.length % n > 0, 
				() -> "Unable to fit " + elements.length + " elements into columns of " + n 
			);
		return new ArrayMatrix(elements, elements.length / n, n);
	}
	
	/**
	 * Factory method
	 * @param m  Number of rows
	 * @param n  Number of columns
	 * @return  New instance of an ArrayMatrix
	 */
	public static Matrix of(int m, int n) {
		if(m <= 0 || n <= 0) {
			throw new IllegalArgumentException("Invalid dimension " + m + "x" + n);
		}
		
		long numElem = (long) m * (long) n;
		if(numElem > Integer.MAX_VALUE) {
			throw new UnsupportedOperationException("Unable to create a " + numElem + " long array.");
		}
		
		return new ArrayMatrix(new double[m * n], m, n);
	}
	
	/**
	 * Constructor
	 * @param array  Backing array
	 * @param m  Number of rows
	 * @param n  Number of columns
	 */
	protected ArrayMatrix(double[] array, int m, int n) {
		this.array = array;
		this.numRow = m;
		this.numCol = n;		
	}

	@Override
	public int getRowCount() {
		return this.numRow;
	}

	@Override
	public int getColCount() {
		return this.numCol;
	}

	@Override
	public double[] getRow(int index) {
		
		return Arrays.copyOfRange(this.array, 
			index * this.numCol, 
			index * this.numCol + this.numCol);
	}

	@Override
	public Matrix setRow(int index, double[] values) {
		if(index < 0){
			throw new ArrayIndexOutOfBoundsException();
		}
		
		if(values == null) {
			throw new IllegalArgumentException("No row values");
		}
		
		if(values.length != this.numCol) {
			throw new IllegalArgumentException("Dimension mismatch");
		}
		
		System.arraycopy(values, 0, this.array, index * this.numCol, this.numCol);
		return this;
	}

	@Override
	public Matrix swapRow(int i, int j) {
		double[] temp = this.getRow(i);
		System.arraycopy(this.array, j * this.numCol, this.array, i * this.numCol, this.numCol);
		System.arraycopy(temp, 0, this.array, j * this.numCol, this.numCol);
		return this;
	}

	@Override
	public <T> T ext(Class<T> clazz) {
		return FacadeProxy.of(clazz, this);
	}

	@Override
	public Matrix copy() {
		return new ArrayMatrix(
			Arrays.copyOf(this.array, this.array.length), 
			this.numRow, this.numCol
		);
	}

	private int numRow, numCol;
	private double[] array;
}
