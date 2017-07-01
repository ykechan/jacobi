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
package jacobi.core.impl;

import jacobi.api.Matrix;
import jacobi.api.annotations.Delegate;
import jacobi.api.annotations.Immutate;
import jacobi.api.ext.Op;
import jacobi.api.ext.Prop;
import jacobi.core.util.Throw;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.DoubleStream;

/**
 *
 * Implementation of a diagonal matrix. This class stores the diagonal values
 * only, which saves many un-necessary zero elements. To maintain being
 * diagonal, this class is immutable.
 * 
 * @author Y.K. Chan
 */
public class DiagonalMatrix extends ImmutableMatrix {

    /**
     * Constructor.
     * @param vector  Diagonal elements
     */
    public DiagonalMatrix(double[] vector) {
        this.vector = Arrays.copyOf(vector, vector.length);
    }

    @Override
    public int getRowCount() {
        return this.vector.length;
    }

    @Override
    public int getColCount() {
        return this.vector.length;
    }

    @Override
    public double[] getRow(int index) {
        double[] row = new double[this.getColCount()];
        row[index] = this.vector[index];
        return row;
    }        

    @Override
    public double get(int i, int j) {
        return i == j ? this.vector[i] : 0.0;
    }

    @Override
    public Matrix copy() {
        return CopyOnWriteMatrix.of(new DiagonalMatrix(this.vector));
    }
    
    /**
     * Determinant of the diagonal matrix.
     * @return  det(A), which A is this matrix
     */
    @Immutate
    @Delegate(facade = Prop.class, method = "det")
    public double det() {
        return DoubleStream.of(this.vector).reduce(1.0, (a, b) -> a * b);
    }
    
    /**
     * Inverse of the diagonal matrix.
     * @return  A^-1
     */
    @Immutate
    @Delegate(facade = Prop.class, method = "inv")
    public Optional<Matrix> inv() {
        double[] diag = new double[this.vector.length];
        for(int i = 0; i < diag.length; i++){
            if(Math.abs(this.vector[i]) < 1e-12){
                return Optional.empty();
            }
            diag[i] = 1.0 / this.vector[i];
        }
        return Optional.of(CopyOnWriteMatrix.of(new DiagonalMatrix(diag)));
    }
    
    /**
     * Compute D * B where D is this diagonal matrix.
     * @param b  Input matrix B
     * @return  D * B
     */
    @Immutate
    @Delegate(facade = Op.class, method = "mul")
    public Matrix mul(Matrix b) {
        Throw.when()
            .isNull(() -> b, () -> "Missing 2nd operand.")
            .isTrue(
                () -> b.getRowCount() != this.getRowCount(),
                () -> "Dimension mismatch. Expects " 
                        + this.getRowCount() 
                        + " rows, actual operand has " 
                        + b.getRowCount()
                        + " rows.");
        if(b instanceof DiagonalMatrix){
            return this.mul((DiagonalMatrix) b);
        }
        if(b instanceof ColumnVector){
            return this.mul((ColumnVector) b);
        }
        Matrix ans = new DefaultMatrix(b);
        for(int i = 0; i < this.vector.length; i++){
            double[] row = ans.getRow(i);
            double k = this.vector[i];
            for(int j = 0; j < row.length; j++){
                row[j] *= k;
            }
            ans.setRow(i, row);
        }
        return ans;
    }
    
    private Matrix mul(DiagonalMatrix diag) {
        return CopyOnWriteMatrix.of(new DiagonalMatrix(
                this.mul(this.vector, diag.vector)
        ));
    }
    
    private Matrix mul(ColumnVector v) {
        return new ColumnVector(this.mul(this.vector, v.getVector()));
    }

    private double[] mul(double[] u, double[] v) {
        double[] w = new double[this.vector.length];
        for(int i = 0; i < w.length; i++){
            w[i] = u[i] * v[i];
        }
        return w;
    }
    
    private double[] vector;
}
