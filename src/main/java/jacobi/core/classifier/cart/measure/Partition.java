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
package jacobi.core.classifier.cart.measure;

import jacobi.api.classifier.Column;
import jacobi.api.classifier.DataTable;
import jacobi.core.classifier.cart.Sequence;
import jacobi.core.util.Weighted;

/**
 * Common interface for measuring the impurity of the outcome distribution after partitioning 
 * a data table by a certain column.
 * 
 * <p>For a pure data set, i.e. all instances have the same outcome, NaN is returned as weight.
 * </p>
 * 
 * <p>Implementations should return the threshold for the partitions for numeric attributes,
 * or an empty array for nominal attributes.</p>
 * 
 * <p>Implementations should access the data in a given access sequence, and only instances
 * appearing in the access sequence is considered.</p>
 * 
 * @author Y.K. Chan
 */
public interface Partition {
    
    /**
     * Measure the impurity of outcome distribution
     * @param table  Data set
     * @param target  Partitioning column
     * @param seq  Access sequence
     * @return  Impurity of outcome distribution
     */
    public Weighted<double[]> measure(DataTable<?> table, Column<?> target, Sequence seq);

}
