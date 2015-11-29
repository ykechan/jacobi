/*
 * Copyright (C) 2015 Y.K. Chan
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
package jacobi.core.op;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.core.util.Throw;

/**
 * Implementation classes for operators.
 * 
 * @author Y.K. Chan
 */
public class Operators {
    
    /**
     * Matrix addition.
     */
    public static class Add extends RowBased {

        public Add() {
            super((a, b) -> a + b);
        }
        
    }
    
    /**
     * Matrix subtraction.
     */
    public static class Sub extends RowBased {

        public Sub() {
            super((a, b) -> a - b);
        }
        
    }
    
    /**
     * Hadamard product (element-by-element multiplication) of matrices.
     */
    public static class Hadamard extends RowBased {

        public Hadamard() {
            super((a, b) -> a * b);
        }
        
    }
    
    /**
     * Scalar multiplication disguised (hacked) as Matrix multiplication.
     */
    public static class ScalarMul extends RowBased {
                
        public ScalarMul() {
            super((a, b, ans) -> {
                for(int i = 0; i < a.length; i++){
                    ans[i] = a[i] * b[0];
                }
                return null;
            });
        }
        
        public Matrix compute(Matrix a, double scalar) {
            Throw.when().isNull(() -> a, () -> "1st operand is null.");
            if(scalar == 0.0){
                return Matrices.zeros(a.getRowCount(), a.getColCount());
            }
            return super.compute(a, new ScalarAsMatrix(a.getRowCount(), a.getColCount(), scalar));
        }

        /**
         * Invalid usage. This is not a matrix-to-matrix operator.
         * @param a  Matrix A
         * @param b  Matrix B
         * @return  Does not return anything.
         * @throws  UnsupportedOperationException always
         */
        @Override
        public Matrix compute(Matrix a, Matrix b) {
            throw new UnsupportedOperationException("Invalid usage.");
        }
        
    }
    
    private static class ScalarAsMatrix implements Matrix {

        public ScalarAsMatrix(int m, int n, double scalar) {
            this.m = m;
            this.n = n;
            this.value = new double[]{scalar};
        }                

        @Override
        public int getRowCount() {
            return this.m;
        }

        @Override
        public int getColCount() {
            return this.n;
        }

        @Override
        public double[] getRow(int index) {
            return this.value;
        }

        @Override
        public Matrix setRow(int index, double[] values) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Matrix swapRow(int i, int j) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T ext(Class<T> clazz) {
            throw new UnsupportedOperationException();
        }
        
        private int m, n;
        private double[] value;
    }
}
