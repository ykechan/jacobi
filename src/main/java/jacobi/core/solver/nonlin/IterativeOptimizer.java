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

import java.util.Arrays;
import java.util.function.Supplier;

import jacobi.core.impl.ColumnVector;
import jacobi.core.util.Weighted;

/**
 * Common interface of an numerical iterative optimizer for vector functions.
 * 
 * <p>A numeric iterative optimizer starts from a starting position, and according to different
 * methods move the vector to other positions. The stopping criteria is the movement is smaller
 * than an acceptable range, or a solution is found. Normally these two criteria coincide.</p>
 * 
 * <p>The problem here is assume to be a minimization problem. For maximization problems,
 * one can always negate the vector function.</p>
 * 
 * @author Y.K. Chan
 *
 */
public interface IterativeOptimizer {
    
    /**
     * Find the optima of a given vector function.
     * @param func  Input vector function
     * @param init  Factory method for starting position
     * @param limit  Maximum number of iteration
     * @param epsilon  Error tolerance
     * @return  The position of optima found with its error value
     */
    public Weighted<ColumnVector> optimize(
        VectorFunction func, 
        Supplier<double[]> init,
        long limit, double epsilon
    );
    
}
