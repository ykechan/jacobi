package jacobi.core.classifier.cart;

import java.util.Set;

import jacobi.core.classifier.cart.data.Column;
import jacobi.core.classifier.cart.data.DataTable;
import jacobi.core.classifier.cart.data.Sequence;
import jacobi.core.classifier.cart.node.DecisionNode;

public interface Rule {
	
	public <T> DecisionNode<T> make(DataTable<T> dataTable, Set<Column<?>> features, Sequence seq);

}
