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

package jacobi.core.decomp.chol;

import jacobi.api.Matrix;
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
@JacobiImport("/jacobi/test/data/CholeskyDecompTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class CholeskyDecompTest {
    
    @JacobiInject(2)
    public Matrix input;
    
    @JacobiResult(1)
    public Matrix lower;
    
    @Test
    @JacobiImport("3x3")
    @JacobiEquals(expected = 1, actual = 1)
    public void test3x3() {
        this.lower = new CholeskyDecomp().compute(this.input).get();
    }

    @Test
    @JacobiImport("4x4")
    @JacobiEquals(expected = 1, actual = 1)
    public void test4x4() {
        this.lower = new CholeskyDecomp().compute(this.input).get();
    }
}
