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
package jacobi.core.op;

import jacobi.api.Matrix;
import jacobi.api.annotations.Immutate;
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
    @Immutate
    public static class Add extends RowBased {

        public Add() {
            super((a, b) -> a + b);
        }
        
    }
    
    /**
     * Matrix subtraction.
     */
    @Immutate
    public static class Sub extends RowBased {

        public Sub() {
            super((a, b) -> a - b);
        }
        
    }
    
    /**
     * Hadamard product (element-by-element multiplication) of matrices.
     */
    @Immutate
    public static class Hadamard extends RowBased {

        public Hadamard() {
            super((a, b) -> a * b);
        }
     
        public Matrix compute(Matrix a, double k) {
            Throw.when().isNull(() -> a, () -> "1st operand is null.");
            return super.compute(a, new Scalar(a.getRowCount(), a.getColCount(), k));
        }
    }    
}
