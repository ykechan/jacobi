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

package jacobi.core.prop;

import jacobi.api.Matrix;
import jacobi.core.decomp.gauss.AbstractElementaryOperatorDecor;
import jacobi.core.decomp.gauss.ElementaryOperator;
import jacobi.core.decomp.gauss.GenericGaussianElim;

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
        switch(matrix.getRowCount()){
            case 0 :
                return 0.0;
            case 1 :
                return this.compute1x1(matrix);
            case 2 :
                return this.compute2x2(matrix);
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
     * Find the determinant of the input 1x1 matrix A.
     * @param m  Input 1x1 matrix
     * @return  det(A)
     */
    protected double compute1x1(Matrix m) {        
        return m.get(0, 0);
    }
    
    /**
     * Find the determinant of the input 2x2 matrix A.
     * @param m  Input 2x2 matrix
     * @return  det(A)
     */
    protected double compute2x2(Matrix m) { 
        double[] u = m.getRow(0);
        double[] v = m.getRow(1);
        return u[0]*v[1] - v[0]*u[1];
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
