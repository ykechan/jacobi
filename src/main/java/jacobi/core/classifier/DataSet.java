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
package jacobi.core.classifier;

import java.util.List;

import jacobi.api.Matrix;
import jacobi.api.classifier.Column;
import jacobi.api.classifier.DataTable;
import jacobi.api.classifier.Instance;
import jacobi.core.classifier.cart.Sequence;
import jacobi.core.impl.ImmutableMatrix;

/**
 * This class is a subset of a full DataTable. This class may represents a subset of rows and 
 * a subset of columns.
 * 
 * @author Y.K. Chan 
 * @param <T>  Type of outcome
 */
public class DataSet<T> implements DataTable<T> {
	
	/**
	 * Constructor.
	 * @param data  Full data table
	 * @param columns  Subset of columns in the data table
	 * @param seq  Subset of rows in the data table
	 */
	public DataSet(DataTable<T> data, List<Column<?>> columns, Sequence seq) {
		this.data = data;
		this.columns = columns;
		this.seq = seq;
	}

	@Override
	public List<Column<?>> getColumns() {
		return this.columns;
	}

	@Override
	public Column<T> getOutcomeColumn() {
		return this.data.getOutcomeColumn();
	}

	@Override
	public Matrix getMatrix() {
		Matrix matrix = this.data.getMatrix();
		return new ImmutableMatrix() {

			@Override
			public int getRowCount() {
				return seq.length();
			}

			@Override
			public int getColCount() {
				return matrix.getColCount();
			}

			@Override
			public double[] getRow(int index) {
				return matrix.getRow(seq.indexAt(index));
			}
			
		};
	}

	@Override
	public List<Instance> getInstances(Column<?> column) {
		return this.seq.apply(this.data.getInstances(column));
	}	

	private DataTable<T> data;
	private List<Column<?>> columns;
	private Sequence seq;	
}
