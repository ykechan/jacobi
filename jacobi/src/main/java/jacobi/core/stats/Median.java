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
