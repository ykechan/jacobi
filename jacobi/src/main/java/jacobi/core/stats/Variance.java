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

import jacobi.api.Matrix;

/**
 * Compute variance of each column of a matrix.
 * 
 * It is common to represent a dataset as a matrix, with a column as a random
 * variable and each row as a data point. In this case this class computes
 * the variance for each random variable.
 * 
 * The variance here is the biased variance, given by the formula
 * 
 * var(X) = Sum{(x[i] - u)^2} / n, where u is the mean and n is number of data.
 * 
 * A biased variance is chosen since it's easier to understand and simplier
 * to convert to un-biased variance if necessary.
 * 
 * @author Y.K. Chan
 */
public class Variance {
    
    /**
     * Compute the standard deviation, which is square root of variance.
     */
    public static class StdDev extends Variance {

        @Override
        public double[] compute(Matrix matrix) {
            double[] var = super.compute(matrix);
            for(int i = 0; i < var.length; i++){
                var[i] = Math.sqrt(var[i]);
            }
            return var;
        }
        
    }
    
    public double[] compute(Matrix matrix) {
        double[] mean = new RowReduce.Mean().compute(matrix);
        double[] var = this.serial(matrix, 0, matrix.getRowCount(), mean);
        for(int i = 0; i < var.length; i++){
            var[i] /= matrix.getRowCount();
        }
        return var;
    }    

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
