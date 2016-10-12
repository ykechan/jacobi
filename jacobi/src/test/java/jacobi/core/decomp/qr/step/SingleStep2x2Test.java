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

package jacobi.core.decomp.qr.step;

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
@JacobiImport("/jacobi/test/data/SingleStep2x2Test.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class SingleStep2x2Test {
    
    @JacobiInject(0)
    public Matrix input;
    
    @JacobiResult(1)
    public Matrix output;
    
    @Test
    @JacobiImport("2x2")
    @JacobiEquals(expected = 1, actual = 1)
    public void test2x2() {
        this.mock().compute(input, null, 0, 2, true);
        this.output = this.input;
    }
    
    @Test
    @JacobiImport("5x5")
    @JacobiEquals(expected = 1, actual = 1)
    public void test5x5() {
        this.mock().compute(input, null, 1, 3, true);
        this.output = this.input;
    }
    
    @Test
    @JacobiImport("Patho 2x2")
    @JacobiEquals(expected = 1, actual = 1)
    public void testPatho2x2() {
        this.mock().compute(input, null, 0, 2, true);
        this.output = this.input;
    }
    
    @Test
    @JacobiImport("Complex 2x2")
    @JacobiEquals(expected = 1, actual = 1)
    public void testComplex2x2() {
        this.mock().compute(input, null, 0, 2, true);
        this.output = this.input;
    }

    protected QRStep mock() {
        return new SingleStep2x2( (m, p, begin, end, full) -> {
            throw new UnsupportedOperationException();
        });
    }
}
