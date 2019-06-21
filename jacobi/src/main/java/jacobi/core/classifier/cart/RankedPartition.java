package jacobi.core.classifier.cart;

import java.util.Map;
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
 * @param <T>
 */
public class RankedPartition<T> implements Partition<T> {
    
    public RankedPartition(Partition<T> partition, Map<Column<?>, Sequence> ranks) {
        this.partition = partition;
        this.ranks = this.toSortedMap(ranks);
    }

    public RankedPartition<T> partition(Sequence seq, IntUnaryOperator grouper) {
        Map<Tuple, Sequence> map = new TreeMap<>();
        return this;
    }

    @Override
    public Weighted<T> measure(DataTable table, Column<?> target, Sequence seq) {
        Sequence rank = this.ranks.get(new Tuple(seq.start(), target.getIndex()));
        return this.partition.measure(table, target, rank == null ? rank : seq);
    }
    
    private SortedMap<Tuple, Sequence> toSortedMap(Map<Column<?>, Sequence> ranks) {
        return ranks.entrySet().stream().collect(Collectors.toMap(
            e -> new Tuple(e.getValue().start(), e.getKey().getIndex()),
            e -> e.getValue(),
            (a, b) -> {
                throw new UnsupportedOperationException(
                   "Duplicated column in ranks "
                );
            },
            () -> new TreeMap<>()
        ));
    }
    
    private SortedMap<Tuple, Sequence> ranks;
    private Partition<T> partition;
    
    protected static class Tuple implements Comparable<Tuple> {
        
        public static Tuple floor(int upper) {
            return new Tuple(upper, Integer.MIN_VALUE);
        }
        
        public static Tuple ceiling(int upper) {
            return new Tuple(upper, Integer.MAX_VALUE);
        }
        
        public final int upper, lower;

        public Tuple(int upper, int lower) {
            this.upper = upper;
            this.lower = lower;
        }

        @Override
        public int compareTo(Tuple oth) {
            return this.upper == oth.upper
                 ? this.lower < oth.lower ? -1 : this.lower > oth.lower ? 1 : 0
                 : this.upper < oth.upper ? -1 : 1;
        }
        
    }
}
