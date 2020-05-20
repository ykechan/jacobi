package jacobi.core.graph.util;

/**
 * Common interface for a min-heap.
 * 
 * <p>
 * A min-heap is a data structure that provides constant time in finding the minimum element, and
 * logarithmic time in adding an element and removing the minimum element.
 * </p>
 * 
 * <p>
 * This interface is specialized in storing integers as elements which each element is
 * associated with a weight in double.
 * </p>
 * 
 * @author Y.K. Chan
 * @deprecated Use jacobi.core.util.Enque and jacobi.core.util.MinHeap instead
 */
@Deprecated
public interface MinHeap {
    
    /**
     * Check if heap is empty.
     * @return  True if empty, false otherwise
     */
    public boolean isEmpty();
    
    /**
     * Find the minimum weight stored in the heap.
     * @return  Weight of an element stored that is minimum
     */
    public double findMin();    
    
    /**
     * Add an new element and its associated weight.
     * @param element  Element value
     * @param weight  Weight value
     */
    public void push(int element, double weight);
    
    /**
     * Remove an element which has minimum weight and retrieve the element value.
     * @return  Element value with minimum weight
     */
    public int pop();

}
