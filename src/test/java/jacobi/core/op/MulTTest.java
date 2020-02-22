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
import jacobi.core.util.MapReducer;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.JacobiJUnit4ClassRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Y.K. Chan
 */
@JacobiImport("/jacobi/test/data/MulTTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class MulTTest {
    
    @JacobiInject(0)
    public Matrix a;
    
    @JacobiInject(1)
    public Matrix b;
    
    @JacobiResult(2)
    public Matrix ans;
    
    public MulTTest() {
    }

    @Test
    @JacobiImport("(3x7)x(7x4)^t")
    @JacobiEquals(expected = 2, actual = 2)
    public void test3x7x7x4t() {
        this.ans = new MulT().compute(a, b);
    }
    
    @Test
    @JacobiImport("(6x2)x(5x2)^t")
    @JacobiEquals(expected = 2, actual = 2)
    public void test6x2x5x2t() {
        this.ans = new MulT().compute(a, b);
    }
    
    @Test
    @JacobiImport("(10x4)x(4x13)^t")
    @JacobiEquals(expected = 2, actual = 2)
    public void testForkJoinOn10x4x4x13() {
        this.ans = this.mockForkJoin().compute(a, b);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testDimensionMismatch() {
        new MulT().compute(Matrices.zeros(5, 2), Matrices.zeros(3, 5));
    }
    
    private MulT mockForkJoin() {
        return new MulT(){

            @Override
            public Matrix compute(Matrix a, Matrix b, Matrix ans) {
                return this.parallel(a, b, ans, MapReducer.DEFAULT_NUM_FLOP / 4);
            }
            
        };
    }
    
}
