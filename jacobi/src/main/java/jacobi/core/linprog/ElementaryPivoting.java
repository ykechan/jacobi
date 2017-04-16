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

package jacobi.core.linprog;

import jacobi.api.Matrix;
import jacobi.core.linprog.MutableTableau.Pivoting;

/**
 * Implementation for pivoting operation by elementary row operations.
 * 
 * Pivoting operation for simplex algorithm is essentially a change of column basis on the tableau
 * [ 0 -c^t 0]
 * [         ], where diag(J) = {J[i, i] = +-1, J[i, j] = 0 for all i &lt;&gt; j}
 * [ b   A* J]
 * 
 * But this operation is done on the compact tableau
 * [ 0 -c^t ]
 * [|b|  A* ]
 * , and the original structure is by swapping out the +-e^k introduced in [-c^t; A*] and 
 * non-standard basis introduced in J.
 * 
 * By conforming to notation in Simplex algorithm, index all refers to the compact tableau.
 * 
 * @author Y.K. Chan
 */
public class ElementaryPivoting implements Pivoting {    
    
    @Override
    public void run(Matrix matrix, int row, int col) {
        double[] pivot = matrix.getRow(row);
        int sign = (int) Math.signum(pivot[col]);
        System.out.println("sign = " + sign);
        if(sign == 0){
            throw new IllegalArgumentException("Unable to use zero as pivot (" + row + "," + col + ").");
        }
        for(int k = 0; k < matrix.getRowCount(); k++){
            if(k == row){
                continue;
            }
            matrix.getAndSet(k, (r) -> this.eliminate(r, pivot, col, sign));
        }
        this.normalize(pivot, col, sign);        
    }    
    
    /**
     * Normalize the pivot row s.t. the enter column is 1, and swap with the leaving column.
     * @param pivot  Pivot row
     * @param enter  Enter column
     * @param sign  Sign of the leaving row
     */
    protected void normalize(double[] pivot, int enter, int sign) {
        double denom = Math.abs(pivot[enter]); 
        pivot[enter] = sign / denom;
        for(int i = enter + 1; i < pivot.length; i++){
            pivot[i] /= denom;
        }
        for(int i = 0; i < enter; i++){
            pivot[i] /= denom;
        }
    }
    
    /**
     * Eliminate the enter column of a row by a pivot row, and swap with the leaving column.
     * @param row  Row to be eliminated
     * @param pivot  Pivot row
     * @param enter  Enter column
     * @param sign  Sign of the leaving row
     */
    protected void eliminate(double[] row, double[] pivot, int enter, int sign) {
        double factor = -row[enter] / pivot[enter]; 
        row[enter] = sign * factor;
        for(int i = enter + 1; i < row.length; i++){
            row[i] += factor * pivot[i];
        }
        for(int i = 0; i < enter; i++){
            row[i] += factor * pivot[i];
        }
    }         

}
