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

package jacobi.core.stats;

import jacobi.api.Matrix;
import jacobi.core.util.Throw;
import java.util.function.DoubleUnaryOperator;

/**
 * Common parent class for computing higher-order moments.
 * 
 * <p>Moment of order n is defined as E(|X - u|^n)/s^n, where u is the mean and s is the standard deviation.
 * In this context, the biased version is used because many variation exists, and this is the simplest and most
 * un-surprising version. If some particular adjustment is needed it can be transformed to it rather easily.</p>
 * 
 * <p>This class generalize the computation by specifying two functions f(d) where d = x - u, and g(v) where v
 * is the variance. Thus the moment is given by Sum{f(x - u)}/N*g(v).</p>
 * 
 * <p>This class is mainly for the computation of skewness and kurtosis. Lower order moments like mean and
 * standard deviation is too common to warrant their own implementation, and even higher order moments are
 * rare in practice.</p>
 * 
 * @author Y.K. Chan
 */
public class HigherMoment {
    
    /**
     * Skewness, i.e.&nbsp; 3rd order of moment.
     */
    public static class Skewness extends HigherMoment {

        /**
         * Constructor.
         */
        public Skewness() {
            super((d) -> d * d * d, (v) -> v * Math.sqrt(v));
        }
        
    }
    
    /**
     * Kurtosis, i.e.&nbsp; 4th order of moment.
     */
    public static class Kurtosis extends HigherMoment {

        /**
         * Constructor.
         */
        public Kurtosis() {
            super((d) -> d * d * d * d, (v) -> v * v);
        }
        
    }

    /**
     * Constructor.
     * @param distFunc  Function on distance from mean
     * @param varFunc  Function on variance
     */
    public HigherMoment(DoubleUnaryOperator distFunc, DoubleUnaryOperator varFunc) {
        this.distFunc = distFunc;
        this.varFunc = varFunc;
        this.varImpl = new Variance();
        this.meanImpl = new RowReduce.Mean();
    }
    
    /**
     * Compute the moment of each columns of the matrix.
     * @param matrix  Input matrix
     * @return  Moment value of each columns
     */
    public double[] compute(Matrix matrix) {
        Throw.when().isNull(() -> matrix, () -> "No matrix to compute.");
        if(matrix.getRowCount() == 0){
            return new double[0];
        }
        DoubleUnaryOperator vFunc = this.varFunc.andThen((v) -> v * matrix.getRowCount());
        double[] mean = this.meanImpl.compute(matrix);        
        double[] denom = this.apply(vFunc, this.varImpl.compute(matrix)); 
        double[] ans = this.serial(matrix, 0, matrix.getRowCount(), mean);
        for(int i = 0; i < ans.length; i++){
            ans[i] /= denom[i];
        }
        return ans;
    }
    
    /**
     * Compute the moment of each columns of the matrix in serial.
     * @param matrix  Input matrix
     * @param begin  Begin index of rows of interest
     * @param end  End index of rows of interest
     * @param mean  Mean value of each columns
     * @return  Moment value of each columns
     */
    protected double[] serial(Matrix matrix, int begin, int end, double[] mean) {
        double[] sum = new double[matrix.getColCount()];
        for(int i = begin; i < end; i++){
            double[] row = matrix.getRow(i);
            for(int j = 0; j < row.length; j++){
                sum[j] += this.distFunc.applyAsDouble(row[j] - mean[j]);
            }
        }
        return sum;
    }
    
    /**
     * Apply a double unary operator on every elements of an array.
     * @param func  Double function
     * @param array  Input array
     * @return  Updated input array
     */
    protected double[] apply(DoubleUnaryOperator func, double[] array) {
        for(int i = 0; i < array.length; i++){
            array[i] = func.applyAsDouble(array[i]);
        }        
        return array;
    }
    
    private DoubleUnaryOperator distFunc, varFunc;
    
    private Variance varImpl;
    private RowReduce.Mean meanImpl;
}
