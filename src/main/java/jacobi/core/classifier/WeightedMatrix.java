/*
 * The MIT License
 *
 * Copyright 2021 Y.K. Chan
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

import java.util.Arrays;
import java.util.List;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.api.classifier.Column;
import jacobi.api.classifier.DataTable;
import jacobi.api.classifier.Instance;
import jacobi.core.impl.ImmutableMatrix;

/**
 * A pure numerical matrix that each row is associated with a boolean value and a weight.
 * 
 * A row is associated with true if its weight is positive, and false if its weight is negative.
 * 
 * A weight with zero would cause the row to be ignored.
 * 
 * @author Y.K. Chan
 *
 */
public class WeightedMatrix extends ImmutableMatrix {
	
	/**
	 * Factory method
	 * @param dataTab  Input data table
	 * @return  Boolean matrix
	 */
	public static WeightedMatrix of(DataTable<Boolean> dataTab) {
		int[] cols = dataTab.getColumns().stream()
				.filter(c -> c.isNumeric())
				.mapToInt(c -> c.getIndex()).toArray();
		
		Column<Boolean> outCol = dataTab.getOutcomeColumn();
		double[] weights = new double[dataTab.size()];
		
		double[][] rows = new double[weights.length][];
		Matrix dataMat = dataTab.getMatrix();
		
		List<Instance> insts = dataTab.getInstances(outCol);
		int k = 0;
		for(int i = 0; i < insts.size(); i++){
			Instance inst = insts.get(i);
			if(inst.weight < 0.0){
				throw new UnsupportedOperationException("Support non-negative weights only");
			}
			
			if(inst.weight == 0){
				continue;
			}
			
			double[] row = dataMat.getRow(i);
			
			int j = k++;
			rows[j] = Arrays.stream(cols).mapToDouble(n -> row[n]).toArray();
			int sgn = outCol.valueOf(inst.outcome) ? 1 : -1;
			weights[j] = sgn * inst.weight;
		}
		return new WeightedMatrix(
			Matrices.wrap(k < rows.length ? Arrays.copyOfRange(rows, 0, k) : rows),
			k < weights.length ? Arrays.copyOfRange(weights, 0, k) : weights,
			cols
		);
	}
	
	/**
	 * Constructor.
	 * @param matrix  Input data matrix
	 * @param weights  Associated weights
	 * @param cols  Index of projected columns
	 */
	public WeightedMatrix(Matrix matrix, double[] weights, int[] cols) {
		this.matrix = matrix;
		this.weights = weights;
	}

	@Override
	public int getRowCount() {
		return this.matrix.getRowCount();
	}

	@Override
	public int getColCount() {
		return this.matrix.getColCount();
	}

	@Override
	public double[] getRow(int index) {
		return this.matrix.getRow(index);
	}
	
	/**
	 * Get the weight of a given row
	 * @param index  Index of the row
	 * @return  Weight of a given row
	 */
	public double getWeight(int index) {
		return this.weights[index];
	}
	
	/**
	 * Get the corresponding column index in the data table
	 * @param index  Index of matrix column
	 * @return  Corresponding column index in the data table
	 */
	public int columnIndex(int index) {
		return 0;
	}

	private Matrix matrix;
	private double[] weights;
	private int[] cols;
}
