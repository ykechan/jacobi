package jacobi.core.classifier.cart;

import jacobi.core.classifier.cart.data.Column;
import jacobi.core.classifier.cart.data.DataTable;
import jacobi.core.classifier.cart.data.Sequence;
import jacobi.core.util.Weighted;

/**
 * Common interface for measuring the impurity of the outcome distribution after partitioning 
 * a data table by a certain column.
 * 
 * <p>Additional information on the partitioning measured, should there are multiple
 * ways to partition by that given column, may also be provided.</p>
 * 
 * <p>Implementations should access the data in a given access sequence, and only instances
 * appearing in the access sequence is considered.</p>
 * 
 * @author Y.K. Chan
 * @param <T>  Type for partition information
 */
public interface Partition<T> {
    
    /**
     * Measure the impurity of outcome distribution
     * @param table  Data set
     * @param target  Partitioning column
     * @param seq  Access sequence
     * @return
     */
    public Weighted<T> measure(DataTable table, Column<?> target, Sequence seq);

}
