/* 
 * The MIT License
 *
 * Copyright 2017 Y.K. Chan
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
package jacobi.api;

import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Matrix data structure.
 * @author Y.K. Chan
 */
public interface Matrix {
    
    /**
     * Get number of rows
     * @return  Number of rows
     */
    public int getRowCount();
    
    /**
     * Get number of columns
     * @return  Number of columns
     */
    public int getColCount();
    
    /**
     * Get row reference
     * @param index  index of row
     * @return Row reference
     */
    public double[] getRow(int index);
    
    /**
     * Set row values
     * @param index  index of row
     * @param values  row values
     * @return This
     * @throws IllegalArgumentException  
     *    if null pointer given or number of columns mismatch.
     */
    public Matrix setRow(int index, double[] values);
    
    /**
     * Get a particular matrix element value. 
     * This method may not be most efficient to get all matrix elements. 
     * Use getRow(...) instead.
     * @param i  Row index
     * @param j  Column index
     * @return   Matrix element.
     */
    public default double get(int i, int j) {
        return this.getRow(i)[j];
    }
    
    /**
     * Set a particular matrix element value. 
     * This method may not be efficient to set all matrix elements. 
     * Use setRow(...) instead.
     * @param i  Row index
     * @param j  Column index
     * @param value  Matrix element.
     * @return This
     */
    public default Matrix set(int i, int j, double value) {
        double[] row = this.getRow(i);
        row[j] = value;
        this.setRow(i, row);
        return this;
    }
    
    /**
     * Swap two rows.
     * @param i  index of row to be swapped
     * @param j  index of row to be swapped
     * @return This
     */
    public Matrix swapRow(int i, int j);
    
    /**
     * Extend functionalities to given interface.
     * @param <T>  Extension interface
     * @param clazz  Java class of the extension interface
     * @return An instance of the interface
     */
    public <T> T ext(Class<T> clazz);
    
    /**
     * Copy the content of current matrix to another mutable matrix.
     * 
     * Returned matrix may not be the same type as the original matrix, but
     * must have the same dimension and value in each elements and mutable.
     * 
     * Returned matrix is independent from the original matrix, i.e.&nbsp;when
     * the value of the original matrix changes, the returned matrix will
     * not be affected.
     * @return  A mutable copy of original matrix
     */
    public Matrix copy();
    
    /**
     * Copy the matrix to a 2-D array.
     * @return  Matrix elements in row-by-row manner.
     */
    public default double[][] toArray() {
        double[][] elem = new double[this.getRowCount()][this.getColCount()];
        for(int i = 0; i < elem.length; i++){
            System.arraycopy(this.getRow(i), 0, elem[i], 0, elem[i].length);
        }
        return elem;
    }

    
    /**
     * Get a row, apply a function on it, and set the row values back.
     * @param <T>  Function return type
     * @param rowIndex  Row index
     * @param f  Row function
     * @return Function return value
     */
    public default <T> T getApplySet(int rowIndex, Function<double[], T> f) {
        double[] row = this.getRow(rowIndex);
        try {
          return f.apply(row);
        } finally {
            this.setRow(rowIndex, row);
        }
    }

    /**
     * Get a row, apply a consumer on it, and set the row values back.
     * @param rowIndex  Row index
     * @param f  Row consumer
     * @return  This matrix
     */
    public default Matrix getAndSet(int rowIndex, Consumer<double[]> f) {
        this.getApplySet(rowIndex, (r) -> { f.accept(r); return null; });
        return this;
    }
    
    
}