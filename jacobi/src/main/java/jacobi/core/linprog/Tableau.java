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

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.core.impl.ColumnVector;
import jacobi.core.impl.ImmutableMatrix;
import jacobi.core.util.Throw;
import java.util.stream.IntStream;

/**
 * Tableau structure for linear programming problem used in Simplex Algorithm.
 * 
 * The Linear Programming problem is as follows:
 * Maximize c^t * x s.t. A*x &lt;= b.
 * 
 * The feasibility inequality constraint A*x &lt;= b can be expressed as a system of linear equation
 * [A|I]*[x|s] = b where s &gt;= 0.
 * 
 * Thus the problem can be expressed as 
 * [ 1 -c^t  0 ][ x ]   [ z ]
 * [           ][   ] = [   ]
 * [ 0    A  I ][ s ]   [ b ]
 * 
 * This class does not impose the requirement that b &gt;= 0. To cater such situation, instead of [A|I],
 * an [A*|J] is used where J = {sgn(b[k])*e^k}, with standard basis {e^k}, and A*^k = sgn(b[k])*A[k].
 * The signs of b[k] is stored and in the tableau the absoluate value of b is used instead. Thus it becomes
 * [ 1 -c^t  0 ][ x ]   [ z ]
 * [           ][   ] = [   ]
 * [ 0   A*  J ][ s ]   [|b|]
 * 
 * The tableau can only be changed by swapping the basis through pivoting operation. After swapping a basis,
 * the column of the enter variable will be one of the standard basis, and the leaving variable change from 
 * the same standard basis, which can be conceptually swapped s.t. J can be maintained. Such swapping is only
 * possible when the pivot value p is chosen s.t. sgn(p) = sgn(b[k]), thus the elimination drive the column
 * in A*^k into sgn(b[k])*e^k.
 * 
 * Thus not all entries are necessary. This implementation keeps the following form:
 * [ 0 -c^t ]
 * [|b|  A* ]
 * 
 * @author Y.K. Chan
 */
public class Tableau {
    
    /**
     * Create the tableau of the linear programming problem max c^t * x s.t. A*x &lt;= b.
     * @param c  Column vector c
     * @param a  Constraint matrix A
     * @param b  Constraint boundary b
     * @return  Problem in tableau form [0 -c^t;b A]
     */
    public static Tableau of(Matrix c, Matrix a, Matrix b) {
        Throw.when()
                .isNull(() -> c, () -> "Missing objective function.")
                .isNull(() -> a, () -> "Missing constraint matrix. (A in A*x <= b)")
                .isNull(() -> b, () -> "Missing constraint criteria. (b in A*x <= b)")
                .isFalse(() -> c.getColCount() == 1, 
                         () -> "Expected objective function (c) as a column/row vector.")
                .isFalse(() -> b.getColCount() == 1,
                         () -> "Expected constraint criteria (b) as a column/row vector.")
                .isFalse(() -> a.getRowCount() == b.getRowCount(), 
                         () -> "Dimension mismatch on constraint matrix and constraint criteria.")
                .isFalse(() -> c.getRowCount() == a.getColCount(),
                         () -> "Dimension mismatch on constraint matrix and objective function.");
        return new Tableau(pack(c, a, b), getSigns(b));
    }
    
    /**
     * Constructor.
     * @param matrix  Container for tableau entries.
     * @param signs  Signs of [+ b].
     */
    protected Tableau(Matrix matrix, int[] signs) {
        this.matrix = matrix;
        this.signs = signs;
    }
    
    /**
     * Get number of columns which is the number of constraints + 1.
     * @return Number of rows
     */
    public int getRowCount() {
        return this.matrix.getRowCount();
    }
    
    /**
     * Get the number of columns which is the dimension of x + 1.
     * @return  Number of columns
     */
    public int getColCount() {
        return this.matrix.getColCount();
    }
    
    /**
     * Get the sign of [+ b]. An addition [+] is padded to match the
     * number of rows of the tableau.
     * @param i  Index of [+ b]
     * @return  1 if i is 0 or b[i - 1] is positive or zero, -1 otherwise.
     */
    public int getSign(int i) {
        return this.signs[i];
    }
    
    /**
     * Get entry value of the tableau form [0 -c^t;b A].
     * @param i
     * @param j
     * @return 
     */
    public double get(int i, int j) {
        return this.matrix.get(i, j);
    }
    
    /**
     * Swap the basis.
     * @param leave  Leave row
     * @param enter  Enter column
     * @return  This
     */
    public Tableau swapBasis(int leave, int enter) {
        int sign = this.signs[leave];
        double[] pivot = this.matrix.getRow(leave);
        if(sign * pivot[enter] < 0.0){
            throw new UnsupportedOperationException();
        }
        for(int i = 0; i < this.matrix.getRowCount(); i++){
            if(i == leave){
                continue;
            }
            this.matrix.getAndSet(i, (r) -> this.eliminate(r, pivot, enter, sign));
        }
        this.normalize(pivot, enter, sign);
        this.matrix.setRow(leave, pivot).set(0, 0, 0.0);
        return this;
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

    /**
     * Get the tableau entries as a matrix. For testing purposes only.
     * @return  Tableau entries
     */
    protected Matrix getMatrix() {
        return ImmutableMatrix.of(matrix);
    }
    
    private int[] signs;
    private Matrix matrix;
    
    /**
     * Pack the linear programming problem max c^t * x s.t. A*x &lt;= b. into a matrix.
     * @param c  Column vector c
     * @param a  Constraint matrix A
     * @param b  Constraint boundary b
     * @return  Matrix which has the form [0 -c^t;b A]
     */
    protected static Matrix pack(Matrix c, Matrix a, Matrix b) {
        Matrix matrix = Matrices.zeros(1 + a.getRowCount(), 1 + c.getRowCount());
        matrix.getAndSet(0, (r) -> invertVector(c, r));
        for(int i = 1; i < matrix.getRowCount(); i++){
            int j = i - 1;
            matrix.getAndSet(i, (r) -> {
                r[0] = b.get(j, 0);
                if(r[0] >= 0.0){
                    System.arraycopy(a.getRow(j), 0, r, 1, a.getColCount()); 
                }else{
                    r[0] = Math.abs(r[0]);
                    negate(a.getRow(j), r);                    
                }
            });
        }
        return matrix;
    }
    
    /**
     * Copy the negated column vector into an array, i.e. c -&gt; [0 -c^t].
     * @param vector  Column vector c
     * @param array  Array instance
     * @return  [0 -c^t]
     */
    protected static double[] invertVector(Matrix vector, double[] array) {
        if(vector instanceof ColumnVector){            
            return negate(((ColumnVector)vector).getVector(), array);
        }
        for(int i = 0; i < array.length; i++){
            array[i] = -vector.get(i, 0);
        }
        return array;
    }
    
    /**
     * Copy the negated array into another array, i.e. v -&gt; [. -v].
     * @param source  Source array
     * @param dest  Destination array
     * @return  [. -v]
     */
    protected static double[] negate(double[] source, double[] dest) {
        for(int i = 1; i < dest.length; i++){
            dest[i] = -source[i - 1];
        }
        return dest;
    } 
    
    /**
     * Get the signs of a column vector, with a 1 prepended.
     * @param b  Column vector
     * @return  [1 sgn(b)]
     */
    protected static int[] getSigns(Matrix b) {
        return IntStream.range(0, b.getRowCount() + 1)
                .map((i) -> i == 0 ? 1 : (int) Math.signum(b.get(i - 1, 0)) )
                .map((i) -> i == 0 ? 1 : i)
                .toArray();
    }
}
