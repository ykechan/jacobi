package jacobi.core.classifier.cart;

import java.util.function.DoubleToIntFunction;

/**
 * This class represents a model of a column in a data set in the CART model.
 * 
 * <p>In CART model, attributes are defined as nominal or numeric. For nominal attributes,
 * data can take a value from a set of items. The data is thus represented as the index of an 
 * array of items pre-defined in this column model.
 * </p>
 * 
 * <p>For numeric attributes, there is no such discrete set. Thus an empty set is 
 * given to reflect such model.
 * </p>
 * 
 * @author Y.K. Chan
 * @param <T>  Type of items
 */
public class ColumnModel<T> {
    
    /**
     * Constant Column Model for Numeric Attributes
     */
    public static final ColumnModel<Void> NUMERIC = new ColumnModel<>(new Void[0], v -> -1);
    
    /**
     * Constructor.
     * @param items  Array of items
     * @param mapping  Function to get discrete the value
     */
    public ColumnModel(T[] items, DoubleToIntFunction mapping) {
        if(items.length == 0 && items.getClass() != Void[].class) {
            throw new IllegalArgumentException("Unable to create nominal column without items");
        }
        this.items = items;
        this.mapping = mapping;
    }
    
    /**
     * Determine if this column is numeric
     * @return  True if this column is numeric, i.e. item set is empty, false otherwise.
     */
    public boolean isNumeric() {
        return this.cardinality() == 0;
    }
    
    /**
     * Get the size of the set of items.
     * @return  Size of the set of items.
     */
    public int cardinality() {
        return this.items.length;
    }
    
    /**
     * Get the item the nominal value represents
     * @param norminal  nominal value
     * @return  Item the nominal value represents
     */
    public T valueOf(int norminal) {
        return this.items[norminal];
    }
    
    /**
     * Get the mapping function.  
     * @return  Mapping function 
     */
    public DoubleToIntFunction getMapping() {
        return mapping;
    }

    private T[] items;
    private DoubleToIntFunction mapping;
}
