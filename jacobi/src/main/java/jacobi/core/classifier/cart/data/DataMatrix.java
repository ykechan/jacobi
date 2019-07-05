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
package jacobi.core.classifier.cart.data;

import java.util.AbstractList;
import java.util.List;

import jacobi.api.Matrix;

public class DataMatrix<T> implements DataTable<T> {

	protected DataMatrix(TypedMatrix<T> numData, List<int[]> nomData, double[] weights) {
		this.numData = numData;
		this.nomData = nomData;
		this.weights = weights;
	}

	@Override
	public List<Column<?>> getColumns() {
		return this.numData.featureColumns;
	}

	@Override
	public Column<T> getOutcomeColumn() {
		return this.numData.outcomeColumn;
	}

	@Override
	public Matrix getMatrix() {
		return this.numData.matrix;
	}

	@Override
	public List<Instance> getInstances(Column<?> column) {
		int[] features = this.nomData.get(column.getIndex());
		
		if((features == null) != column.isNumeric()) {
			throw new IllegalArgumentException();
		}
		
		int[] outcomes = this.nomData.get(this.getOutcomeColumn().getIndex());		
		return this.instancesOf(features, outcomes, this.weights);
	}
	
	protected List<Instance> instancesOf(int[] features, int[] outcomes, double[] weights) {
		if(features == null){
			
		}
		return features == null
			? new AbstractList<Instance>() {
	
				@Override
				public Instance get(int index) {
					return new Instance(index, outcomes[index], weights[index]);
				}
	
				@Override
				public int size() {
					return outcomes.length;
				}
				
			}
			: new AbstractList<Instance>() {
	
				@Override
				public Instance get(int index) {
					return new Instance(features[index], outcomes[index], weights[index]);
				}
	
				@Override
				public int size() {
					return outcomes.length;
				}
				
			};
	}

	private TypedMatrix<T> numData;
	private List<int[]> nomData;
	private double[] weights;
	
	protected static class TypedMatrix<T> {
		
		public final Matrix matrix;
		
		public final List<Column<?>> featureColumns;
		
		public final Column<T> outcomeColumn;

		public TypedMatrix(Matrix matrix, List<Column<?>> featureColumns, Column<T> outcomeColumn) {
			this.matrix = matrix;
			this.featureColumns = featureColumns;
			this.outcomeColumn = outcomeColumn;
		}
		
	}
}
