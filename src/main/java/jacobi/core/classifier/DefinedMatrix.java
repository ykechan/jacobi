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

import java.util.AbstractList;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

import jacobi.api.Matrix;
import jacobi.api.classifier.Column;
import jacobi.api.classifier.DataTable;
import jacobi.api.classifier.Instance;
import jacobi.core.util.Throw;

/**
 * Implementation of a data table by defining columns on a backing matrix.
 * 
 * @author Y.K. Chan
 * @param <T>  Type of outcome
 */
public class DefinedMatrix<T> implements DataTable<T>, Reweightable<T> {
	
	/**
	 * Get the factory function on a given matrix and associated outcomes.
	 * @param matrix  Input matrix
	 * @param outcomes  Associated outcomes
	 * @return  A factory function to create a DefinedMatrix given a list of column definitions
	 */
	public static <T> Function<List<Column<?>>, DefinedMatrix<T>> of(Matrix matrix, List<T> outcomes) {
		Throw.when()
			.isNull(() -> matrix, () -> "No input matrix")
			.isNull(() -> outcomes, () -> "No outcome values")
			.isTrue(outcomes::isEmpty, () -> "No instances.")
			.isTrue(
				() -> matrix.getRowCount() != outcomes.size(), 
				() -> "Unable to associate " 
					+ matrix.getRowCount() + " instances with "
					+ outcomes.size() + " outcomes."
			);
		
		return DefinedMatrix.of(matrix, extractNominals(matrix.getColCount(), outcomes));
	}
	
	/**
	 * Get the factory function on a given matrix and outcome column definition.
	 * @param matrix  Input matrix
	 * @param outcomeDef  Outcomes column definition
	 * @return  A factory function to create a DefinedMatrix given a list of column definitions
	 */
	public static <T> Function<List<Column<?>>, DefinedMatrix<T>> of(Matrix matrix, Column<T> outcomeDef) {
		Throw.when()
			.isNull(() -> matrix, () -> "No input matrix")
			.isNull(() -> outcomeDef, () -> "No outcome definition")
			.isTrue(() -> matrix.getRowCount() == 0, () -> "No instances.");
		return DefinedMatrix.of(matrix, (Nominal<T>) extractNominals(matrix, outcomeDef));
	}
	
	/**
	 * Get the factory function on a given matrix and outcome column definition and outcome values.
	 * @param matrix  Input matrix
	 * @param outNom  Outcome column definition and outcome values
	 * @return  A factory function to create a DefinedMatrix given a list of column definitions
	 */
	protected static <T> Function<List<Column<?>>, DefinedMatrix<T>> of(Matrix matrix, Nominal<T> outNom) {
		return ls -> {
			int[][] nomVals = new int[Math.max(matrix.getRowCount(), 1 + outNom.def.getIndex())][];
			
			for(Column<?> col : ls){
				Nominal<?> nom = extractNominals(matrix, col);
				nomVals[col.getIndex()] = nom.values;
			}
			
			double[] weights = new double[matrix.getRowCount()];
			Arrays.fill(weights, 1.0);
			
			nomVals[outNom.def.getIndex()] = outNom.values;
			return new DefinedMatrix<>(
				new ColumnDefs<>(ls.stream()
					.filter(c -> c.getIndex() != outNom.def.getIndex())
					.collect(Collectors.toList()), outNom.def),
				new ColumnData(matrix, Arrays.asList(nomVals)),
				weights
			);
			
		};
	}
	
	/**
	 * Constructor.
	 * @param colDefs  Column definition
	 * @param colData  Column data
	 * @param weights  Associated weight with each instances
	 */
	protected DefinedMatrix(ColumnDefs<T> colDefs, ColumnData colData, double[] weights) {
		this.colDefs = colDefs;
		this.colData = colData;
		this.weights = weights;
	}

	@Override
	public List<Column<?>> getColumns() {
		return this.colDefs.features;
	}

	@Override
	public Column<T> getOutcomeColumn() {
		return this.colDefs.outCol;
	}

	@Override
	public Matrix getMatrix() {
		return this.colData.numData;
	}

	@Override
	public List<Instance> getInstances(Column<?> column) {
		int[] nomVals = this.colData.nomData.get(column.getIndex());
		
		if((nomVals == null) != column.isNumeric()) {
			throw new IllegalArgumentException("Column type mismatch.");
		}
		
		int[] outVals = this.colData.nomData.get(this.getOutcomeColumn().getIndex());
		return nomVals == null
			? this.getNumerics(outVals, weights)
			: this.getNominals(nomVals, outVals, weights);
	}
	
	@Override
	public DataTable<T> reweight(double[] weights) {
		return new DefinedMatrix<>(this.colDefs, this.colData, weights);
	}
	
	/**
	 * Get the list of instances when pairing with a numeric column. The feature values of the instances
	 * are the indices of the instances.
	 * @param outVals  Outcome values
	 * @param weights  Weights of instances
	 * @return  List of instances
	 */
	protected List<Instance> getNumerics(int[] outVals, double[] weights) {
		return new AbstractList<Instance>() {

			@Override
			public Instance get(int index) {
				return new Instance(index, outVals[index], weights[index]);
			}

			@Override
			public int size() {
				return weights.length;
			}
			
		};
	}
	
	/**
	 * Get the list of instances when pairing with a nominal column.
	 * @param nomVals  Nominal values
	 * @param outVals  Outcome values
	 * @param weights  Weights of instances
	 * @return  List of instances
	 */
	protected List<Instance> getNominals(int[] nomVals, int[] outVals, double[] weights) {
		return new AbstractList<Instance>() {

			@Override
			public Instance get(int index) {
				return new Instance(nomVals[index], outVals[index], weights[index]);
			}

			@Override
			public int size() {
				return weights.length;
			}
			
		};
	}
	
