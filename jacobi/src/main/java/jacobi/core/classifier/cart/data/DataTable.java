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
 * @author Y.K. Chan
 *
 */
public interface DataTable extends Matrix {
    
    /**
     * Get all column types
     * @return  List of all columns
     */
    public List<Column<?>> getColumns();
    
    /**
     * Get all column types for nominal columns only.
     * @return  List of all nominal columns
     */
    public List<Column<?>> getNominalColumns();
    
    /**
     * Get the column type for outcome
     * @return  Column type for outcome
     */
    public Column<?> getOutcomeColumn();
    
    /**
     * Get the nominal values of a row
     * @param index  Row index
     * @return  Nominal values
     */
    public int[] nominals(int index);
    
    /**
     * Get the outcome of a row
     * @param index  Row index
     * @return  Outcome of a row
     */
    public int outcome(int index);

    @Override
    public default Matrix setRow(int index, double[] values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public default Matrix set(int i, int j, double value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public default Matrix swapRow(int i, int j) {
        throw new UnsupportedOperationException();
    }
    
}
