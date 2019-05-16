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

import java.util.function.IntFunction;

/**
 * Represents a series of scalar functions {F<sub>k</sub>: R -&lt; R}.
 * 
 * <p>Conceptually this can be think of as a higher-order function Integer -&lt; ScalarFunction.
 * However this class is benefitual where the series is long and creating individual functions
 * objects can be avoided.
 * </p>
 * 
 * @author Y.K. Chan
 *
 */
public interface FunctionSeries {
    
    /**
     * Construct a series of functions from higher-order function Integer -&lt; ScalarFunction.
     * @param funcSeries  Series of function as higher-order function
     * @return  Function series
     */
    public static FunctionSeries of(IntFunction<ScalarFunction> funcSeries) {
        return new FunctionSeries() {

            @Override
            public double valueAt(int index, double x) {
                return funcSeries.apply(index).valueAt(x);
            }

            @Override
            public double slopeAt(int index, double x) {
                return funcSeries.apply(index).slopeAt(x);
            }

            @Override
            public double convexityAt(int index, double x) {
                return funcSeries.apply(index).convexityAt(x);
            }
            
        };
    }
    
    /**
     * Value of the function in the series
     * @param index  Index of the function in the series
     * @param x  Function argument
     * @return  Value of the function
     */
    public double valueAt(int index, double x);
    
    /**
     * Slope of the function in the series, i.e. the first derivative.
     * @param index  Index of the function in the series
     * @param x  Function argument
     * @return  Slope of the function
     */
    public double slopeAt(int index, double x);
    
    /**
     * Convexity of the function in the series, i.e. the second derivative.
     * @param index  Index of the function in the series
     * @param x  Function argument
     * @return  Convexity of the function
     */
    public double convexityAt(int index, double x);

}
