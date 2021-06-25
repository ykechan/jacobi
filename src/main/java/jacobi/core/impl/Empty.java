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

import java.util.NoSuchElementException;

import jacobi.api.Matrix;
import jacobi.core.facade.FacadeProxy;

/**
 *
 * An empty matrix, i.e.&nbsp;a matrix with no element.
 * 
 * <p>This serves as a NULL object for matrix.</p>
 * 
 * @author Y.K. Chan
 */
public enum Empty implements Matrix {
	
	/**
	 * Default instance
	 */
	INST;
	
	/**
	 * Get singleton instance
	 * @return  Instance
	 */
	public static Empty getInstance() {
		return INST;
	}

	@Override
	public int getRowCount() {
		return 0;
	}

	@Override
	public int getColCount() {
		return 0;
	}

	@Override
	public double[] getRow(int index) {
		throw new NoSuchElementException();
	}

	@Override
	public Matrix setRow(int index, double[] values) {
		throw new NoSuchElementException();
	}

	@Override
	public Matrix swapRow(int i, int j) {
		throw new NoSuchElementException();
	}

	@Override
	public <T> T ext(Class<T> clazz) {
		return FacadeProxy.of(clazz, this);
	}

	@Override
	public Matrix copy() {
		return this;
	}
}
