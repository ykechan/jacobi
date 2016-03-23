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

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.core.util.Triplet;

/**
 * 
 * This class decomposes a matrix A into P * E * U where E is the product of a 
 * series of elementary matrix, and U is upper triangular, and P is permutation
 * matrix.
 * 
 * 
 * @author Y.K. Chan
 */
public class GaussianDecomp {
    
    public Triplet compute(Matrix matrix) {
        return null;
    }
    

    private static class Memento extends AbstractElementaryOperatorDecor {

        public Memento(ElementaryOperator op) {
            super(op);
            this.perm = new Permutation(op.getMatrix().getRowCount());
            this.lower = Matrices.identity(this.perm.getRowCount());
        }
        
        public Permutation getPermutation() {
            return this.perm;
        }

        public Matrix getLower() {
            return lower;
        }

        @Override
        public void swapRows(int i, int j) {
            super.swapRows(i, j);
            if(i != j){
                this.perm.swapRow(i, j);
                this.swapLower(i, j);
            }            
        }

        @Override
        public void rowOp(int i, double a, int j) {            
            super.rowOp(i, a, j);
            double[] row = this.lower.getRow(i);
            row[j] = a;
            this.lower.setRow(i, row);
        }
        
        private void swapLower(int i, int j) {
            int n = Math.min(i, j);
            double[] u = this.lower.getRow(i);
            double[] v = this.lower.getRow(j);
            
            for(int k = 0; k < n; k++){
                double temp = u[k];
                u[k] = v[k];
                v[k] = temp;
            }
            this.lower.setRow(i, u).setRow(j, v);
        }
        
        private Permutation perm;
        private Matrix lower;
    }
}
