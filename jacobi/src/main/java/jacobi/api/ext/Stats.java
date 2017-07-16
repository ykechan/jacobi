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
import jacobi.core.stats.Covar;
import jacobi.core.stats.HigherMoment;
import jacobi.core.stats.RowReduce;
import jacobi.core.stats.Variance;

/**
 * Extension for statistical properties, i.e.&nbsp;mean, variance, standard deviation
 * etc.
 * 
 * @author Y.K. Chan
 */
@Immutate
@Facade
public interface Stats {
    
    /**
     * Find the maximum element for each columns.
     * @return  Maximum elements of every columns.
     */
    @Implementation(RowReduce.Max.class)
    public double[] max();
    
    /**
     * Find the minimum element for each columns.
     * @return  Minimum elements of every columns.
     */
    @Implementation(RowReduce.Min.class)
    public double[] min();
    
    /**
     * Find the mean value for each columns.
     * @return  Mean values of every columns.
     */
    @Implementation(RowReduce.Mean.class)
    public double[] mean();
    
    /**
     * Find the biased variance for each columns.
     * @return  Biased variance of every columns.
     */
    @Implementation(Variance.class)
    public double[] var();
    
    /**
     * Find the biased standard deviation for each columns.
     * @return  Biased standard deviation of every columns.
     */
    @Implementation(Variance.StdDev.class)
    public double[] stdDev();
    
    /**
     * Find the biased skewness for each columns
     * @return  Biased skewness of every columns
     */
    @Implementation(HigherMoment.Skewness.class)
    public double[] skew();
    
    /**
     * Find the biased kurtosis for each columns
     * @return  Biased kurtosis of every columns
     */
    @Implementation(HigherMoment.Kurtosis.class)
    public double[] kurt();
    
    /**
     * Find the covariance matrix between every columns.
     * @return  Covariance matrix between every columns.
     */
    @Implementation(Covar.class)
    public Matrix covar();        
    
}
