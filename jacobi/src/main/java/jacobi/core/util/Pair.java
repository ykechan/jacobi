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

package jacobi.core.util;

import jacobi.api.Matrix;
import java.util.function.Supplier;

/**
 * Data object for a pair of matrices &lt;A, B&gt;, usually representing A * B.
 * 
 * @author Y.K. Chan
 */
public interface Pair {
    
    /**
     * Get matrix A
     * @return  Matrix A
     */
    public Matrix getLeft();
    
    /**
     * Get matrix B
     * @return  Matrix B
     */
    public Matrix getRight();
    
    /**
     * Create a pair of matrices &lt;A, B&gt;
     * @param a  Matrix A
     * @param b  Matrix B
     * @return  A pair of matrices
     */
    public static Pair of(Matrix a, Matrix b) {
        return new Pair(){

            @Override
            public Matrix getLeft() {
                return a;
            }

            @Override
            public Matrix getRight() {
                return b;
            }
            
        };
    }
    
    /**
     * Create a pair of matrices &lt;A, B&gt;, which B is computed lazily.
     * @param a  Matrix A
     * @param b  Supplier of Matrix B
     * @return  A pair of matrices
     */
    public static Pair of(Matrix a, Supplier<Matrix> b) {
        return new Pair() {

            @Override
            public Matrix getLeft() {
                return a;
            }

            @Override
            public Matrix getRight() {
                return this.right == null ? this.right = b.get() : this.right;
            }
            
            private Matrix right;
        };
    }
    
    /**
     * Create a pair of matrices &lt;A, B&gt;, which A is computed lazily.
     * @param a  Supplier of Matrix A
     * @param b  Matrix B
     * @return  A pair of matrices
     */
    public static Pair of(Supplier<Matrix> a, Matrix b) {
        return new Pair() {

            @Override
            public Matrix getLeft() {
                return this.left == null ? this.left = a.get() : this.left;
            }

            @Override
            public Matrix getRight() {
                return b;
            }
            
            private Matrix left;
        };
    }
}
