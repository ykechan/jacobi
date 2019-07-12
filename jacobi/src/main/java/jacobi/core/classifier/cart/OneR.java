package jacobi.core.classifier.cart;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

import jacobi.core.classifier.cart.data.Column;
import jacobi.core.classifier.cart.data.DataTable;
import jacobi.core.classifier.cart.data.Sequence;
import jacobi.core.classifier.cart.node.DecisionNode;
import jacobi.core.classifier.cart.node.NominalSplit;
import jacobi.core.util.Weighted;

public class OneR implements Rule {
	
	public OneR(Rule zeroR, Partition<?> partition) {
		this.zeroR = zeroR;
		this.partition = partition;
	}
	
	@Override
	public <T> Weighted<DecisionNode<T>> make(
			DataTable<T> dataTable, 
			Set<Column<?>> features, 
			Sequence seq) {
		
		Weighted<?> split = null;
		Column<?> target = null;
		for(Column<?> feat : features) {
			Weighted<?> result = this.partition.measure(dataTable, feat, seq);
			
			if(split == null || result.weight < split.weight){
				split = result;
				target = feat;
			}
		}
		
		if(split == null){
			return this.zeroR.make(dataTable, features, seq);
		}
		
		if(!target.isNumeric()) {
			
		}
		return null;
	}
	
	protected <T> Merger<T> nominal(Column<?> nomCol) {
		return ls -> new NominalSplit<>(nomCol, null, ls);
	}
	
	protected <T> Merger<T> binaryNumeric(Column<?> numCol, double threshold) {
		return ls -> null;
	}		
	
	private Rule zeroR;
	private Partition<?> partition;	
	
	protected interface Merger<T> extends Function<List<DecisionNode<T>>, DecisionNode<T>> {
		
	}
}
