package jacobi.core.classifier.cart.rule;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.IntUnaryOperator;

import jacobi.api.Matrix;
import jacobi.api.classifier.Column;
import jacobi.api.classifier.cart.DecisionNode;
import jacobi.core.classifier.cart.data.DataTable;
import jacobi.core.classifier.cart.data.Sequence;
import jacobi.core.classifier.cart.measure.Partition;
import jacobi.core.classifier.cart.measure.RankedPartition;
import jacobi.core.util.Ranking;
import jacobi.core.util.Weighted;

/**
 * Implementation of the C4.5 algorithm.
 * 
 * <p>This implementation of C4.5 is not standard, i.e. this does not covers all the enhancements 
 * C4.5 bought to Id3. This class focuses on handling numerical attribute. Missing values and 
 * pruning are handled by other classes.</p>
 * 
 * @author Y.K. Chan
 *
 */
public class C45 implements Rule {
	
	/**
	 * Constructor.
	 * @param partition  Partition function
	 * @param ruleFact  Rule factory
	 */
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
	
	/**
	 * Sort numeric features
	 * @param dataTab  Input data table
	 * @return  Map of columns to rankings of instances
	 */
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
	
	/**
	 * Function for creating a rule implementation
	 * @author Y.K. Chan
	 *
	 */
	@FunctionalInterface
	public interface RuleFactory {
		
		/**
		 * Create a rule implementation
		 * @param partition  Partition function
		 * @param beforeSplit  Listener when splitting
		 * @return  Rule implementation
		 */
		public Rule create(
			Partition partition, 
			BiConsumer<Sequence, IntUnaryOperator> beforeSplit
		);
		
	}
}
