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
package jacobi.core.op;

import java.util.function.DoubleBinaryOperator;

/**
 * Functional interface for computing two rows of values and output to 
 * another row.
 * 
 * @author Y.K. Chan
 * @param <T>  Computation result, if any.
 */
@FunctionalInterface
public interface RowOperation<T> {
    
    /**
     * Row operation composed of element-to-element function performing to
     * each element of the row values.
     * @param op  Element-to-element function.
     * @return  Row operation representation
     */
    public static RowOperation<Void> forEach(DoubleBinaryOperator op) {
        return (u, v, w) -> {
            for(int i = 0; i < u.length; i++){
                w[i] = op.applyAsDouble(u[i], v[i]);
            }
            return null;
        };
    }
    
    /**
     * Perform operation.
     * @param u  row values of 1st operand
     * @param v  row values of 2nd operand
     * @param w  output row buffer
     * @return  Computation result, if any.
     */
    public T apply(double[] u, double[] v, double[] w);
    
}
