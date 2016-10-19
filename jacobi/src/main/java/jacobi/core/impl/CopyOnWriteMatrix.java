/* 
 * The MIT License
 *
 * Copyright 2016 Y.K. Chan
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
import jacobi.core.facade.FacadeProxy;

/**
 * A matrix decorator that copies the value of the underlying matrix
 * whenever written upon.
 * 
 * Computational result can take some special form, for example a diagonal 
 * matrix. It maybe advantageous to return an special immutable implementation 
 * that does not fully realize all matrix elements. 
 * 
 * However user may further change the value of the return results. This class
 * is for such cases, it would then materializes all elements to a vanilla
 * DefaultMatrix. All operations would be transparent with respect to the user.
 * 
 * @author Y.K. Chan
 */
public final class CopyOnWriteMatrix implements Matrix {
    
    /**
     * Decorate a matrix to a Copy-on-write matrix
     * @param matrix  Base matrix
     * @return  A copy-on-write matrix
     */
    public static CopyOnWriteMatrix of(Matrix matrix) {
        return matrix instanceof CopyOnWriteMatrix
                ? (CopyOnWriteMatrix) matrix
                : new CopyOnWriteMatrix( ImmutableMatrix.of(matrix) );
    }

    protected CopyOnWriteMatrix(Matrix matrix) {
        this.materialized = false;
        this.matrix = matrix;
    }

    @Override
    public int getRowCount() {
        return this.matrix.getRowCount();
    }

    @Override
    public int getColCount() {
        return this.matrix.getColCount();
    }

    @Override
    public double[] getRow(int index) {
        return this.matrix.getRow(index);
    }

    @Override
    public double get(int i, int j) {
        return this.matrix.get(i, j);
    }

    @Override
    public Matrix setRow(int index, double[] values) {
        this.materialize().setRow(index, values);
        return this;
    }

    @Override
    public Matrix swapRow(int i, int j) {
        this.materialize().swapRow(i, j);
        return this;
    }

    @Override
    public <T> T ext(Class<T> clazz) {
        return FacadeProxy.of(clazz, this.matrix, this);
    }

    @Override
    public Matrix copy() {
        return CopyOnWriteMatrix.of(this.matrix);
    }
    
    private Matrix materialize() {
        if(!this.materialized){
            synchronized(this) {
                if(!this.materialized){
                    this.matrix = new DefaultMatrix(this.matrix);
                    this.materialized = true;
                }
            }
        }
        return this.matrix;
    }
    
    private Matrix matrix;
    private volatile boolean materialized;
}
