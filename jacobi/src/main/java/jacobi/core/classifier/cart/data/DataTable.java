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
package jacobi.core.classifier.cart.data;

import java.util.List;

import jacobi.api.Matrix;
import jacobi.api.classifier.Column;

/**
 * Common interface of a data table in CART model.
 * 
 * <p>A data table is a tuple of a matrix with column types defined, a typed outcome
 * value associated with each row of the matrix, and a weight value associated with 
 * each row of the matrix 
 * </p>
 * 
 * <p>This data structure is designed to view the table by pairing a feature value
 * and its outcome together with an associated weight, in a certain access sequence.</p>
 * 
 * <p>For numeric column, the feature value given in the Instance object is the reference 
 * index of the corresponding row instead of the actual numeric value.</p>
 * 
 * <p>All implementations of this interface should be immutable. Neither the content nor
 * the ordering of the instances should change.</p>
 * 
 * @author Y.K. Chan
 *
 */
public interface DataTable<T> {        
    
    /**
     * Get the list of feature columns in this data table
     * @return  List of feature columns
     */
    public List<Column<?>> getColumns();
    
    /**
     * Get the type of outcome column in this data table
     * @return  Column of outcome
     */
    public Column<T> getOutcomeColumn();
    
    /**
     * Get the number of instances in this table
     * @return  Number of instances
     */
    public default int size() {
        return this.getMatrix().getRowCount();
    }
    
    /**
     * Get the backing matrix of this data table.
     * @return  Numerical data matrix
     */
    public Matrix getMatrix();
    
    /**
     * Get the instances of rows in a particular access sequence
     * @param column  Column of feature attribute, null for all 0 values as feature
     * @return  List of instances, i.e. pair of feature value and its outcome with an associated weight
     */
    public List<Instance> getInstances(Column<?> column);
    
}
