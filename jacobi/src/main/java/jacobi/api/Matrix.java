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
     * Swap two rows.
     * @param i  index of row to be swapped
     * @param j  index of row to be swapped
     * @return This
     */
    public Matrix swapRow(int i, int j);
    
}
