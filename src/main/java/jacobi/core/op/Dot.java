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

package jacobi.core.op;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.core.impl.ColumnVector;
import jacobi.core.util.MapReducer;
import jacobi.core.util.Throw;

/**
 * Compute the dot product of each columns of two matrices.
 * 
 * <p>The dot product of two column vectors is the sum of produce of each of their elements.</p>
 * 
 * @author Y.K. Chan
 */
public class Dot {
    
    /**
     * Default lower limit of number of rows to decide using parallelism.
     */
    public static final int DEFAULT_THRESHOLD = 64;
    
    /**
     * Compute the dot product between two vectors
     * @param u  Left vector
     * @param v  Right vector
     * @return  Dot product of the two vectors
     */
    public static double prod(double[] u, double[] v) {
    	double ans = 0.0;
    	for(int i = 0; i < v.length; i++){
    		ans += u[i] * v[i];
    	}
    	return ans;
    }

    /**
     * Constructor.
     */
    public Dot() {
        this.mul = new Mul();
    }
    
    /**
     * Compute the dot product of matrix A and B.
     * @param a  Matrix A
     * @param b  Matrix B
     * @return  Dot product
     */
    public Matrix compute(Matrix a, Matrix b) {
        Throw.when()
            .isNull(() -> a, () -> "First operand is missing.")
            .isNull(() -> b, () -> "Second operand is missing.")
            .isTrue(
                () -> a.getRowCount() != b.getRowCount() || a.getColCount() != b.getColCount(),
                () -> "Dimension mismatch. Unable to compute dot product of a "
                    + a.getRowCount()+ "x" + a.getColCount()
                    + " matrix with a "
                    + b.getRowCount()+ "x" + b.getColCount()
                    + " matrix.");
        if(a.getRowCount() == 0){
            return Matrices.zeros(0);
        }
        if(a instanceof ColumnVector){
            return mul.compute(Matrices.wrap(new double[][]{ ((ColumnVector) a).getVector() }), b);
        }
        return Matrices.wrap(new double[][]{             
            a.getRowCount() < DEFAULT_THRESHOLD ? this.serial(a, b) : this.parallel(a, b)
        });
    }
    
    /**
     * Compute the dot product of matrix A and B in parallel.
     * @param a  Matrix A
     * @param b  Matrix B
     * @return  Values of dot product of each columns in A and B
     */
    protected double[] parallel(Matrix a, Matrix b) {
        return MapReducer.of(0, a.getRowCount())
                .flop(this.numFlops(a))
                .map((begin, end) -> this.dot(a, b, begin, end))
                .reduce((u, v) -> this.sum(u, v))
                .get();
    }
    
    /**
     * Compute the dot product of matrix A and B in serial.
     * @param a  Matrix A
     * @param b  Matrix B
     * @return  Values of dot product of each columns in A and B
     */
    protected double[] serial(Matrix a, Matrix b) {
        return this.dot(a, b, 0, a.getRowCount());
    }
    
    /**
     * Compute the sum of product of columns for limited rows.
     * @param a  Matrix A
     * @param b  Matrix B
     * @param begin  Begin index of rows of interest
     * @param end  End index of rows of interest
     * @return  Sum of product of the columns for limited rows
     */
    protected double[] dot(Matrix a, Matrix b, int begin, int end) {
        double[] w = new double[a.getColCount()];
        for(int i = begin; i < end; i++){
            this.dot(a.getRow(i), b.getRow(i), w);
        }
        return w;
    }
    
    /**
     * Merge two arrays by summing the elements.
     * @param u  Vector u
     * @param v  Vector v
     * @return  Instance of v which contains the value u + v
     */
    protected double[] sum(double[] u, double[] v) {
        for(int i = 0; i < v.length; i++){
            v[i] += u[i];
        }
        return v;
    }

    /**
     * Compute the dot product of two vectors and sum the result to the resultant vector.
     * @param u  Input vector u
     * @param v  Input vector v
     * @param w  Resultant vector w
     * @return  Instance of w
     */
    protected double[] dot(double[] u, double[] v, double[] w) {
        for(int i = 0; i < w.length; i++){
            w[i] += u[i] * v[i];
        }
        return w;
    }
    
    /**
     * Find the number of flops of computing a row.
     * @param mat  Input matrix
     * @return  Number of flops of computing a row
     */
    protected int numFlops(Matrix mat) {
        return mat.getColCount();
    }
    
    private Mul mul;
}
