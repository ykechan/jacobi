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
package jacobi.core.decomp.svd;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.core.op.Mul;
import jacobi.core.prop.Transpose;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.JacobiJUnit4ClassRunner;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Y.K. Chan
 */
@JacobiImport("/jacobi/test/data/SingularValueDecompTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class SingularValueDecompTest {
    
    @JacobiInject(0)
    public Matrix input;
    
    @JacobiResult(0)
    public Matrix verification;
    
    @Test
    @JacobiImport("12x3")
    @JacobiEquals(expected = 0, actual = 0)
    @SuppressWarnings("InfiniteRecursion")
    public void test12x3() {
        Matrix u = Matrices.identity(this.input.getRowCount());
        Matrix v = Matrices.identity(this.input.getColCount());
        Matrix d = Matrices.diag(new SingularValueDecomp().compute(this.input, u, v), u.getRowCount());
        Mul mul = new Mul();
        this.verification = mul.compute(mul.compute(u, d), new Transpose().compute(v));        
    }
    
    @Test
    @JacobiImport("13x4")
    @JacobiEquals(expected = 0, actual = 0)
    @SuppressWarnings("InfiniteRecursion")
    public void test13x4() {
        Matrix u = Matrices.identity(this.input.getRowCount());
        Matrix v = Matrices.identity(this.input.getColCount());
        Matrix d = Matrices.diag(new SingularValueDecomp().compute(this.input, u, v), u.getRowCount());
        Mul mul = new Mul();
        this.verification = mul.compute(mul.compute(u, d), new Transpose().compute(v));        
    }
    
    @Test
    @JacobiImport("13x12")
    @JacobiEquals(expected = 0, actual = 0)
    @SuppressWarnings("InfiniteRecursion")
    public void test13x12() {
        Matrix u = Matrices.identity(this.input.getRowCount());
        Matrix v = Matrices.identity(this.input.getColCount());
        Matrix d = Matrices.diag(new SingularValueDecomp().compute(this.input, u, v), u.getRowCount());
        Mul mul = new Mul();
        this.verification = mul.compute(mul.compute(u, d), new Transpose().compute(v));        
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testLeftDimensionMismatch() {
        new SingularValueDecomp().compute(Matrices.zeros(3, 2), Matrices.zeros(0), null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testRightDimensionMismatch() {
        new SingularValueDecomp().compute(Matrices.zeros(3, 2), null, Matrices.zeros(0));
    }
    
    @Test
    public void testEmpty() {
        Assert.assertEquals(0, new SingularValueDecomp().compute(Matrices.zeros(0)).length); 
        Assert.assertEquals(0, new SingularValueDecomp().compute(Matrices.zeros(0), Matrices.zeros(0), Matrices.zeros(0)).length);
        Assert.assertEquals(0, new SingularValueDecomp().compute(Matrices.zeros(0), null, Matrices.zeros(0)).length);        
        Assert.assertEquals(0, new SingularValueDecomp().compute(Matrices.zeros(0), Matrices.zeros(0), null).length); 
    }    
    
}
