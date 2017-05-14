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
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * Implementation of the Tableau structure.
 * 
 * The LP problem max c^t * x s.t. A*x &lt;= b, x &gt;= 0 can be expressed as 
 * [ c^t  0 ][ x ]   [ z ]
 * [        ][   ] = [   ]
 * [  A   I ][ s ]   [ b ]
 * 
 * If b &gt;= 0, the trivial solution [0 b] is feasible. However if some b[k] &lt; 0, [0 b] is not feasible.
 * In this case, an auxiliary scalar variable is added s.t. [A I -1]*[x s t] = b, thus [0 s |min(b)|] is feasible. 
 * In such cases, the auxiliary problem:
 * 
 * min t -&gt; max -t s.t. [A I t]*[x s t] = b, need to be solved first.
 * 
 * The auxiliary problem can be expressed as 
 * 
 * [  A  I -1 ][ x ]    [ z ]
 * [ c^t 0 -1 ][ s ] =  [ b ]
 * [  0  0 -1 ][ t ]    [ 0 ]
 * 
 * Internally, this class keeps the tableau in following format
 * [  A   -1 b ]
 * [ c^t  -1 0 ]
 * [  0   -1 0 ]
 * 
 * Notice that for each pivoting operation, a non-basic column in the tableau is drives to a e^k, and a e^k in I
 * is mutated to a non-basic column, where e^k is some standard basis. A swap of column and be done and I is maintained.
 * The swapping of variables is kept by a mapping from column index to variable index.
 * 
 * When the auxiliary problem is solved, the tableau can be collapsed into the standard LP 
 * [  A  b ]
 * [ c^t 0 ]
 * 
 * @author Y.K. Chan
 */
public class MutableTableau implements Tableau {
    
    /**
     * Start constructing a tableau with builder.
     * @param aux  True if auxiliary row/column needed, false otherwise.
     * @return  Helper builder for constructing tableau.
     */
    public static Using build(boolean aux) { 
        return (p) -> (c, a, b) -> {
            int[] vars = IntStream.range(0, a.getRowCount() + a.getColCount() + (aux ? 1 : 0)).toArray();            
            return new MutableTableau(pack(c, a, b, aux), vars, p, aux);
        };
    }

    /**
     * Constructor.
     * @param matrix  Internal matrix
     * @param vars  Hash array of column index to variable index
     * @param pivoting  Implementation of pivoting operation
     * @param isAux  True if auxiliary row/column needed, false otherwise
     */
    protected MutableTableau(Matrix matrix, int[] vars, Pivoting pivoting, boolean isAux) {
        this.matrix = matrix;
        this.vars = vars;
        this.pivoting = pivoting;
        this.isAux = isAux;
    }

    @Override
    public Matrix getMatrix() {
        return new ImmutableMatrix() {

            @Override
            public int getRowCount() {
                return rowCount;
            }

            @Override
            public int getColCount() {
                return matrix.getColCount();
            }

            @Override
            public double[] getRow(int index) {
                return Arrays.copyOf(matrix.getRow(index), matrix.getColCount());
            }
            
            private int rowCount = matrix.getRowCount() - (isAux ? 2 : 1) ;
        };
    }

    @Override
    public double[] getCoeff() {
        return Arrays.copyOf(matrix.getRow(matrix.getRowCount() - 1), matrix.getColCount() - 1);
    }

    @Override
    public int[] getVars() {
        return Arrays.copyOf(this.vars, this.vars.length);
    }
    
    /**
     * Get entry of the coefficient of the objective function c^t * x.
     * @param i  Index of the coefficient
     * @return  Value of the coefficient
     */
    public double getCoeff(int i) {
        return matrix.get(matrix.getRowCount() - 1, i);
    }
    
    /**
     * Pivot on a select element.
     * @param i  Row index of pivot
     * @param j  Column index of pivot
     * @return  This object.
     */
    public MutableTableau pivot(int i, int j) {
        this.pivoting.perform(this.matrix, i, j);
        int padded = isAux ? 2 : 1;
        int enter = j == this.matrix.getColCount() - padded ? this.vars.length - 1 : j;
        
        if(i >= this.matrix.getRowCount() - padded){
            throw new IllegalArgumentException("No corresponding exit variable for " + i);
        }
        int leave = this.matrix.getColCount() + i - padded; 
        int temp = this.vars[enter];
        this.vars[enter] = this.vars[leave];
        this.vars[leave] = temp;
        return this;
    }
    
