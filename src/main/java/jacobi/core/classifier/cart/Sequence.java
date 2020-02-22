package jacobi.core.classifier.cart;

import java.util.AbstractList;
import java.util.List;
import java.util.function.IntUnaryOperator;

/**
 * Common interface representing a sequence of indices to a list of items.
 * 
 * @author Y.K. Chan
 *
 */
public interface Sequence {
	
	/**
     * Get the position of this sub-sequence with the full sequence. 
     * A full sequence is just an improper sub-sequence.
     * @return  Position of this sub-sequence within the full sequence.
     */
    public int position();
	
	/**
	 * Get the length of sequence.
	 * @return  Length of the sequence
	 */
	public int length();
	
	/**
	 * Get the index at a certain position
	 * @param rank  Input rank, i.e. position of the index
	 * @return  Index at a certain position
	 */
	public int indexAt(int rank);
	
	/**
     * Re-arrange this sequence into different groups in-place. Items are assigned to groups, and
     * re-arranged such that the following properties hold: all items in group i comes before
     * any items in group j if i &lt; j, and if item a comes before item b in the original
     * sequence and both items are in group k, item a comes before item b after re-arrangement.
     * @param grouper  Function to get the group number given the index of item
     * @return  Sequence to access in different groups.
     */
	public List<Sequence> groupBy(IntUnaryOperator groupFn);
	
	/**
     * Get a list from a list of items which will be access in sequence order.
     * @param items  List of items
     * @return  Iterator to access the items in sequence order
     */
    public default <T> List<T> apply(List<T> items) {
        return new AbstractList<T>() {

            @Override
            public T get(int index) {
                return items.get(indexAt(index));
            }

            @Override
            public int size() {
                return length();
            }
        }; 
    }

}
