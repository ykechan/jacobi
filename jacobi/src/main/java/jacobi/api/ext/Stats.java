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
package jacobi.api.ext;

import jacobi.api.Matrix;
import jacobi.api.annotations.Facade;
import jacobi.api.annotations.Implementation;
import jacobi.api.annotations.NonPerturbative;
import jacobi.core.stats.Covar;
import jacobi.core.stats.RowReduce;
import jacobi.core.stats.Variance;

/**
 * Extension for statistical properties, i.e. mean, variance, standard deviation
 * etc.
 * 
 * @author Y.K. Chan
 */
@NonPerturbative
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
     * Find the covariance matrix between every columns.
     * @return  Covariance matrix between every columns.
     */
    @Implementation(Covar.class)
    public Matrix covar();
    
}
