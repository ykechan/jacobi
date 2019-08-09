package jacobi.core.classifier.cart;

import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.IntUnaryOperator;
import java.util.stream.Collectors;

import jacobi.core.classifier.cart.data.Column;
import jacobi.core.classifier.cart.data.DataTable;
import jacobi.core.classifier.cart.data.Sequence;
import jacobi.core.util.Weighted;

/**
 * Maintain the ranks of instances within different partitions and pass it to 
 * underlying implementation of Partition function.
 * 
 * <p>For numeric columns, it is beneficial to keep the sorted sequence of rows
 * each that it need not to be sorted again after partitioning. This class tracks
 * the partition action and maintain the sorted sequence, which would be used 
 * to measure numeric columns instead of using the sequence given.</p>
 * 
 * <p>This class is stateful.</p>
 * 
 * @author Y.K. Chan
 */
public class RankedPartition implements Partition {
    
	/**
	 * Constructor
	 * @param partition  Base partition function
	 * @param ranks  Initial ranked sequence
	 */
    public RankedPartition(Partition partition, Map<Column<?>, Sequence> ranks) {
        this.partition = partition;
        this.ranks = this.toSortedMap(ranks);
    }

    /**
     * Group the related ranked sequences given an event of grouping occurred to 
     * the default sequence
     * @param seq  Default sequence
     * @param grouper  Grouping function
     * @return  This instance with ranked sequences updated
     */
    public RankedPartition groupBy(Sequence seq, IntUnaryOperator grouper) {
        Map<Tuple, Sequence> targets = new TreeMap<>(this.ranks.subMap(
        	Tuple.floor(seq.start()), 
        	Tuple.ceiling(seq.start())
        ));
        
        for(Map.Entry<Tuple, Sequence> entry : targets.entrySet()) {
        	this.ranks.putAll(
        		entry.getValue().groupBy(grouper)
        			.stream()
        			.collect(Collectors.toMap(
        				s -> new Tuple(s.start(), entry.getKey().lower), 
        				s -> s
        			))
        	);
        	this.ranks.remove(entry.getKey());
        }
        return this;
    }

    @Override
    public Weighted<double[]> measure(DataTable<?> table, Column<?> target, Sequence seq) {
        Sequence rank = this.ranks.get(new Tuple(seq.start(), target.getIndex()));
        
        if(rank != null && rank.length() != seq.length()) {
            throw new IllegalArgumentException("Partition overlapped");
        }
        
        return this.partition.measure(table, target, rank == null ? rank : seq);
    }
    
    /**
     * Create a sorted map of ranked sequence map on column 
     * @param ranks  Ranked sequence map on column
     * @return  Sorted map of ranked sequence on start position and column index
     */
    private SortedMap<Tuple, Sequence> toSortedMap(Map<Column<?>, Sequence> ranks) {
        return ranks.entrySet().stream().collect(Collectors.toMap(
            e -> new Tuple(e.getValue().start(), e.getKey().getIndex()),
            e -> e.getValue(),
            (a, b) -> {
                throw new UnsupportedOperationException(
                   "Duplicated column in ranks "
                );
            },
            TreeMap::new
        ));
    }
    
    private SortedMap<Tuple, Sequence> ranks;
    private Partition partition;
    
    /**
     * A tuple of integer &lt;a, b&gt; that is comparable for using as keys of sequences.
     * 
     * <p>A tuple &lt;a, b&gt; is less than &lt;c, d&gt; iff a &lt; b, or a = b and c &lt; d.
     * Greater than is similarly defined.</p>
     * 
     * @author Y.K. Chan
     *
     */
    protected static class Tuple implements Comparable<Tuple> {
        
    	/**
    	 * Minimum value of a tuple having the upper part equals to the specified value
    	 * @param upper  Upper value
    	 * @return  Minimum value of a tuple with given upper part
    	 */
        public static Tuple floor(int upper) {
            return new Tuple(upper, Integer.MIN_VALUE);
        }
        
        /**
    	 * Maximum value of a tuple having the upper part equals to the specified value
    	 * @param upper  Upper value
    	 * @return  Minimum value of a tuple with given upper part
    	 */
        public static Tuple ceiling(int upper) {
            return new Tuple(upper, Integer.MAX_VALUE);
        }
        
        /**
         * Upper and lower part of value.
         */
        public final int upper, lower;

        /**
         * Constructor.
         * @param upper  Upper part
         * @param lower  Lower part
         */
        public Tuple(int upper, int lower) {
            this.upper = upper;
            this.lower = lower;
        }
        
        @Override
		public int hashCode() {
        	return Objects.hash(this.lower, this.upper);
		}

		@Override
		public boolean equals(Object obj) {
			if(obj instanceof Tuple) {
				Tuple oth = (Tuple) obj;
				return this.lower == oth.lower && this.upper == oth.upper;
			}
			return false;
		}

		@Override
        public int compareTo(Tuple oth) {
            return this.upper == oth.upper
                 ? this.lower < oth.lower ? -1 : this.lower > oth.lower ? 1 : 0
                 : this.upper < oth.upper ? -1 : 1;
        }
        
    }
}
