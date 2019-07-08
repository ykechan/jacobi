package jacobi.core.classifier.cart;

import java.util.List;
import java.util.Set;

import jacobi.core.classifier.cart.data.Column;
import jacobi.core.classifier.cart.data.DataTable;
import jacobi.core.classifier.cart.data.Instance;
import jacobi.core.classifier.cart.data.Sequence;
import jacobi.core.classifier.cart.node.Decision;
import jacobi.core.classifier.cart.node.DecisionNode;

public class ZeroR implements Rule {

	@Override
	public <T> DecisionNode<T> make(DataTable<T> dataTable, Set<Column<?>> features, Sequence seq) {
		Column<T> outCol = dataTable.getOutcomeColumn();
		
		List<Instance> instances = seq.apply(dataTable.getInstances(outCol));
		return this.make(this.distribution(instances, outCol), outCol);
	}
	
	public <T> DecisionNode<T> make(double[] dist, Column<T> outcomeCol) {
		int ans = this.argmax(dist);
		return new Decision<>(outcomeCol.valueOf(ans));
	}
	
	protected double[] distribution(List<Instance> instances, Column<?> outcomeCol) {
		double[] dist = new double[outcomeCol.cardinality()];
		for(Instance inst : instances) {
			dist[inst.feature] += inst.weight;
		}
		return dist;
	}
	
	protected int argmax(double[] dist) {
		int max = 0;
		for(int i = 0; i < dist.length; i++){
			if(dist[i] > dist[max]) {
				max = i;
			}
		}
		return max;
	}

}
