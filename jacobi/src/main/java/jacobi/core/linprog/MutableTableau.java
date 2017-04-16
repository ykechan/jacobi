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
import jacobi.core.util.IntArray;
import jacobi.core.util.Throw;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * Tableau structure that is mutable.
 * 
 * This class is mainly responsible for constructing the tableau from the LP problem.
 * 
 * The tableau can only be mutated by the pivoting operation implementation.
 * 
 * Pivoting operation for simplex algorithm is essentially a change of column basis on the tableau
 * [ 0 -c^t 0 ]
 * [          ], where I is an identity matrix
 * [ b   A  I ]
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
public class MutableTableau implements Tableau {
    
    /**
     * Create a factory of tableau accepting an implementation of pivoting operation.
     * @param c  Objective coefficient 
     * @param a  Constraint matrix
     * @param b  Constraint boundary
     * @return  Factory of tableau
     */
    public static final Function<Pivoting, MutableTableau> of(Matrix c, Matrix a, Matrix b) {
        return (f) -> new MutableTableau(c, a, b, f);
    }
    
    /**
     * Constructor.
     * @param c  Objective coefficient 
     * @param a  Constraint matrix
     * @param b  Constraint boundary
     * @param pivoting  Implementation of pivoting operation
     */
    public MutableTableau(Matrix c, Matrix a, Matrix b, Pivoting pivoting) {        
        this.matrix = this.pack(c, a, b);
        this.vars = IntStream.range(0, a.getColCount() + a.getRowCount()).toArray();
        this.pivoting = pivoting;
        
        this.immutableMatrix = ImmutableMatrix.of(this.matrix);
        this.immutableSigns = new IntArray(this.getSigns(b));
        this.immutableVars = new IntArray(this.vars);
    }

    @Override
    public Matrix getMatrix() {
        return this.immutableMatrix;
    }

    @Override
    public IntArray getSigns() {
        return this.immutableSigns;
    }

    @Override
    public IntArray getVars() {
        return this.immutableVars;
    }
    
    /**
     * Swap a basic and non-basic column basis. 
     * @param row  Row index of the basic column basis in J
     * @param col  Column index of the non-basic column basis
     */
    public void swapBasis(int row, int col) {
        int sign = this.getSigns().get(row);
        if(sign * this.matrix.get(row, col) < 0.0){
            throw new IllegalArgumentException();
        }
        this.pivoting.run(this.matrix, row, col); 
        this.matrix.set(0, 0, 0.0);
        this.swapVars(col - 1, this.matrix.getColCount() + row - 2);
    }
    
    /**
     * Swap two variable indices.
     * @param i  First variable index
     * @param j  Second variable index
     */
    protected void swapVars(int i, int j) {
        int temp = this.vars[i];
        this.vars[i] = this.vars[j];
        this.vars[j] = temp;
    }
    
    /**
     * Validate the parameter for the linear programming problem.
     * @param c  Objective coefficient c
     * @param a  Constraint matrix A
     * @param b  Constraint boundary b
     */
    private void validate(Matrix c, Matrix a, Matrix b) {
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
    }
    
    /**
     * Pack the linear programming problem max c^t * x s.t. A*x &lt;= b. into a matrix.
     * @param c  Column vector c
     * @param a  Constraint matrix A
     * @param b  Constraint boundary b
     * @return  Matrix which has the form [0 -c^t;b A]
     */
    private Matrix pack(Matrix c, Matrix a, Matrix b) {
        this.validate(c, a, b);
        Matrix mat = Matrices.zeros(1 + a.getRowCount(), 1 + c.getRowCount());
        mat.getAndSet(0, (r) -> invertVector(c, r));
        for(int i = 1; i < mat.getRowCount(); i++){
            int j = i - 1;
            mat.getAndSet(i, (r) -> {
                r[0] = b.get(j, 0);
                if(r[0] >= 0.0){
                    System.arraycopy(a.getRow(j), 0, r, 1, a.getColCount()); 
                }else{
                    r[0] = Math.abs(r[0]);
                    negate(a.getRow(j), r);                    
                }
            });
        }
        return mat;
    }
    
    /**
     * Copy the negated column vector into an array, i.e. c -&gt; [0 -c^t].
     * @param vector  Column vector c
     * @param array  Array instance
     * @return  [0 -c^t]
     */
    private double[] invertVector(Matrix vector, double[] array) {
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
    private double[] negate(double[] source, double[] dest) {
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
    private int[] getSigns(Matrix b) {
        return IntStream.range(0, b.getRowCount() + 1)
                .map((i) -> i == 0 ? 1 : (int) Math.signum(b.get(i - 1, 0)) )
                .map((i) -> i == 0 ? 1 : i)
                .toArray();
    }
    
    private Matrix matrix;
    private int[] vars;
    
    private Matrix immutableMatrix;
    private IntArray immutableSigns, immutableVars;
    
    private Pivoting pivoting;

    /**
     * Interface of pivoting operation for simplex algorithm.
     * 
     * This is essentially a change of column basis on the tableau
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
     */
    public interface Pivoting {
        
        /**
         * Perform pivoting operation.
         * @param matrix  Mutable compact tableau [0 -c^t; |b| A*].
         * @param row  Row index of leaving variable, i.e. the row s.t. the leaving column in J is non-zero.
         * @param col  Column index of enter variable
         * @throws  IllegalArgumentException if the swapping of columns cannot be done due to opposite signs.
         */
        public void run(Matrix matrix, int row, int col);
        
    }
    
}
