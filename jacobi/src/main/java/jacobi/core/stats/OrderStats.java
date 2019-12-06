package jacobi.core.stats;

import jacobi.api.Matrix;
import jacobi.core.stats.select.AdaptiveSelect;
import jacobi.core.stats.select.Select;

public class OrderStats {
	
	public static final int DEFAULT_USE_HEAP = 32;
	
	public static final double DEFAULT_CONST_FACTOR = 2.5;
	
	public OrderStats() {
		this(AdaptiveSelect.of(DEFAULT_USE_HEAP, DEFAULT_CONST_FACTOR));
	}

	public OrderStats(Select selector) {
		this(selector, new RowReduce.Min(), new RowReduce.Max());
	}

	protected OrderStats(Select selector, RowReduce min, RowReduce max) {
		this.selector = selector;
		this.min = min;
		this.max = max;
	}

	public double[] compute(Matrix input, int order) {
		if(order == 0) {
			return this.min.compute(input);
		}
		
		if(order == input.getRowCount() - 1) {
			return this.max.compute(input);
		}
		
		
		return null;
	}
	
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

	private Select selector;
	private RowReduce min, max;
}
