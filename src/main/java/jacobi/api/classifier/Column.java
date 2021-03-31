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
package jacobi.api.classifier;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.DoubleToIntFunction;
import java.util.stream.Collectors;

/**
 * This class represents the type of column in a data set in CART model.
 * 
 * <p>In the CART model, an attribute can be nominal or numeric. A numeric attribute
 * is simply its own value. A nominal attribute however can take only a finite, discrete,
 * usually small number of values.</p>
 * 
 * <p>In this framework, nominal attributes are encoded in an index of array of items provided, 
 * given by a mapping function from a double to an integer. Thus the nominal values are in
 * [0, n) if there are n items.</p>
 * 
 * @author Y.K. Chan
 * @param <T>  Type of nominal items
 */
public class Column<T> implements Comparable<Column<?>> {
    
    /**
     * Create a numeric column
     * @param index  Index of the column
     * @return  Column object representing a numeric attribute
     */
    public static Column<Double> numeric(int index) {
        return new Column<>(index, Collections.emptyList(), v -> -1);
    }
    
    /**
     * Create a boolean column that is true when value positive, and false otherwise.
     * Zero is not considered positive, and thus will be rendered as false. 
     * @param index  Index of the column
     * @return  A boolean column
     */
    public static Column<Boolean> signed(int index) {
        return new Column<>(index, 
            Arrays.asList(Boolean.FALSE, Boolean.TRUE),
            v -> v > 0.0 ? 1 : 0
        );
    }
    
    /**
     * Create a nominal column with un-specified items
     * @param index  Index of the column
     * @param numItems  Number of values this nominal column can take
     * @param mapping  Mapping function from continuous to integral value
     * @return  Column object representing a nominal attribute
     * @throws  IllegalArgumentException if number of items is negative or zero.
     */
    public static Column<Integer> nominal(int index, int numItems, DoubleToIntFunction mapping) {
        if(numItems < 1){
            throw new IllegalArgumentException();
        }
        return new Column<>(index, new AbstractList<Integer>() {

            @Override
            public Integer get(int index) {
                return index;
            }

            @Override
            public int size() {
                return numItems;
            }
            
        }, mapping);
    }                
    
    /**
     * Create a nominal column with a collection of items.
     * @param index  Index of the column
     * @param items  Collection of items, can be duplicated.
     * @param <T>  Type of nominal items
     * @throws  IllegalArgumentException if number of items is negative or zero.
     */
    public static <T> Column<T> of(int index, Collection<T> items) {
        if(items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Unable to create nominal column with no items.");
        }
        
        return new Column<>(index,
            Collections.unmodifiableList(
                items instanceof Set
                ? items.stream().collect(Collectors.toList())
                : items.stream().distinct().collect(Collectors.toList())
            ),
            v -> (int) v
        );
    }
    
    /**
     * Create a column with given type class. Supports double.class/Double.class
     * for numeric column, boolean.class/Boolean.class for boolean column on signs,
     * and any Enum class.
     * @param index  Index of the column
     * @param type  Type of column items
     * @return  Column with given type class
     * @throws  IllegalArgumentException when type class is not supported.
     */
    @SuppressWarnings("unchecked")
    public static <T> Column<T> of(int index, Class<T> type) {
        if(type == double.class || type == Double.class) {
            return (Column<T>) Column.numeric(index);
        }
        
        if(type == boolean.class || type == Boolean.class) {
            return (Column<T>) Column.signed(index);
        }
        
        if(type.isEnum()) {
            return new Column<>(index, 
                Collections.unmodifiableList(Arrays.asList( 
                    type.getEnumConstants() 
                )), 
                v -> (int) v
            );
        }        
        
        throw new IllegalArgumentException("Un-recogized type " + type);
    }
    
    /**
     * Constructor.
     * @param index  Index of this column
     * @param items  List of discrete items for nominal attribute, empty for numeric
     * @param mapping  Mapping function from double to integer nominal value
     */
    public Column(int index, List<T> items, DoubleToIntFunction mapping) {
        this.index = index;
        this.items = items;
        this.mapping = mapping;
    }
    
    /**
     * Get the index of this column
     * @return  Index of this column
     */
    public int getIndex() {
        return index;
    }
    
    /**
     * Get the items for nominal attribute
     * @return  Items for nominal attribute, or empty for numeric attribute
     */
    public List<T> getItems() {
        return items;
    }
    
    /**
     * Get the mapping to integral value
     * @return  Mapping to integral value
     */
    public DoubleToIntFunction getMapping() {
        return mapping;
    }
    
    /**
     * Get the item given the nominal value
     * @param value  Nominal value
     * @return  Nominal item
     * @throws  IndexOutOfBoundsException if this is a numeric attribute
     */
    public T valueOf(int value) {
        return this.items.get(value);
    }
    
    /**
     * Get the item given the nominal value in numeric
     * @param value  Nominal value in numeric
     * @return  Nominal item
     * @throws  IndexOutOfBoundsException if this is a numeric attribute
     */
    public T valueOf(double value) {
        return this.valueOf(this.mapping.applyAsInt(value));
    }
    
    /**
     * Get the number of items the nominal attribute can take
     * @return  Number of items the nominal attribute can take
     */
    public int cardinality() {
        return this.items.size();
    }
    
    /**
     * Get if this attribute is numeric
     * @return  True if this attribute is numeric, false otherwise
     */
    public boolean isNumeric() {
        return this.items.isEmpty();
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(this.index, this.items);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Column){
            return this.compareTo((Column<?>) obj) == 0;
        }
        return false;
    }

    @Override
    public int compareTo(Column<?> col) {
        if(this == col){
            return 0;
        }
        
        if(this.getIndex() != col.getIndex()){
            return this.getIndex() < col.getIndex() ? -1 : 1;
        }
        
        if(this.cardinality() != col.cardinality()){
            throw new IllegalArgumentException("Unable to compare conflicting columns "
                + "#" + this.getIndex() + this.items
                + " and #" + col.getIndex() + col.items 
            );
        }
        
        for(int i = 0; i < this.cardinality(); i++) {
            if(!Objects.equals(this.valueOf(i), col.valueOf(i))) {
                throw new IllegalArgumentException();
            }
        }
        return 0;
    }
    

    private int index;
    private List<T> items;
    private DoubleToIntFunction mapping;
}
