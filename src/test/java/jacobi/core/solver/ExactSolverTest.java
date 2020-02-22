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
import jacobi.test.util.Jacobi;
import jacobi.test.util.JacobiJUnit4ClassRunner;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Y.K. Chan
 */
@JacobiImport("/jacobi/test/data/ExactSolverTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class ExactSolverTest {
    
    @JacobiInject(0)
    public Matrix lhs;
    
    @JacobiInject(1)
    public Matrix rhs;
    
    @JacobiInject(2)
    public Matrix upper;
    
    @JacobiInject(3)
    public Matrix rhsAfter;
    
    @JacobiResult(4)
    public Matrix ans;
    
    @Test
    @JacobiImport("5x5 to 5x1")    
    @JacobiEquals(expected = 4, actual = 4)
    public void testSolve5x5Against5x1() {
        this.ans = this.mock(upper, rhsAfter).solve(lhs, rhs).get();
    }
    
    @Test
    @JacobiImport("4x4 to 4x2")    
    @JacobiEquals(expected = 4, actual = 4)
    public void testSolve4x4Against4x2() {
        this.ans = this.mock(upper, rhsAfter).solve(lhs, rhs).get();
    }
    
    @Test
    @JacobiImport("Degen 4x4 to 4x1")
    public void testSolveDegen4x4Against4x1() {
        Assert.assertFalse(this.mock(upper, rhsAfter).solve(lhs, rhs).isPresent());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testNotExactSystem() {
        this.ans = this.mock(null, null).solve(Matrices.zeros(5, 3), Matrices.zeros(5, 1)).get();        
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testDimensionMismatch() {
        this.ans = this.mock(null, null).solve(Matrices.zeros(7, 7), Matrices.zeros(5, 1)).get();
        Assert.assertEquals(0, this.ans.getRowCount());
    }
    
    @Test
    public void testEmptySystem() {
        this.ans = this.mock(null, null).solve(Matrices.zeros(0), Matrices.zeros(0)).get();
        Assert.assertEquals(0, this.ans.getRowCount());
    }
    
    private ExactSolver mock(Matrix upper, Matrix rhs) {
        return new ExactSolver(){

            @Override
            protected Optional<Matrix> backwardSubs(Matrix a, Matrix y) {
                Jacobi.assertEquals(upper, a);
                Jacobi.assertEquals(rhs, y);
                return super.backwardSubs(a, y);
            }
            
        };
    }
    
}
