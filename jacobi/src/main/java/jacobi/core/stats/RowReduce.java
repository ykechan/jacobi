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
        Throw.when().isNull(() -> matrix, () -> "No matrix to compute.");
        return matrix.getRowCount() == 0 ? EMPTY : this.serial(matrix, 0, matrix.getRowCount());
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
    
    private static final double[] EMPTY = new double[0];
}
