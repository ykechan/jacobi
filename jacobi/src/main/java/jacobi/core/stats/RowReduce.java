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
import jacobi.core.util.Throw;
import java.util.Arrays;
import java.util.function.BiConsumer;

/**
 * For computing vector that is from reducing of all rows in a matrix, e.g.
 * max, min, mean. 
 * 
 * @author Y.K. Chan
 */
public class RowReduce { 
    
    /**
     * Entrance class for maximum function
     */
    public static class Max extends RowReduce {

        public Max() {
            super(RowReduce::max);
        }
        
    }
    
    /**
     * Entrance class for minimum function
     */
    public static class Min extends RowReduce {

        public Min() {
            super(RowReduce::min);
        }
        
    }
    
    /**
     * Entrance class for mean function
     */
    public static class Mean extends RowReduce {

        public Mean() {
            super(RowReduce::sum);
        }

        @Override
        public double[] compute(Matrix matrix) {
            double[] ans = super.compute(matrix);
            for(int i = 0; i < ans.length; i++){
                ans[i] /= matrix.getRowCount();
            }
            return ans;
        }
        
    }

    /**
     * Constructor.
     * @param reduce   Reduce function to combine two rows
     */
    public RowReduce(BiConsumer<double[], double[]> reduce) {
        this.reduce = reduce;
    }
    
    /**
     * Compute the reduction result. 
     * @param matrix  Input matrix
     * @return  Reduction result.
     */
    public double[] compute(Matrix matrix) {
        Throw.when().isEmpty(matrix, "No matrix to compute.");
        return this.serial(matrix, 0, matrix.getRowCount());
    }
    
    /**
     * Compute the reduction result in a given range in serial.
     * @param matrix  Input matrix
     * @param begin  Index of begin of rows of interest
     * @param end  Index of end of rows of interest, exclusive
     * @return  Reduction result
     */
    protected double[] serial(Matrix matrix, int begin, int end) {
        if(end - begin < 1){
            throw new IllegalStateException();
        }
        double[] ans = Arrays.copyOf(matrix.getRow(begin), matrix.getColCount());
        for(int i = begin + 1; i < end; i++){
            this.reduce.accept(ans, matrix.getRow(i));
        }
        return ans;
    }
    
    /**
     * Maximum function to be used as lambda
     * @param u  First row
     * @param v  Second row
     */
    protected static void max(double[] u, double[] v) {
        for(int i = 0; i < u.length; i++){
            u[i] = Math.max(u[i], v[i]);
        }
    }
    
    /**
     * Minimum function to be used as lambda
     * @param u  First row
     * @param v  Second row
     */
    protected static void min(double[] u, double[] v) {
        for(int i = 0; i < u.length; i++){
            u[i] = Math.min(u[i], v[i]);
        }
    }
    
    /**
     * Addition function to be used as lambda
     * @param u  First row
     * @param v  Second row
     */
    protected static void sum(double[] u, double[] v) {
        for(int i = 0; i < u.length; i++){
            u[i] += v[i];
        }
    }
    
    private BiConsumer<double[], double[]> reduce;
}
