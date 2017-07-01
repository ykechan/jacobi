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
import jacobi.api.ext.Op;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.JacobiJUnit4ClassRunner;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Y.K. Chan
 */
@JacobiImport("/jacobi/test/data/MulTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class MulTest {
    
    @JacobiInject(0)
    public Matrix matrixA;
    
    @JacobiInject(1)
    public Matrix matrixB;
    
    @JacobiResult(2)
    public Matrix ans;
    
    @Test
    @JacobiImport("5x5 mul 5x5")
    @JacobiEquals(expected = 2, actual = 2)
    public void test5x5Mul5x5() {
        this.ans = new Mul().compute(this.matrixA, this.matrixB);
    }
    
    @Test
    @JacobiImport("7x7 mul 7x7")
    @JacobiEquals(expected = 2, actual = 2)
    public void test7x7Mul7x7() {
        this.ans = new Mul().compute(this.matrixA, this.matrixB);
    }
    
    @Test
    @JacobiImport("7x7 mul 7x7")
    @JacobiEquals(expected = 2, actual = 2)
    public void test7x7Mul7x7WithLesserStride() {
        this.ans = new Mul(3).compute(this.matrixA, this.matrixB);
    }
    
    @Test
    @JacobiImport("7x7 mul 7x7")
    @JacobiEquals(expected = 2, actual = 2)
    public void test7x7Mul7x7WithEqualStride() {
        this.ans = new Mul(7).compute(this.matrixA, this.matrixB);
    }

    @Test
    @JacobiImport("7x3 mul 3x5")
    @JacobiEquals(expected = 2, actual = 2)
    public void test7x3Mul3x5() {
        this.ans = new Mul().compute(this.matrixA, this.matrixB);
    }
    
    @Test
    @JacobiImport("2x9 mul 9x1")
    @JacobiEquals(expected = 2, actual = 2)
    public void test2x9Mul9x1() {
        this.ans = new Mul().compute(this.matrixA, this.matrixB);
    }
    
    @Test
    @JacobiImport("8x4 mul 4x8")
    @JacobiEquals(expected = 2, actual = 2)
    public void test8x4Mul4x8WithDivisibleStride() {
        this.ans = new Mul(2).compute(this.matrixA, this.matrixB);
    }
    
    @Test
    @JacobiImport("13x1 mul 1x4")
    @JacobiEquals(expected = 2, actual = 2)
    public void test13x1Mul1x4() {
        this.ans = this.matrixA.ext(Op.class).mul(this.matrixB).get();
    }
    
    @Test(expected = RuntimeException.class)
    public void test5x2Mul4x3() {
        this.matrixA = Matrices.zeros(5, 2);
        this.matrixB = Matrices.zeros(4, 3);
        this.ans = this.matrixA.ext(Op.class).mul(this.matrixB).get();
    }
    
    @Test
    @JacobiImport("7x7 mul 7x7")
    @JacobiEquals(expected = 2, actual = 2)
    public void test7x7Mul7x7InParallel() {
        this.ans = this.parallelMock().compute(this.matrixA, this.matrixB);
    }

    @Test
    @JacobiImport("7x3 mul 3x5")
    @JacobiEquals(expected = 2, actual = 2)
    public void test7x3Mul3x5InParallel() {
        this.ans = this.parallelMock().compute(this.matrixA, this.matrixB);
    }
    
    @Test
    @JacobiImport("2x9 mul 9x1")
    @JacobiEquals(expected = 2, actual = 2)
    public void test2x9Mul9x1InParallel() {
        this.ans = this.parallelMock().compute(this.matrixA, this.matrixB);
    }
    
    @Test
    @JacobiImport("Scalar mul 7x7")
    @JacobiEquals(expected = 2, actual = 2)
    public void testScalarMul7x7() {
        this.ans = this.matrixA.ext(Op.class).mul(this.matrixB.get(0, 0)).get();
    }
    
    @Test
    @JacobiImport("Scalar mul 13x2")
    @JacobiEquals(expected = 2, actual = 2)
    public void testScalarMul13x2() {
        this.ans = this.matrixA.ext(Op.class).mul(this.matrixB.get(0, 0)).get();
    }
    
    @Test
    @JacobiImport("1x7 mul 7x1")
    @JacobiEquals(expected = 2, actual = 2)
    public void test1x7Mul7x1() {
        AtomicBoolean marker = new AtomicBoolean(false);
        this.ans = new Mul(){

            @Override
            protected double dot(double[] u, double[] v) {
                marker.set(true);
                return super.dot(u, v);
            }

        }.compute(this.matrixA, this.matrixB);
        Assert.assertTrue(marker.get());
    }
    
    protected Mul parallelMock() {
        return new Mul(){

            @Override
            protected void compute(Matrix a, Matrix b, Matrix ans) {
                this.parallel(a, b, ans);
            }
            
        };
    }
}
