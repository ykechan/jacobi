/*
 * Copyright (C) 2015 Y.K. Chan
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package jacobi.api;

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
     * Returned matrix is independent from the original matrix, i.e. when
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
     */
    public default void getAndSet(int rowIndex, Consumer<double[]> f) {
        this.getApplySet(rowIndex, (r) -> { f.accept(r); return null; });
    }
}