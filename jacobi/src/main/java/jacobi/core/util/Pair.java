/* 
 * The MIT License
 *
 * Copyright 2016 Y.K. Chan
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
