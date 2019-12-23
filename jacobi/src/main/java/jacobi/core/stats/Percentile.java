package jacobi.core.stats;

import jacobi.api.Matrix;
import jacobi.core.prop.Transpose;
import jacobi.core.stats.select.DualFixedPointSelect;
import jacobi.core.stats.select.Select;
import jacobi.core.util.Throw;

public class Percentile {
	
	public Percentile() {
		this(
			new Transpose(), 
			DualFixedPointSelect.getInstance(), 
			new RowReduce.Min(), 
			new RowReduce.Max()
		);
	}
	
	protected Percentile(Transpose transpose, Select selector, RowReduce min, RowReduce max) {
		this.transpose = transpose;
		this.selector = selector;
		this.min = min;
		this.max = max;
	}

	public double[] median(Matrix input) {
		Throw.when().isNull(() -> input, () -> "No input matrix.");
		
		int k = (input.getRowCount() / 2) - (input.getRowCount() + 1) % 2;
		
		return this.transpose.compute(input, r -> {
				selector.select(r, 0, r.length, k);
				return r.length % 2 == 0 
					? (r[k] + r[k + 1]) / 2
					: r[k];
			})
			.stream().mapToDouble(Double::doubleValue).toArray();
	}
	
	public double[] compute(Matrix input, int k) {
		if(k < 0 || k > 100) {
			throw new IllegalArgumentException(k + "-th percentile doesn't exist.");
		}
		
		if(k == 0 || k == 100){
			return (k == 0 ? this.min : this.max).compute(input);
		}
		
		if(k == 50) {
			return this.median(input);
		}
		
		if((input.getRowCount() * k) % 100 == 0){
			int target = (input.getRowCount() * k) / 100;
			return this.transpose.compute(input, r -> {
				selector.select(r, 0, r.length, target);
				return r[target];
			}).stream().mapToDouble(Double::doubleValue).toArray();
		}
		
		// ...
		return null;
	}

	private Transpose transpose;
	private Select selector;
	private RowReduce min, max;
}
