/* 
 * The MIT License
 *
 * Copyright 2019 Y.K. Chan
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package jacobi.core.classifier.cart;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntUnaryOperator;

/**
 * Implementation of Sequence backed by an integer array.
 * 
 * @author Y.K. Chan
 *
 */
public class ArraySequence implements Sequence {
    
    /**
     * Constructor.
     * @param seq  Ranked index of items to access
     * @param begin  Begin rank of interest
     * @param end  End rank of interest
     */
    public ArraySequence(int[] seq, int begin, int end) {
        this.seq = seq;
        this.begin = begin;
        this.end = end;
    }
    
    @Override
    public int length() {
        return this.end - this.begin;
    }

    @Override
    public int position() {
        return this.begin;
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
     * Re-arrange this sequence into different groups in-place. Items are assigned to groups, and
     * re-arranged such that the following properties hold: all items in group i comes before
     * any items in group j if i &lt; j, and if item a comes before item b in the original
     * sequence and both items are in group k, item a comes before item b after re-arrangement.
     * @param grouper  Function to get the group number given the index of item
     * @return  Sequence to access in different groups.
     */
    public List<Sequence> groupBy(IntUnaryOperator grouper) {
        int[] dist = new int[DEFAULT_SIZE];
        int step = DEFAULT_INCREASE;
        
        int[] groups = new int[this.length()];
        int maxGp = 0;
        
        boolean monotone = true;
        int prev = 0;
        for(int i = 0; i < groups.length; i++) {
            groups[i] = grouper.applyAsInt(this.indexAt(i));            
            if(groups[i] > maxGp) {
                maxGp = groups[i];
            }
            
            if(groups[i] < prev) {
            	monotone = false;
            }
            
            prev = groups[i];
            
            int nextLen = dist.length;
            while(groups[i] >= nextLen){
                nextLen += (step += 2);
            }
            if(nextLen > dist.length) {
                dist = Arrays.copyOf(dist, nextLen);
            } 
            dist[groups[i]]++;
        }
        
        if(!monotone) {
        	this.regroup(groups, dist, maxGp + 1);
        }
        
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
            public ArraySequence get(int index) {
                int start =  starts[index];
                int finish = index + 1 < starts.length ? starts[index + 1] : end;
                return new ArraySequence(seq, start, finish);
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