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
package jacobi.core.decomp.qr;

import jacobi.api.Matrix;
import jacobi.api.ext.Op;
import jacobi.api.ext.Prop;
import jacobi.core.impl.ColumnVector;
import jacobi.core.prop.Transpose;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.Jacobi;
import jacobi.test.util.JacobiJUnit4ClassRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Y.K. Chan
 */
@JacobiImport("/jacobi/test/data/HouseholderReflectorTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class HouseholderReflectorTest {
    
    @JacobiInject(1)
    public Matrix input;
    
    @JacobiResult(2)
    public Matrix output;
    
    @JacobiInject(3)
    public Matrix column;
    
    @JacobiResult(4)
    public Matrix verify;
    
    @Test
    @JacobiImport("5x5")
    @JacobiEquals(expected = 2, actual = 2, epsilon = 1e-12)
    public void test5x5Elements() {
        this.output = new HouseholderReflector(new Transpose().compute(this.input).getRow(0), 0);
        Jacobi.assertEquals(this.output, this.output.copy());
        Jacobi.assertEquals(this.output, this.output.ext(Prop.class).inv().get());
    }

    @Test
    @JacobiImport("Unit 5x5")
    @JacobiEquals(expected = 2, actual = 2, epsilon = 1e-12)
    public void testUnit5x5Elements() {
        this.output = new HouseholderReflector(new Transpose().compute(this.input).getRow(0), 0);
    }
    
    @Test
    @JacobiImport("7x7 From 2")
    @JacobiEquals(expected = 2, actual = 2, epsilon = 1e-12)
    public void test7x7From2Elements() {
        this.output = new HouseholderReflector(new Transpose().compute(this.input).getRow(0), 2);
    }
    
    @Test
    @JacobiImport("Apply Left 5x1")
    @JacobiEquals(expected = 2, actual = 2, epsilon = 1e-12)
    @JacobiEquals(expected = 4, actual = 4, epsilon = 1e-12)
    public void testApply5x1() {
        HouseholderReflector h = new HouseholderReflector(new Transpose().compute(this.input).getRow(0), 0);
        this.output = h;
        this.verify = h.mul(this.column);
    }
    
    @Test
    @JacobiImport("Apply Left 5x1")
    @JacobiEquals(expected = 2, actual = 2, epsilon = 1e-12)
    @JacobiEquals(expected = 4, actual = 4, epsilon = 1e-12)
    public void testApply5x1ByStream() {
        HouseholderReflector h = new HouseholderReflector(new Transpose().compute(this.input).getRow(0), 0){

            @Override
            protected double[] partialApply(Matrix matrix, int startCol) {
                return this.partialApplyByStream(matrix, startCol);
            }
            
        };
        this.output = h;
        this.verify = h.mul(this.column);
    }
    
    @Test
    @JacobiImport("Apply Left 7x1")
    @JacobiEquals(expected = 2, actual = 2, epsilon = 1e-12)
    @JacobiEquals(expected = 4, actual = 4, epsilon = 1e-12)
    public void testApply7x1() {
        HouseholderReflector h = new HouseholderReflector(new Transpose().compute(this.input).getRow(0), 0);
        this.output = h;
        this.verify = h.ext(Op.class).mul(this.column).get();
    }
    
    @Test
    @JacobiImport("Apply Left 5x3")
    @JacobiEquals(expected = 2, actual = 2, epsilon = 1e-12)
    @JacobiEquals(expected = 4, actual = 4, epsilon = 1e-12)
    public void testApply5x3() {
        HouseholderReflector h = new HouseholderReflector(new Transpose().compute(this.input).getRow(0), 0);
        this.output = h;
        this.verify = h.mul(this.column);
    }
    
    @Test
    @JacobiImport("Apply Left 5x3")
    @JacobiEquals(expected = 2, actual = 2, epsilon = 1e-12)
    @JacobiEquals(expected = 4, actual = 4, epsilon = 1e-12)
    public void testApply5x3ByStream() {
        HouseholderReflector h = new HouseholderReflector(new Transpose().compute(this.input).getRow(0), 0){

            @Override
            protected double[] partialApply(Matrix matrix, int startCol) {
                return this.partialApplyByStream(matrix, startCol);
            }
            
        };
        this.output = h;
        this.verify = h.mul(this.column);
    }
    
    @Test
    @JacobiImport("Apply Left 7x1")
    public void testInverse() {
        HouseholderReflector h = new HouseholderReflector(new Transpose().compute(this.input).getRow(0), 0);
        Jacobi.assertEquals(h, h.inv().get());
        Jacobi.assertEquals(h, h.transpose());
    }
    
    @Test(expected = IllegalArgumentException.class)
    @JacobiImport("Apply Left 7x1")
    public void testMismatchWithColumnVector() {
        HouseholderReflector h = new HouseholderReflector(new Transpose().compute(this.input).getRow(0), 0);
        h.applyLeft(new ColumnVector(new double[]{1.0, 2.0, 3.0}));
    }
    
    @Test
    public void testNormalizeWithZeroVector() {
        HouseholderReflector h = new HouseholderReflector(new double[5], 0);
        Assert.assertEquals(0.0, h.normalize(), 1e-16);
    }
    
}
