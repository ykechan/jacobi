package jacobi.core.classifier.cart.rule;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;
import java.util.stream.Collectors;

import jacobi.api.Matrix;
import jacobi.api.classifier.Column;
import jacobi.api.classifier.DataTable;
import jacobi.api.classifier.cart.DecisionNode;
import jacobi.core.classifier.cart.ArraySequence;
import jacobi.core.classifier.cart.Sequence;
import jacobi.core.classifier.cart.measure.Partition;
import jacobi.core.classifier.cart.measure.RankedPartition;
import jacobi.core.util.Ranking;

/**
 * Implementation of the C4.5 algorithm.
 * 
 * <p>This implementation of C4.5 is not standard, i.e. this does not covers all the enhancements 
 * C4.5 bought to Id3. This class focuses on handling numerical attributes. Missing values and 
 * pruning are handled by other classes.</p>
 * 
 * @author Y.K. Chan
 *
 */
public class C45 implements Rule {
	
	/**
	 * Factory method for standard C4.5 implementation
	 * @param impurity  Impurity measurement function
	 * @param partition  Partition function
	 * @return  Implementation of C4.5
	 */
	public static C45 of(Partition partition) {
		return new C45(partition, p -> new Id3(
			ZeroR.getInstance(),
			new OneR(ZeroR.getInstance(), p)
		));
	}
	
	/**
	 * Constructor.
	 * @param partition  Partition function
	 * @param ruleFact  Rule factory
	 */
	public C45(Partition partition, Function<Partition, Rule> ruleFact) {
		this.partition = partition;
		this.ruleFact = ruleFact;
	}

	@Override
	public <T> DecisionNode<T> make(DataTable<T> dataTab, 
			Set<Column<?>> features, 
			Sequence seq) {
		
		Map<Column<?>, Sequence> sortSeq = this.sortByCols(dataTab, seq);
		
		if(sortSeq.isEmpty()) {
			// no numeric feature
			return this.ruleFact.apply(this.partition).make(dataTab, features, seq);
		}
		
		RankedPartition rankPart = new RankedPartition(this.partition, sortSeq);
		return this.ruleFact
			.apply(rankPart)
			.make(dataTab, features, this.listenGroupBy(seq, rankPart::groupBy));
	}
	
	/**
	 * Sort numeric features
	 * @param dataTab  Input data table
	 * @return  Map of columns to rankings of instances
	 */
	protected Map<Column<?>, Sequence> sortByCols(DataTable<?> dataTab, Sequence defaultSeq) {
		Map<Column<?>, Sequence> map = new TreeMap<>();
		Matrix matrix = dataTab.getMatrix();
		Ranking ranking = Ranking.of(defaultSeq.length());
		
		for(Column<?> col : dataTab.getColumns()) {
			if(!col.isNumeric()){
				continue;
			}
			
			int[] seq = ranking
					.init(i -> matrix.get(defaultSeq.indexAt(i), col.getIndex()))
					.sort();
			for(int i = 0; i < seq.length; i++) {
				seq[i] = defaultSeq.indexAt(seq[i]);
			}
			
			map.put(col, new ArraySequence(seq, 0, seq.length));
		}
		
		return map;
	}
	
	protected Sequence listenGroupBy(Sequence seq, BiConsumer<Sequence, IntUnaryOperator> before) {
		return new Sequence() {

			@Override
			public int position() {
				return seq.position();
			}

			@Override
			public int length() {
				return seq.length();
			}

			@Override
			public int indexAt(int rank) {
				return seq.indexAt(rank);
			}

			@Override
			public List<Sequence> groupBy(IntUnaryOperator groupFn) {
				before.accept(seq, groupFn);
				return seq.groupBy(groupFn)
					.stream()
					.map(s -> listenGroupBy(s, before))
					.collect(Collectors.toList());
			}
			
		};
	}
	
	private Partition partition;
	private Function<Partition, Rule> ruleFact;
}
