/*
 * Copyright (C) 2016 Y.K. Chan
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package jacobi.core.stats;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.api.ext.Op;

/**
 * Computation of covariance matrix.
 * 
 * A covariance matrix of a m-by-n matrix A, is a n-by-n matrix V, that an
 * entry v[i, j] in V is the covariance of column i and column j in A. 
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
