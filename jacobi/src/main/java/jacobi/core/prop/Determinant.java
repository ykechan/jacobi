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
package jacobi.core.prop;

import jacobi.api.Matrix;
import jacobi.core.decomp.gauss.AbstractElementaryOperatorDecor;
import jacobi.core.decomp.gauss.ElementaryOperator;
import jacobi.core.decomp.gauss.GenericGaussianElim;
import jacobi.core.decomp.qr.step.shifts.DoubleShift;
import jacobi.core.util.Throw;

/**
 * Find the determinant of a given matrix.
 * 
 * This class has no side-effect, i.e. it copies the data for necessary
 * operation.
 * 
 * The current implementation is using Gaussian Elimination.
 * 
 * @author Y.K. Chan
 */
public class Determinant {

    /**
     * Constructor.
     */
    public Determinant() {
        this.gaussElim = new GenericGaussianElim();
    }
    
    /**
     * Find the determinant of the input matrix A.
     * @param matrix  Input matrix A
     * @return  det(A)
     */
    public double compute(Matrix matrix) {
        Throw.when()
            .isNull(() -> matrix, () -> "No matrix to compute.")
            .isFalse(
                () -> matrix.getRowCount() == matrix.getColCount(), 
                () -> "Trace not exists for non-square matrices");
        switch(matrix.getRowCount()){
            case 0 :
                return 0.0;
            case 1 :
                return matrix.get(0, 0);
            case 2 :
                return DoubleShift.of(matrix, 0).getDet();
            case 3 :
                return this.compute3x3(matrix);
            default :
                break;
        }
        double det = this.gaussElim.compute(matrix, (op) -> new Sign(op)).get();
        for(int i = 0; i < matrix.getRowCount(); i++){            
            det *= matrix.get(i, i);
        }
        return det;
    }
    
    /**
     * Find the determinant of the input 3x3 matrix A.
     * @param m  Input 3x3 matrix
     * @return  det(A)
     */
    protected double compute3x3(Matrix m) {        
        double[] u = m.getRow(0);
        double[] v = m.getRow(1);
        double[] w = m.getRow(2);
        return u[0] * (
                v[1]*w[2] - v[2]*w[1] 
               )
             - u[1] * (
                v[0]*w[2] - v[2]*w[0] 
               )
             + u[2] * (
                v[0]*w[1] - v[1]*w[0] 
               );
    }
    
    private GenericGaussianElim gaussElim;
    
    private static class Sign extends AbstractElementaryOperatorDecor {

        public Sign(ElementaryOperator op) {
            super(op);
            this.sgn = 1;
        }
        
        public int get() {
            return this.sgn;
        }

        @Override
        public void swapRows(int i, int j) {
            super.swapRows(i, j);            
            if(i != j){
                this.sgn *= -1;
            }
        }

        @Override
        public void rowOp(int i, double a, int j) {  
            super.rowOp(i, a, j);
        }
        
        private int sgn;
    }
}
