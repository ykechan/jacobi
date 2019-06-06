package jacobi.core.classifier.cart.data;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.IntUnaryOperator;

/**
 * This class represents an access sequence to a list of items.
 * 
 * @author Y.K. Chan
 *
 */
public class Sequence {
    
    /**
     * Constructor.
     * @param seq  Ranked index of items to access
     * @param begin  Begin rank of interest
     * @param end  End rank of interest
     */
    public Sequence(int[] seq, int begin, int end) {
        this.seq = seq;
        this.begin = begin;
        this.end = end;
    }
    
    /**
     * Get the length of this sequence
     * @return  Length of this sequence
     */
    public int length() {
        return this.end - this.begin;
    }
    
    /**
     * Get the index of item to access given the rank
     * @param rank  Rank of the item
     * @return  Index of item to access
     */
    public int indexAt(int rank) {
        return this.seq[this.begin + rank];
    }
    
    /**
     * Get an iterator from a list of items which will be access in sequence order.
     * @param items  List of items
     * @return  Iterator to access the items in sequence order
     */
    public <T> Iterator<T> iterator(List<T> items) {
        return new Iterator<T>() {

            @Override
            public boolean hasNext() {
                return curr < end;
            }

            @Override
            public T next() {
                return items.get(curr++);
            }
            
            private int curr = begin;
        }; 
    }
    
    /**
     * Re-arrange this sequence into different groups, in which groups will be accessed having
     * lower group number, and maintain in-place access sequence within each groups.
     * @param grouper  Function to get the group number given the index of item
     * @return  Sequence to access in different groups.
     */
    public List<Sequence> groupBy(IntUnaryOperator grouper) {
        int[] dist = new int[DEFAULT_SIZE];
        int step = DEFAULT_INCREASE;
        
        int[] groups = new int[this.length()];
        int maxGp = 0;
        
        for(int i = 0; i < groups.length; i++) {
            groups[i] = grouper.applyAsInt(this.indexAt(i));            
            if(groups[i] > maxGp) {
                maxGp = groups[i];
            }
            int nextLen = dist.length;
            while(groups[i] >= nextLen){
                nextLen += (step += 2);
            }
            if(nextLen > dist.length) {
                dist = Arrays.copyOf(dist, nextLen);
            }            
            dist[groups[i]]++;
        }
        this.regroup(groups, dist, maxGp + 1);
        return this.groupList(dist, maxGp + 1);
    } 
    
    /**
     * Split this sequence in sub-sequences after grouping.
     * @param dist   Distribution of items of each group, i.e. group sizes
     * @param num  Number of groups
     * @return  Sub-sequences for groups
     */
    protected List<Sequence> groupList(int[] dist, int num) {
        int[] starts = this.cumulative(dist, num, this.begin);
        return new AbstractList<Sequence>() {

            @Override
            public Sequence get(int index) {
                int start =  starts[index];
                int finish = index + 1 < starts.length ? starts[index + 1] : end;
                return new Sequence(seq, start, finish);
            }

            @Override
            public int size() {
                return starts.length;
            }
            
        };
    }
    
    /**
     * Re-arrange this access sequence according to group number first.
     * @param groups  Group numbers for each rank
     * @param dist   Distribution of items of each group, i.e. group sizes
     * @param num  Number of groups
     */
    protected void regroup(int[] groups, int[] dist, int num) {        
        int[] starts = this.cumulative(dist, num, 0);
        int[] seq = new int[groups.length];
        for(int i = 0; i < groups.length; i++) {
            int pos = starts[groups[i]]++;
            seq[pos] = this.seq[this.begin + i];
        }
        System.arraycopy(seq, 0, this.seq, this.begin, seq.length);
    }
    
    /**
     * Find the starting positions of each groups given the distribution of items.
     * @param dist   Distribution of items of each group, i.e. group sizes
     * @param len  Number of groups
     * @param base  Default starting position
     * @return  Starting positions of each groups
     */
    protected int[] cumulative(int[] dist, int len, int base) {
        int[] starts = new int[len];
        starts[0] = base;
        for(int i = 1; i < starts.length; i++) {
            starts[i] = starts[i - 1] + dist[i - 1]; 
        }
        return starts;
    }
    
    
    private int[] seq;
    private int begin, end; 
    
    private static final int DEFAULT_SIZE = 4;
    
    private static final int DEFAULT_INCREASE = 3;
}