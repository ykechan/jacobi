package jacobi.core.classifier;

import java.util.List;

import jacobi.api.Matrix;
import jacobi.api.classifier.Column;
import jacobi.api.classifier.DefinedSupervised;
import jacobi.core.classifier.cart.data.DataTable;
import jacobi.core.util.Throw;

public class DefinedSupervisedFactory {
	
	public <T> DefinedSupervised<T> create(Matrix data, 
			List<Column<?>> features, 
			List<T> outcomes) {
		
		Throw.when()
			.isNull(() -> data, () -> "Missing input data.")
			.isNull(() -> features, () -> "Missing features.")
			.isNull(() -> outcomes, () -> "Missing outcomes.")
			.isTrue(() -> data.getRowCount() == 0, () -> "No training instances")
			.isTrue(
				() -> data.getRowCount() != outcomes.size(), 
				() -> "Number of instances (" + data.getRowCount() + ") and outcomes (" 
						+ outcomes.size() +  ") mismatch"
			);
		
		for(Column<?> col : features) {
			if(col.getIndex() < 0 || col.getIndex() >= data.getColCount()) {
				throw new IllegalArgumentException(
					"Invalid column index #" + col.getIndex() + " found."
				);
			}
		}
		return null;
	}
	
	protected <T> DataTable<T> createDataTable() {
		return null;
	}

}
