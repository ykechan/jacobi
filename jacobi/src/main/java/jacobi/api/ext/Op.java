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
package jacobi.api.ext;

import jacobi.api.Matrix;
import jacobi.api.annotations.Facade;
import jacobi.api.annotations.Implementation;
import jacobi.api.annotations.Immutate;
import jacobi.core.op.Dot;
import jacobi.core.op.Mul;
import jacobi.core.op.MulT;
import jacobi.core.op.Operators;
import java.util.function.Supplier;

/**
 * Extension for matrix arithmetics operations, including addition, multiplication,
 * and subtraction. 
 * 
 * This extension is a chaining interface, i.e.&nbsp;that is it supports doing multiple
 * operations and gets back the answer as an Supplier, without the need to
 * create extension every time.
 * 
 * This extension is non-perturbative, i.e.&nbsp;it preserves the value of the matrices
 * it operates upon.
 * 
 * @author Y.K. Chan
 */
@Immutate
@Facade
public interface Op extends Supplier<Matrix> {
    
    /**
     * Matrix addition, i.e.&nbsp;C = A + B
     * @param b  2nd Operand. The first operand is the extension parameter.
     * @return  Op instance with Resultant matrix
     */
    @Implementation(Operators.Add.class)
    public Op add(Matrix b);
    
    /**
     * Matrix subtraction, i.e.&nbsp;C = A - B
     * @param b  2nd Operand. The first operand is the extension parameter.
     * @return  Op instance with Resultant matrix
     */
    @Implementation(Operators.Sub.class)
    public Op sub(Matrix b);
    
    /**
     * Matrix multiplication, i.e.&nbsp;C = A * B
     * @param b  2nd Operand. The first operand is the extension parameter.
     * @return  Op instance with Resultant matrix
     */
    @Implementation(Mul.class)
    public Op mul(Matrix b);
    
    /**
     * Matrix multiplication with transpose of the 2nd operand, i.e.&nbsp;C = A * B^t
     * @param b  2nd Operand. The first operand is the extension parameter.
     * @return  Op instance with Resultant matrix
     */
    @Implementation(MulT.class)
    public Op mulT(Matrix b);
    
    /**
     * Hadamand multiplication, i.e.&nbsp;element-by-element multiplication.
     * @param b  2nd Operand. The first operand is the extension parameter.
     * @return  Op instance with Resultant matrix
     */
    @Implementation(Operators.Hadamard.class)
    public Op hadamard(Matrix b);
    
    /**
     * Matrix multiplication with a scalar. C = k * A
     * @param k  scalar operand. The first operand is the extension parameter.
     * @return  Op instance with Resultant matrix
     */
    @Implementation(Operators.Hadamard.class)
    public Op mul(double k);
    
    /**
     * Dot product with column vectors of the matrix.
     * @param b  2nd Operand. The first operand is the extension parameter.
     * @return  Op instance with Resultant matrix
     */
    @Implementation(Dot.class)
    public Op dot(Matrix b);
    
}