	/**
	 * Extract nominal values from a column of an matrix by its definition.
	 * @param matrix  Input matrix
	 * @param colDef  Column definition
	 * @return  Column definition with nominal values
	 */
	protected static <T> Nominal<T> extractNominals(Matrix matrix, Column<T> colDef) {
		int index = colDef.getIndex();
		if(index < 0 || index >= matrix.getColCount()){
			throw new IllegalArgumentException("Invalid column index #" + index);
		}
		
		if(colDef.isNumeric()) {
			return new Nominal<>(colDef, null);
		}
		
		int[] nomVals = new int[matrix.getRowCount()];
		for(int i = 0; i < nomVals.length; i++){
			nomVals[i] = colDef.getMapping().applyAsInt(matrix.get(i, index));
		}
		return new Nominal<>(colDef, nomVals);
	}		
	
	/**
	 * Extract nominal values and detect its definition by the given items.
	 * @param dataIndex  Index of the items that will be placed in nominal data array. 
	 * @param items  Nominal items
	 * @return  Column definition with nominal values
	 * @throws  IllegalArgumentException if there are no items, 
	 * 			   or the items are doubles/floats, 
	 * 			   or unable to detect the type of the items.
	 */
	@SuppressWarnings("unchecked")
	protected static <T> Nominal<T> extractNominals(int dataIndex, List<T> items) {
		if(items == null || items.isEmpty()) {
			throw new IllegalArgumentException("No nominal data to extract.");
		}
		
		T item = items.get(0);
		
		if(item instanceof Double || item instanceof Float){
			throw new IllegalArgumentException("Unable to extract nominals from numeric items.");
		}
		
		if(item instanceof Boolean) {
			return (Nominal<T>) new Nominal<>(
				Column.signed(dataIndex),
				extractValues(Boolean.class, items, b -> b ? 1 : 0)
			);
		}
		
		if(item.getClass().isEnum()) {
			Map<Object, Integer> map = new IdentityHashMap<>();
			Object[] enums = item.getClass().getEnumConstants();
			for(int i = 0; i < enums.length; i++) {
				map.put(enums[i], i);
			}
			
			return (Nominal<T>) new Nominal<>(
				Column.of(dataIndex, item.getClass()),
				extractValues(item.getClass(), items, map::get)
			);
		}
		
		if(item instanceof Comparable){
			Object[] enums = items.stream().distinct().sorted().toArray();
			return (Nominal<T>) new Nominal<>(
				new Column<>(dataIndex, Arrays.asList(enums), v -> (int) v),
				items.stream().mapToInt(i -> Arrays.binarySearch(enums, i)).toArray()
			);
		}
		
		throw new IllegalArgumentException();
	}
	
	/**
	 * Extract nominal values of a list of items given it's class and encoding function
	 * @param clazz  Class of items
	 * @param items  List of items
	 * @param encoding  Encoding function
	 * @return  Nominal values
	 */
	@SuppressWarnings("unchecked")
	protected static <T> int[] extractValues(Class<T> clazz, List<?> items, ToIntFunction<T> encoding) {
		int[] values = new int[items.size()];
		for(int i = 0; i < values.length; i++) {
			Object item = items.get(i);
			
			if(!clazz.isInstance(item)){
				throw new IllegalArgumentException(
					"Found item " + item + " which is not of homogeneous type " + clazz.getName()
				);
			}
			
			values[i] = encoding.applyAsInt((T) item);
		}
		return values;
	}
	
	private ColumnDefs<T> colDefs;
	private ColumnData colData;
	private double[] weights;

	/**
	 * Data object for column definitions.
	 * 
	 * @author Y.K. Chan
	 * @param <T>  Type of outcome
	 */
	protected static class ColumnDefs<T> {
		
		/**
		 * Column definition for feature columns. The list does not necessarily covers all
		 * columns of the instance, and not necessarily in any particular order.
		 */
		public final List<Column<?>> features;
		
		/**
		 * Column definition for the outcome column. The outcome column does not necessarily
		 * be within the matrix. 
		 */
		public final Column<T> outCol;

		/**
		 * Constructor.
		 * @param features  List of definition of feature columns
		 * @param outCol  Definition of outcome column
		 */
		public ColumnDefs(List<Column<?>> features, Column<T> outCol) {
			this.features = features;
			this.outCol = outCol;
		}
		
	}
	
	/**
	 * Data object for column data. 
	 * 
	 * @author Y.K. Chan
	 *
	 */
	protected static class ColumnData {
		
		/**
		 * Data in numeric form, with each row as an instance of feature vector. Depending on
		 * definition, the row vector may include the outcome column.
		 */
		public final Matrix numData;
		
		/**
		 * Nominal column data. Each array is the data for the entire column. 
		 */
		public final List<int[]> nomData;

		public ColumnData(Matrix numData, List<int[]> nomData) {
			this.numData = numData;
			this.nomData = nomData;
		}
		
	}
	
	/**
	 * Data transfer object of a column definition and column values.
	 * 
	 * @author Y.K. Chan
	 * @param <T>  Type of column
	 */
	protected static class Nominal<T> {
		
		/**
		 * Column definition.
		 */
		public final Column<T> def;
		
		/**
		 * Column values.
		 */
		public final int[] values;

		/**
		 * Constructor.
		 * @param def  Column definition
		 * @param values  Column values
		 */
		public Nominal(Column<T> def, int[] values) {
			this.def = def;
			this.values = values;
		}
				
	}	
	
}
