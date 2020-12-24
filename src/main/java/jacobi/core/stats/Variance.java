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

/**
 * Compute variance of each column of a matrix.
 * 
 * <p>It is common to represent a dataset as a matrix, with a column as a random
 * variable and each row as a data point. In this case this class computes
 * the variance for each random variable.</p>
 * 
 * <p>The variance here is the biased variance, given by the formula</p>
 * 
 * <p>var(X) = Sum{(x[i] - u)^2} / n, where u is the mean and n is number of data.</p>
 * 
 * <p>A biased variance is chosen since it's easier to understand and simplier
 * to convert to un-biased variance if necessary.</p>
 * 
 * @author Y.K. Chan
 */
public class Variance {
    
    /**
     * Compute the standard deviation, which is square root of variance.
     */
    public static class StdDev {

        /**
         * Constructor.
         */
        public StdDev() {
            this.varImpl = new Variance();
        }

        /**
         * Compute the variance of each columns of the matrix.
         * @param matrix  Input matrix
         * @return  Variance value of each columns
         */
        public double[] compute(Matrix matrix) {
            Throw.when().isNull(() -> matrix, () -> "No matrix to compute.");
            if(matrix.getRowCount() == 0){
                return new double[0];
            }
            double[] var = this.varImpl.compute(matrix);
            for(int i = 0; i < var.length; i++){
                var[i] = Math.sqrt(var[i]);
            }
            return var;
        }
        
        private Variance varImpl;
    }
    
    /**
     * Compute the variance of each columns of the matrix.
     * @param matrix  Input matrix
     * @return  Variance value of each columns
     */
    public double[] compute(Matrix matrix) {
        return this.compute(matrix, new RowReduce.Mean().compute(matrix));
    }
    
    /**
     * Compute the variance of each columns of the matrix given the mean
     * @param matrix  Input matrix
     * @param mean  Mean of each columns
     * @return  Variance value of each columns
     */
    public double[] compute(Matrix matrix, double[] mean) {
    	Throw.when().isNull(() -> matrix, () -> "No matrix to compute.");
        if(matrix.getRowCount() == 0){
            return new double[0];
        }
        
        double[] var = this.serial(matrix, 0, matrix.getRowCount(), mean);
        for(int i = 0; i < var.length; i++){
            var[i] /= matrix.getRowCount();
        }
        return var;
    }

    /**
     * Compute the variance of each columns in given row range and the mean.
     * @param matrix  Input matrix
     * @param begin  Begin of row of interest
     * @param end  End of row of interest
     * @param mean  Mean of each columns
     * @return  Variance value of each columns
     */
    protected double[] serial(Matrix matrix, int begin, int end, double[] mean) {
        double[] sum = new double[mean.length];
        for(int i = begin; i < end; i++){
            double[] row = matrix.getRow(i);
            for(int j = 0; j < row.length; j++){
                double delta = row[j] - mean[j];
                sum[j] += delta * delta;
            }
        }
        return sum;
    }
    
}
