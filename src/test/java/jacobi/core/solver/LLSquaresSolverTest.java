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
package jacobi.core.solver;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
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
@JacobiImport("/jacobi/test/data/LLSquaresSolverTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class LLSquaresSolverTest {
    
    @JacobiInject(0)
    public Matrix input;
    
    @JacobiInject(1)
    public Matrix target;
    
    @JacobiResult(2)
    public Matrix ans;
    
    private LLSquaresSolver solver;

    public LLSquaresSolverTest() {
        this.solver = new LLSquaresSolver();
    }
    
    @Test
    @JacobiImport("13x3x1")
    @JacobiEquals(expected = 2, actual = 2)
    public void test13x3x1() {
        this.ans = this.solver.solve(this.input, this.target).get();
    }
    
    @Test
    @JacobiImport("Simple Regression")
    @JacobiEquals(expected = 2, actual = 2)
    public void testSimpleRegression() {
        this.ans = this.solver.solve(this.input, this.target).get();
    }
    
    @Test
    @JacobiImport("Degen 7x4x1")
    public void testDegen7x4x1() {
        Assert.assertFalse(this.solver.solve(this.input, this.target).isPresent());
    }
    
    @Test
    @JacobiImport("Degen 11x3x1")
    public void testDegen11x3x1() {
        Assert.assertFalse(this.solver.solve(this.input, this.target).isPresent());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testNullSystem() {
        this.ans = this.solver.solve(null, Matrices.zeros(0)).get();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testNullTarget() {
        this.ans = this.solver.solve(Matrices.zeros(0), null).get();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testUnderDetermined() {
        this.ans = this.solver.solve(Matrices.zeros(3, 5), Matrices.zeros(3)).get();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDimensionMismatch() {
        this.ans = this.solver.solve(Matrices.zeros(13, 3), Matrices.zeros(10, 1)).get();
    }    
    
    @Test
    public void testEmptySystem() {
        this.ans = this.solver.solve(Matrices.zeros(0), Matrices.zeros(0)).get();
        Assert.assertEquals(0, this.ans.getRowCount());
    }
    
}
