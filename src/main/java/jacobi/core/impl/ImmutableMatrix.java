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
import jacobi.core.facade.FacadeProxy;
import java.util.Arrays;

/**
 * Common parent class for a matrix implementation that is immutable.
 * 
 * <p>Usually child class represents matrix that is in a special form or value,
 * such as an identity matrix. Instead of fully realize all matrix entries,
 * it may be favorable to compute the matrix elements on demand, because
 * what is interesting is not the elements but its operations. For example the 
 * product of any matrix with an identity matrix is instantly known, which
 * is itself, saving O(n^3) operations.</p>
 * 
 * <p>These operations can be implemented as delegates that can will be chosen
 * preferably by the facade engine through extension interfaces.</p>
 * 
 * <p>However such advantage is lost if matrix element values are changed, and it
 * is no longer, say, an identity matrix. Therefore the implementation of an
 * identity matrix must be immutable.</p>
 * 
 * <p>An immutable matrix would not return its inner array in getRow(...), and
 * methods swapRow(...), set(...), setRow(...) would results in an
 * UnsupportedOperationException to be thrown.</p>
 * 
 * @author Y.K. Chan
 */
public abstract class ImmutableMatrix implements Matrix { // NOPMD
    
    /**
     * Decorate a base matrix to make it immutable.
     * @param base  Base matrix
     * @return  An immutable matrix
     */
    public static ImmutableMatrix of(Matrix base) {
        return base instanceof ImmutableMatrix 
            ? (ImmutableMatrix) base
            : new ImmutableMatrix() {

            @Override
            public int getRowCount() {
                return base.getRowCount();
            }

            @Override
            public int getColCount() {
                return base.getColCount();
            }

            @Override
            public double[] getRow(int index) {
                return Arrays.copyOf(base.getRow(index), base.getColCount());
            }

            @Override
            public double get(int i, int j) {
                return base.get(i, j);
            }

            @Override
            public Matrix copy() {
                return base.copy();
            }

            @Override
            public <T> T ext(Class<T> clazz) {
                return FacadeProxy.of(clazz, base, this);
            }
            
        };
    }

    @Override
    public final Matrix setRow(int index, double[] values) {
        return this.throwUnsupported();
    }

    @Override
    public final Matrix set(int i, int j, double value){
        return this.throwUnsupported();
    }

    @Override
    public final Matrix swapRow(int i, int j) {
        return this.throwUnsupported();
    }

    @Override
    public <T> T ext(Class<T> clazz) {
        return FacadeProxy.of(clazz, this);
    }

    @Override
    public Matrix copy() {
        return new DefaultMatrix(this);
    }

    private Matrix throwUnsupported() {
        throw new UnsupportedOperationException(this.getClass() + " is immutable.");
    }
}
