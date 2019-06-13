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

import jacobi.core.classifier.cart.data.Column;
import jacobi.core.classifier.cart.data.DataTable;
import jacobi.core.classifier.cart.data.Sequence;
import jacobi.core.util.Weighted;

/**
 * Common interface for partition the data set based on a given column.
 * 
 * <p>The function returns the impurity measure of partitioning the outcome 
 * based on a given column, together with some additional information to describe
 * the partition.</p>
 * 
 * @author Y.K. Chan
 * @param <T>  Type for additional information of the partitions
 */
public interface Partitioner<T> {
    
    /**
     * Find the impurity measure together with additional information 
     * of the partition given a column.
     * @param table  Data set
     * @param weights  Weights of each instances
     * @param col  Input column
     * @param seq  Sequence of instances
     * @return  Impurity measure with additional information
     */
    public Weighted<T> partition(
        DataTable table, 
        double[] weights, 
        Column<?> col, 
        Sequence seq
    );

}