    /**
     * Dropping the auxiliary row and column to create a new tableau.
     * @return  A new tableau without the auxiliary row and column, or empty if auxiliary variable is basic.
     * @throws  UnsupportedOperationException if this tableau is not auxiliary
     */
    public Optional<MutableTableau> collapse() {
        if(!isAux){
            throw new UnsupportedOperationException("Tableau is not an auxiliary tableau.");
        }
        int auxVar = this.matrix.getRowCount() - 2 + this.matrix.getColCount() - 1 - 1;
        int auxIdx = IntStream.range(0, this.vars.length).filter((i) -> this.vars[i] == auxVar)
                .reduce((a, b) -> {
                    throw new IllegalStateException("Variable index corrupted.");
                })
                .orElseThrow(() -> new IllegalStateException("Auxiliary variable not found."));
        if(auxIdx >= this.matrix.getColCount() - 2){
            return Optional.empty();
        }
        Matrix tab = Matrices.zeros(this.matrix.getRowCount() - 1, this.matrix.getColCount() - 1);
        for(int i = 0; i < tab.getRowCount(); i++){
            double[] row = matrix.getRow(i);
            tab.getAndSet(i, (r) -> {
                System.arraycopy(row, 0, r, 0, r.length); 
                r[auxIdx] = row[row.length - 2];
                r[r.length - 1] = row[row.length - 1];
            });
        }
        int[] newVars = Arrays.copyOf(this.vars, this.vars.length - 1);
        newVars[auxIdx] = this.vars[this.vars.length - 1];
        return Optional.of(new MutableTableau(tab, newVars, pivoting, false));
    }    
    
    /**
     * Pack the linear programming problem max c^t * x s.t. A*x &lt;= b. into a matrix.
     * @param c  Column vector c
     * @param a  Constraint matrix A
     * @param b  Constraint boundary b
     * @param isAux  True for constructing auxiliary problem, false otherwise
     * @return  Matrix which has the form [A b; -c^t 0] or [A -1 b; -c^t -1 0] for auxiliary problem
     */
    private static Matrix pack(Matrix c, Matrix a, Matrix b, boolean isAux) {
        validate(c, a, b);
        Matrix mat = Matrices.zeros((isAux ? 2 : 1) + a.getRowCount(), (isAux ? 2 : 1) + c.getRowCount());        
        for(int i = 0; i < a.getRowCount(); i++){
            int j = i;
            mat.getAndSet(i, (r) -> {
                System.arraycopy(a.getRow(j), 0, r, 0, a.getColCount()); 
                if(isAux){
                    r[a.getColCount()] = -1.0; 
                    r[a.getColCount() + 1] = b.get(j, 0); 
                }else{
                    r[a.getColCount()] = b.get(j, 0); 
                }
            });
        }
        mat.getAndSet(a.getRowCount(), (r) -> invertVector(c, r) );
        if(isAux){
            mat.getAndSet(a.getRowCount() + 1, (r) -> r[r.length - 2] = -1.0);
        }
        return mat;
    }
    
    /**
     * Validate the parameter for the linear programming problem.
     * @param c  Objective coefficient c
     * @param a  Constraint matrix A
     * @param b  Constraint boundary b
     */
    private static void validate(Matrix c, Matrix a, Matrix b) {
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
     * Copy the negated column vector into an array, i.e. c -&gt; [c^t 0] or [c^t 0 0].
     * @param vector  Column vector c
     * @param array  Array instance
     * @param isAux  True if auxiliary column need, false otherwise
     * @return  [c^t 0]
     */
    private static double[] invertVector(Matrix vector, double[] array) {
        if(vector instanceof ColumnVector){ 
            System.arraycopy(((ColumnVector) vector).getVector(), 0, array, 0, vector.getRowCount());
            return array;
        }
        for(int i = 0; i < vector.getRowCount(); i++){
            array[i] = vector.get(i, 0);
        }       
        return array;        
    }
    
    
    private Matrix matrix;
    private int[] vars;
    private Pivoting pivoting;
    private boolean isAux;

    /**
     * Common interface for Pivoting operation.
     * 
     * Pivoting operation for simplex algorithm is essentially a change of column basis on the tableau
     * [ 1 -c^t  0 ][ x ]   [ z ]
     * [           ][   ] = [   ]
     * [ 0    A  I ][ s ]   [ b ]
     * 
     * This can be done by performing elementary row operations on the tableau.
     * 
     * Note that the row and column index refers to the index of the given matrix. This class is 
     * oblivious to the actual format of the tableau. It should work whether it is given a full tableau
     * [ 1 -c^t 0 | z]                      [   A   b ]
     * [ 0   A  I | b], or compact tableau  [ -c^t  0 ], or with auxiliary variable.
     */
    public interface Pivoting {
        
        /**
         * Perform pivoting operation on given pivot.
         * @param matrix  Internal matrix of the tableau
         * @param row  Row index of the pivot
         * @param col  Column index of the pivot
         */
        public void perform(Matrix matrix, int row, int col);
        
    }
    
    /**
     * Builder helper interface that accepts the Pivoting operation implementation.
     */
    public interface Using { 
        
        /**
         * Accept pivoting operation in constructing the Tableau.
         * @param pivoting  Implementation of pivoting operation.
         * @return  Builder of tableau
         */
        public Builder use(Pivoting pivoting);
        
    }
    
    /**
     * Builder interface that accepts the linear programming problem maximize c^t * x s.t. A*x &lt; b.
     */
    public interface Builder {
        
        /**
         * Construct the Tableau.
         * @param c  Coefficient of objective function
         * @param a  Constraint matrix A
         * @param b  Constraint boundary b
         * @return  Resultant tableau
         */
        public MutableTableau of(Matrix c, Matrix a, Matrix b);
        
    }
    
}
