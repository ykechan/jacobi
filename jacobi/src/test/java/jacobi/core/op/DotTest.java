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
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Y.K. Chan
 */
@JacobiImport("/jacobi/test/data/DotTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class DotTest {
    
    @JacobiInject(0)
    public Matrix a;
    
    @JacobiInject(1)
    public Matrix b;
    
    @JacobiResult(2)
    public Matrix ans;
    
    @Test
    @JacobiImport("7x3")
    @JacobiEquals(expected = 2, actual = 2)
    public void test7x3() {
        this.ans = new Dot().compute(a, b);
    }
    
    @Test
    @JacobiImport("9x1")
    @JacobiEquals(expected = 2, actual = 2)
    public void test9x1() {
        this.ans = new Dot().compute(a, b);
    }
    
    @Test
    @JacobiImport("16x7")
    @JacobiEquals(expected = 2, actual = 2)
    public void test16x7InParallel() {
        this.ans = this.useParallel().compute(a, b);        
    }
    
    @Test
    @JacobiImport("16x7")
    @JacobiEquals(expected = 2, actual = 2)
    public void test16x7InMapReduce() {
        this.ans = this.useMapReduce().compute(a, b);        
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testNull1stOperand() {
        new Dot().compute(null, Matrices.zeros(0));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testNull2ndOperand() {
        new Dot().compute(Matrices.zeros(0), null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testColMismatch() {
        new Dot().compute(Matrices.zeros(7, 4), Matrices.zeros(7, 2));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testRowMismatch() {
        new Dot().compute(Matrices.zeros(6, 4), Matrices.zeros(7, 4));
    }
    
    @Test
    public void testEmptyMatrix() {
        Assert.assertEquals(0, new Dot().compute(Matrices.zeros(0), Matrices.zeros(0)).getRowCount());
    }
    
    protected Dot useParallel() {
        return new Dot(){

            @Override
            protected double[] serial(Matrix a, Matrix b) {
                return super.parallel(a, b);
            }

            @Override
            protected int numFlops(Matrix mat) {
                return MapReducer.DEFAULT_NUM_FLOP / 2;
            }
            
        };
    }
    
    protected Dot useMapReduce() {
        return new Dot() {

            @Override
            protected double[] serial(Matrix a, Matrix b) {
                return super.parallel(a, b);
            }
            
        };
    }
    
}
