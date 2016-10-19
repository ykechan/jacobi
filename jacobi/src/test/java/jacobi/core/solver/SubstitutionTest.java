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
package jacobi.core.solver;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.core.solver.Substitution.Mode;
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
@JacobiImport("/jacobi/test/data/SubstitutionTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class SubstitutionTest {
    
    @JacobiInject(1)
    public Matrix tri;
    
    @JacobiInject(3)
    @JacobiResult(2)
    public Matrix rhs;
    
    @Test
    @JacobiImport("Upper Full Rank 5x5x1")
    @JacobiEquals(expected = 2, actual = 2)
    public void testUpperFullRank5x5x1() {
        new Substitution(Mode.BACKWARD, this.tri).compute(rhs);
    }
    
    @Test
    @JacobiImport("Upper Full Rank 5x5x3")
    @JacobiEquals(expected = 2, actual = 2)
    public void testUpperFullRank5x5x3() {
        new Substitution(Mode.BACKWARD, this.tri).compute(rhs);
    }

    @Test
    @JacobiImport("Upper Full Rank 7x7x2")
    @JacobiEquals(expected = 2, actual = 2)
    public void testUpperFullRank7x7x2() {
        new Substitution(Mode.BACKWARD, this.tri).compute(rhs);
    }
    
    @Test
    @JacobiImport("Lower Full Rank 6x6x1")
    @JacobiEquals(expected = 2, actual = 2)
    public void testLowerFullRank6x6x1() {
        new Substitution(Mode.FORWARD, this.tri).compute(rhs);
    }
    
    @Test
    @JacobiImport("Upper Full Rank 7x7x2")
    @JacobiEquals(expected = 2, actual = 2)
    public void testUpperFullRank7x7x2InParallel() {
        new Substitution(Mode.BACKWARD, this.tri){

            @Override
            protected void substitute(Matrix rhs, double[] subs, int subIndex, int begin, int end) {
                this.stream(rhs, subs, subIndex, begin, end);
            }
            
        }.compute(rhs);
    }
    
    @Test
    @JacobiImport("Lower Full Rank 6x6x1")
    @JacobiEquals(expected = 2, actual = 2)
    public void testLowerFullRank6x6x1InParallel() {
        new Substitution(Mode.FORWARD, this.tri){

            @Override
            protected void substitute(Matrix rhs, double[] subs, int subIndex, int begin, int end) {
                this.stream(rhs, subs, subIndex, begin, end);
            }
            
        }.compute(rhs);
    }
    
    @Test
    @JacobiImport("Upper 4x4x2")
    public void testUpperUnderDet4x4x2() {
        Assert.assertNull(new Substitution(Mode.BACKWARD, this.tri).compute(rhs));
    }
    
    @Test
    @JacobiImport("Lower 5x5x1")
    public void testLowerUnderDet5x5x1() {
        Assert.assertNull(new Substitution(Mode.FORWARD, this.tri).compute(rhs));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testRowCountMismatch() {
        new Substitution(Mode.BACKWARD, Matrices.zeros(5, 3)).compute(Matrices.zeros(2));
    }
    
}
