/* 
 * The MIT License
 *
 * Copyright 2019 Y.K. Chan
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
package jacobi.core.solver.nonlin;

import jacobi.api.Matrix;
import jacobi.core.impl.ColumnVector;

/**
 * Representation of a multivariate scalar-valued function.
 * 
 * <p>In most use cases the optimal points of such function are interested instead of
 * the solution. Therefore the methods to obtain the derivatives are defined instead of
 * the function value itself.</p>
 * 
 * @author Y.K. Chan
 *
 */
public interface VectorFunction {
    
    /**
     * Gradient of the vector function at a given position.
     * @param pos  Input position
     * @return  Gradient value as a column vector
     */
    public ColumnVector grad(double[] pos);
    
    /**
     * Hessian matrix of the vector function at a given position.
     * @param pos  Input position
     * @return  Hessian matrix
     */
    public Matrix hess(double[] pos);

}
