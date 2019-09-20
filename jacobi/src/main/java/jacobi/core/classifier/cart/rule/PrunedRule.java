package jacobi.core.classifier.cart.rule;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import jacobi.api.classifier.Column;
import jacobi.api.classifier.DataTable;
import jacobi.api.classifier.cart.DecisionNode;
import jacobi.core.classifier.cart.Sequence;
import jacobi.core.classifier.cart.node.BinaryNumericSplit;
import jacobi.core.classifier.cart.node.NominalSplit;

public class PrunedRule implements Rule {
	
	public PrunedRule(Rule base, OneR oneR, Pruner pruner) {
		this.base = base;
		this.oneR = oneR;
		this.pruner = pruner;
	}
	
	@Override
	public <T> DecisionNode<T> make(DataTable<T> dataTable, 
			Set<Column<?>> features,
			Sequence seq) {
		return null;
	}
	
	protected <T> DecisionNode<T> prune(DecisionNode<T> node, 
			DataTable<T> dataTab, 
			Sequence seq) {
		
		List<DecisionNode<T>> children = this.childrenOf(node);
		if(children.isEmpty()){
			return node;
		}
		
		DecisionNode<T> first = children.get(0);
		return this.pruner.prune(
			children.stream().allMatch(n -> n.equals(first)) 
				? first 
				: node, 
			dataTab, 
			seq
		);
	}
	
	protected <T> List<DecisionNode<T>> childrenOf(DecisionNode<T> node) {
		if(node instanceof NominalSplit) {
			return ((NominalSplit<T>) node).getChildren();
		}
		
		if(node instanceof BinaryNumericSplit){
			BinaryNumericSplit<T> binary = (BinaryNumericSplit<T>) node;
			return Arrays.asList(binary.getLeft(), binary.getRight());
		}
		
		return Collections.emptyList();
	}

	private Rule base;
	private OneR oneR;
	private Pruner pruner;
}
