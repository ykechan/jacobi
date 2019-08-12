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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.DoubleToIntFunction;
import java.util.stream.Collectors;

import jacobi.api.Matrix;
import jacobi.api.classifier.cart.Column;

/**
 * Implementation of a data table in CART model.
 * 
 * @author Y.K. Chan
 * @param <T>  Type of outcome
 */
public class DataMatrix<T> implements DataTable<T> {	
	
	public static <T> DataMatrix<T> of(Matrix matrix, List<?> colDefs, List<T> outcomes) {
		return null;
	}
	
	public static <T> DataMatrix<T> of(Matrix matrix, List<Column<?>> colDefs, Column<T> outcomeCol) {
		// ...
		
		List<Column<?>> cols = colDefs.stream()
			.filter(c -> !c.equals(outcomeCol))
			.collect(Collectors.toList());
		
		TypedMatrix<T> typedMat = new TypedMatrix<>(matrix, cols, outcomeCol);
		
		double[] weights = new double[matrix.getRowCount()];
		Arrays.fill(weights, 1.0);
		
		return new DataMatrix<>(typedMat, 
			extractNominals(typedMat), 
			weights
		);
	}

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
			throw new IllegalArgumentException("Conflicting column type");
		}
		
		int[] outcomes = this.nomData.get(this.getOutcomeColumn().getIndex());		
		return this.instancesOf(features, outcomes, this.weights);
	}		
	
	protected List<Instance> instancesOf(int[] features, int[] outcomes, double[] weights) {
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
	
	protected static Column<?> detectDef(int columnIndex, Object obj) {
		if(obj == Double.class
		
		|| obj == Float.class) {
			
		}
		return null;
	}
	
	protected static List<int[]> extractNominals(TypedMatrix<?> typedMat) {
		Matrix matrix = typedMat.matrix;
		
		int[][] nomList = new int[matrix.getColCount()][];
		
		List<Column<?>> nomCols = typedMat.featureColumns
				.stream()
				.filter(c -> !c.isNumeric())
				.collect(Collectors.toCollection(ArrayList::new));
		nomCols.add(typedMat.outcomeColumn);
		
		for(Column<?> col : nomCols) {
			nomList[col.getIndex()] = extractColumn(matrix, col.getIndex(), col.getMapping());
		}
		
		return Arrays.asList(nomList);
	}
	
	protected static int[] extractColumn(Matrix matrix, int colIdx, DoubleToIntFunction mapper) {
		int[] nom = new int[matrix.getRowCount()];
		for(int i = 0; i < nom.length; i++) {
			double[] row = matrix.getRow(i);
			nom[i] = mapper.applyAsInt(row[colIdx]);
		}
		return nom;
	}
	
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
