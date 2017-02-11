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
