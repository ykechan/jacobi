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
 * Data object for a triplet of matrices &lt;U, E, V&gt;, , usually representing U * E * V.
 * 
 * @author Y.K. Chan
 */
public interface Triplet {
   
    /**
     * Get the left matrix U in &lt;U, E, V&gt;
     * @return Left matrix U
     */
    public Matrix getLeft();
    
    /**
     * Get the middle matrix E in &lt;U, E, V&gt;
     * @return Middle matrix E
     */
    public Matrix getMiddle();
    
    /**
     * Get the right matrix V in &lt;U, E, V&gt;
     * @return right matrix V
     */
    public Matrix getRight();
    
    /**
     * Construct a triplet of matrices &lt;U, E, V&gt;
     * @param u  Matrix U
     * @param e  Matrix E
     * @param v  Matrix V
     * @return  Triplet of matrices
     */
    public static Triplet of(Matrix u, Matrix e, Matrix v) {
        return new Triplet() {

            @Override
            public Matrix getLeft() {
                return u;
            }

            @Override
            public Matrix getMiddle() {
                return e;
            }

            @Override
            public Matrix getRight() {
                return v;
            }
            
        };
    }
    
    /**
     * Construct a triplet of matrices &lt;U, E, V&gt;, which V is computed lazily.
     * @param u  Matrix U
     * @param e  Matrix E
     * @param v  Supplier of Matrix V
     * @return  Triplet of matrices
     */
    public static Triplet of(Matrix u, Matrix e, Supplier<Matrix> v) {
        return new Triplet() {

            @Override
            public Matrix getLeft() {
                return u;
            }

            @Override
            public Matrix getMiddle() {
                return e;
            }

            @Override
            public Matrix getRight() {
                return this.matrixV == null ? this.matrixV = v.get() : this.matrixV;
            }
            
            private Matrix matrixV;
        };
    }
}
