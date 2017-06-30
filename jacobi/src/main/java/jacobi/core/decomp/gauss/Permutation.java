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
package jacobi.core.decomp.gauss;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.api.annotations.Delegate;
import jacobi.api.annotations.Immutate;
import jacobi.api.ext.Op;
import jacobi.api.ext.Prop;
import jacobi.core.facade.FacadeProxy;
import jacobi.core.impl.CopyOnWriteMatrix;
import jacobi.core.impl.ImmutableMatrix;
import jacobi.core.util.Throw;
import java.util.Arrays;

/**
 * A permutation matrix i.e.&nbsp; matrix obtained from only swapping rows of an identity matrix.
 * 
 * <p>
 * To remain being a permutation matrix this class is immutable.
 * </p>
 * 
 * @author Y.K. Chan
 */
public class Permutation extends ImmutableMatrix {

    /**
     * Constructor.
     * @param length  Number of rows/columns
     */
    public Permutation(int length) {
        this.indices = new int[length];
        for(int i = 0; i < this.indices.length; i++){
            this.indices[i] = i;
        }
    }

    /**
     * Constructor.
     * @param indices  Permutation of row indices
     * @param order  Order of permutation
     */
    protected Permutation(int[] indices, int order) { // NOPMD - private usage
        this.indices = indices; 
        this.order = order;
    }
    
    /**
     * Determinant of the permutation matrix, which is the order of permutation.
     * @return  Determinant of permutation matrix
     */
    @Immutate
    @Delegate(facade = Prop.class, method = "det")
    public double det() {
        return this.order;
    }
    
    /**
     * Inverse of the permutation matrix, which is itself a permutation matrix.
     * @return  Inverse of the permutation matrix
     */
    @Immutate
    @Delegate(facade = Prop.class, method = "inv")
    public Matrix inv() {
        int[] inverse = new int[this.indices.length];
        for(int i = 0; i < inverse.length; i++){
            inverse[ this.indices[i] ] = i;
        }
        return new Permutation(inverse, this.order);
    }
    
    /**
     * Multiply with another matrix, which requires only swapping rows of the other matrix.
     * @param b  Matrix operand
     * @return  Resultant matrix
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
        Matrix ans = Matrices.zeros(b.getRowCount(), b.getColCount());
        for(int i = 0; i < ans.getRowCount(); i++){
            ans.setRow(i, b.getRow(this.indices[i]));
        }
        return ans;
    }

    @Override
    public int getRowCount() {
        return this.indices.length;
    }

    @Override
    public int getColCount() {
        return this.indices.length;
    }
    
    @Override
    public double[] getRow(int index) {
        double[] row = new double[this.indices.length];
        row[ this.indices[index] ] = 1.0;
        return row;
    }

    @Override
    public Matrix copy() {
        return CopyOnWriteMatrix.of(new Permutation(Arrays.copyOf(this.indices, this.getRowCount()), order));
    }

    @Override
    public <T> T ext(Class<T> clazz) {
        return FacadeProxy.of(clazz, this);
    }

    private int[] indices;
    private int order;
}
