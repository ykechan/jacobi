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

import jacobi.api.Matrices;
import jacobi.api.Matrix;

/**
 * Computation of covariance matrix.
 * 
 * <p>A covariance matrix of a m-by-n matrix A, is a n-by-n matrix V, that an
 * entry v[i, j] in V is the covariance of column i and column j in A. </p>
 * 
 * @author Y.K. Chan
 */
public class Covar {

    /**
     * Constructor.
     */
    public Covar() {
        this.meanFunc = new RowReduce.Mean();
    }
    
    /**
     * Compute the covariance matrix.
     * @param matrix  Input matrix A.
     * @return  Covariance matrix V.
     */
    public Matrix compute(Matrix matrix) {
        double[] mean = this.meanFunc.compute(matrix);
        Matrix cov = Matrices.zeros(mean.length);
        this.serial(matrix, 0, matrix.getRowCount(), mean, cov);
        this.normalize(cov, matrix.getRowCount());
        
        // copy upper-trangular entries        
        for(int i = 1; i < cov.getRowCount(); i++){
            double[] row = cov.getRow(i);
            for(int j = 0; j < i; j++){
                row[j] = cov.get(j, i);
            }            
            cov.setRow(i, row);
        }
        return cov;
    }
    
    /**
     * Compute the covariance matrix of a range of rows, and add it to 
     * pre-existing result.
     * @param matrix  Input matrix A
     * @param begin  Index of the beginning of rows of interest
     * @param end  Index of the end of rows of interest
     * @param mean  Mean vector
     * @param cov  Pre-existing covariance matrix
     * @return  Updated pre-existing covariance matrix
     */
    protected Matrix serial(Matrix matrix, int begin, int end, double[] mean, Matrix cov) {
        double[] dist = new double[mean.length];
        for(int k = begin; k < end; k++){
            double[] row = matrix.getRow(k);
            for(int i = 0; i < dist.length; i++){
                dist[i] = row[i] - mean[i];
            }
            for(int i = 0; i < dist.length; i++){
                double[] covRow = cov.getRow(i);
                for(int j = i; j < dist.length; j++){
                    covRow[j] += dist[i] * dist[j];
                }
                cov.setRow(i, covRow);
            }
        }
        return cov;
    }
    
    /**
     * Normalize over the number of population for upper-triangular part of the covariance matrix
     * @param cov  Covariance matrix
     * @param length  Number of population
     * @return  cov
     */
    protected Matrix normalize(Matrix cov, int length) {
        for(int i = 0; i < cov.getRowCount(); i++){
            double[] row = cov.getRow(i);
            for(int j = i; j < row.length; j++){
                row[j] /= length;
            }
            cov.setRow(i, row);
        }
        return cov;
    }

    private RowReduce.Mean meanFunc;    
}
