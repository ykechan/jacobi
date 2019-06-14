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

/**
 * Common interface of a data table in CART model.
 * 
 * <p>A data table consists of feature vectors and its paired outcome.</p>
 * 
 * @author Y.K. Chan
 *
 */
public interface DataTable {
    
    /**
     * Get all column types
     * @return  List of all columns
     */
    public List<Column<?>> getColumns();
    
    /**
     * Get the data matrix for numeric values
     * @return  Numeric values as matrix
     */
    public Matrix getMatrix();
    
    /**
     * Get the nominal values of a column
     * @param index  Column index
     * @return  Nominal values
     */
    public int[] nominals(int index);
    
    /**
     * Get the column type for outcome
     * @return  Column type for outcome
     */
    public Column<?> getOutcomeColumn();
    
    /**
     * Get the outcomes of the instances
     * @return  Outcomes of a row
     */
    public int[] outcomes();
    
    /**
     * Get the number of rows in this data table
     * @return  Number of rows
     */
    public default int size() {
        return this.outcomes().length;
    }                        
    
}
