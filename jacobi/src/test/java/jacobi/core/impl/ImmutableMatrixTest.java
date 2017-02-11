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

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.api.annotations.Facade;
import jacobi.api.annotations.Immutate;
import jacobi.api.annotations.Implementation;
import jacobi.test.util.Jacobi;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Y.K. Chan
 */
public class ImmutableMatrixTest {
    
    @Test(expected = UnsupportedOperationException.class)
    public void testSetRow() {
        double[] array = {Math.PI, Math.E, Math.sqrt(2.0)};
        Matrix matrix = ImmutableMatrix.of(new DefaultMatrix(new double[][]{
            array
        }));
        double[] row = matrix.getRow(0);
        Assert.assertFalse(row == array);
        Assert.assertArrayEquals(array, row, 1e-16);       
        Assert.assertEquals(1, matrix.getRowCount());
        Assert.assertEquals(3, matrix.getColCount());
        row[1] = Math.sqrt(5.0);
        Assert.assertEquals(Math.PI, matrix.get(0, 0), 1e-16);
        Assert.assertEquals(Math.E, matrix.get(0, 1), 1e-16);
        Assert.assertEquals(Math.sqrt(2.0), matrix.get(0, 2), 1e-16);                
        matrix.setRow(0, row);
    }    
    
    @Test
    public void testImmutableProp() {
        Matrix matrix = ImmutableMatrix.of(new DefaultMatrix(new double[][]{
            { Math.PI, Math.E, Math.sqrt(2.0) }
        }));
        
        Assert.assertEquals(0.0, matrix.ext(SampleFacade.class).zero(), 1e-16);
        
    }
    
    @Test
    public void testCopy() {
        double[] array = {Math.PI, Math.E, Math.sqrt(2.0)};
        Matrix matrix = ImmutableMatrix.of(new DefaultMatrix(new double[][]{array}));
        
        Matrix copy = matrix.copy();
        copy.setRow(0, new double[]{0.0, 1.0, 2.0});
        
        Jacobi.assertEquals(Matrices.of(new double[][]{array}), matrix);
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void testSwapRow() {
        ImmutableMatrix.of(new DefaultMatrix(new double[][]{
            {1.0, 2.0, 3.0},
            {4.0, 5.0, 6.0}
        })).swapRow(0, 1);
    }
    
    @Test
    public void testBaseCopy() {
        double[] array = {Math.PI, Math.E, Math.sqrt(2.0)};
        Matrix matrix = new ImmutableRowVector(array);
        Matrix copy = matrix.copy();
        copy.set(0, 0, 0.0);
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void testBaseSetRow() {
        double[] array = {Math.PI, Math.E, Math.sqrt(2.0)};
        new ImmutableRowVector(array).setRow(0, new double[]{1.0, 2.0, 3.0});
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void testBaseSetElem() {
        double[] array = {Math.PI, Math.E, Math.sqrt(2.0)};
        new ImmutableRowVector(array).set(0, 1, 0.0);
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void testBaseSwapRow() {
        double[] array = {Math.PI, Math.E, Math.sqrt(2.0)};
        new ImmutableRowVector(array).swapRow(0, 0);
    }
    
    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testEmptyGetRow() {
        Empty.getInstance().getRow(0);
    }
    
    @Immutate
    @Facade
    public interface SampleFacade {
        
        @Implementation(SampleImpl.class)
        public double zero();
        
    }
        
    @Immutate
    public static class SampleImpl {
        
        public double zero(Matrix input) {
            return 0.0;
        }
    }
        
    public static class ImmutableRowVector extends ImmutableMatrix {

        public ImmutableRowVector(double[] row) {
            this.row = Arrays.copyOf(row, row.length);
        }

        @Override
        public int getRowCount() {
            return 1;
        }

        @Override
        public int getColCount() {
            return this.row.length;
        }

        @Override
        public double[] getRow(int index) {
            return Arrays.copyOf(this.row, this.row.length);
        }
        
        private double[] row;
    }
}
