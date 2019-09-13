package jacobi.core.classifier.ensemble;

import jacobi.api.classifier.DataTable;

public interface ReWeightable<T> {
	
	public DataTable<T> reweight(double[] weights);

}
