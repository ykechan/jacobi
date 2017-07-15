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
        return a instanceof ColumnVector 
            ? mul.compute(Matrices.wrap(new double[][]{ ((ColumnVector) a).getVector() }), b)
            : Matrices.wrap(new double[][]{             
                a.getRowCount() < DEFAULT_THRESHOLD ? this.dot(a, b) : this.parallel(a, b)
              });
    }
    
    /**
     * 
     * @param a
     * @param b
     * @return 
     */
    protected double[] parallel(Matrix a, Matrix b) {
        return MapReducer.of(0, a.getRowCount())
                .flop(a.getColCount())
                .map((begin, end) -> this.dot(a, b, begin, end))
                .reduce((u, v) -> this.sum(u, v))
                .get();
    }
    
    protected double[] dot(Matrix a, Matrix b) {
        return this.dot(a, b, 0, a.getRowCount());
    }
    
    protected double[] dot(Matrix a, Matrix b, int begin, int end) {
        double[] w = new double[a.getColCount()];
        for(int i = begin; i < end; i++){
            this.dot(a.getRow(i), b.getRow(i), w);
        }
        return w;
    }
    
    protected double[] sum(double[] u, double[] v) {
        for(int i = 0; i < v.length; i++){
            v[i] += u[i];
        }
        return v;
    }

    protected double[] dot(double[] u, double[] v, double[] w) {
        for(int i = 0; i < w.length; i++){
            w[i] += u[i] * v[i];
        }
        return w;
    }
    
    private Mul mul;
}
