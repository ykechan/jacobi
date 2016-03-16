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
package jacobi.core.decomp.gauss;

import jacobi.api.Matrix;

/**
 * Common interface for an Elementary row operator in Gaussian Elimination.
 * 
 * Gaussian elimination can be carried out, with partial-pivoting, by 
 * only 2 types of elementary row operation:
 * 
 * - Swapping two rows, i.e. r[i] &lt;-&gt; r[j]
 * - Add another row multiplied with a scalar, i.e. r[i] &lt;- r[i] + k * r[j]
 * 
 * @author Y.K. Chan
 */
public interface ElementaryOperator {
   
    /**
     * Swapping two rows, i.e. r[i] &lt;-&gt; r[j]
     * @param i  row index
     * @param j  row index
     */
    public void swapRows(int i, int j);
    
    /**
     * Add another row multiplied with a scalar, i.e. r[i] &lt;- r[i] + a * r[j]
     * @param i  row index i
     * @param a  scalar value a
     * @param j  row index j
     */
    public void rowOp(int i, double a, int j);
    
    /**
     * Get the matrix the operator is working on.
     * @return  Operand matrix
     */
    public Matrix getMatrix();
    
}
