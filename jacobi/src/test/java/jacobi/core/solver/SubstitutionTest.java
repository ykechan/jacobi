/*
 * Copyright (C) 2016 Y.K. Chan
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package jacobi.core.solver;

import jacobi.api.Matrix;
import jacobi.core.solver.Substitution.Mode;
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
    
    @Test(expected = UnsupportedOperationException.class)
    @JacobiImport("Upper 4x4x2")
    public void testUpperUnderDet4x4x2() {
        new Substitution(Mode.BACKWARD, this.tri).compute(rhs);
    }
    
    @Test(expected = UnsupportedOperationException.class)
    @JacobiImport("Lower 5x5x1")
    public void testLowerUnderDet5x5x1() {
        new Substitution(Mode.BACKWARD, this.tri).compute(rhs);
    }
}
