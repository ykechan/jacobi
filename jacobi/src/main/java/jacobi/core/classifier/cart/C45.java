package jacobi.core.classifier.cart;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.IntUnaryOperator;

import jacobi.api.Matrix;
import jacobi.core.classifier.cart.data.Column;
import jacobi.core.classifier.cart.data.DataTable;
import jacobi.core.classifier.cart.data.Sequence;
import jacobi.core.classifier.cart.node.DecisionNode;
import jacobi.core.util.Ranking;
import jacobi.core.util.Weighted;

/**
 * Implementation of the C4.5 algorithm.
 * 
 * @author Y.K. Chan
 *
 */
public class C45 implements Rule {
	
	protected C45(Partition partition, RuleFactory ruleFact) {
		this.partition = partition;
		this.ruleFact = ruleFact;
	}

	@Override
	public <T> Weighted<DecisionNode<T>> make(DataTable<T> dataTab, 
			Set<Column<?>> features, 
			Sequence seq) {
		
		Map<Column<?>, Sequence> sortSeq = this.sortByCols(dataTab);
		RankedPartition rankPart = new RankedPartition(this.partition, sortSeq);
		
		return this.ruleFact
			.create(rankPart, rankPart::groupBy)
			.make(dataTab, features, seq);
	}
		
	protected Map<Column<?>, Sequence> sortByCols(DataTable<?> dataTab) {
		Map<Column<?>, Sequence> map = new TreeMap<>();
		Matrix matrix = dataTab.getMatrix();
		Ranking ranking = Ranking.of(dataTab.size());
		
		for(Column<?> col : dataTab.getColumns()) {
			if(!col.isNumeric()){
				continue;
			}
			
			int[] seq = ranking.init(i -> matrix.get(i, col.getIndex())).sort();
			map.put(col, new Sequence(seq, 0, seq.length));
		}
		
		return map;
	}
	
	private Partition partition;
	private RuleFactory ruleFact;
	
	@FunctionalInterface
	public interface RuleFactory {
		
		public Rule create(
			Partition partition, 
			BiConsumer<Sequence, IntUnaryOperator> beforeSplit
		);
		
	}
}
