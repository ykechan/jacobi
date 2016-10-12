/*
 * Copyright (C) 2016 Y.K. Chan
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

package jacobi.core.stats;

import jacobi.api.Matrix;

/**
 * Compute the median of each column.
 * 
 * It is common to represent a dataset as a matrix, with a column as a random
 * variable and each row as a data point. In this case this class computes
 * the median for each random variable.
 * 
 * Median can be found by sorting the whole data, but it would be overkill and
 * not efficient enough. Instead this class uses self-implemented (stochastic)-linear 
 * selection algorithm.
 * 
 * This class employs linear-selection, but instead of choosing median-of-medians,
 * it randomly samples 15 sample data, find 3 medians from 3 groups of 5,
 * and uses the median for quicksort pivoting. If each is not the right median,
 * it repeats the procedure with range limited.
 * 
 * @author Y.K. Chan
 */
public class Median {
    
    private int medianOf5(int[] pivots, Matrix matrix, int col) {
        if(matrix.get(pivots[0], col) > matrix.get(pivots[1], col)){
            this.swap(pivots, 0, 1);
        }
        if(matrix.get(pivots[2], col) > matrix.get(pivots[3], col)){
            this.swap(pivots, 2, 3);
        }
        if(matrix.get(pivots[0], col) < matrix.get(pivots[2], col)){
            
        }
        return 0;
    }
    
    private void swap(int[] array, int i, int j) {
        int temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }
}
