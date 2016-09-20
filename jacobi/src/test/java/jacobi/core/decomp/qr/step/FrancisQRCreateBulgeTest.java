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
import jacobi.core.decomp.qr.step.FrancisQR;
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
@JacobiImport("/jacobi/test/data/FrancisQRCreateBulgeTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class FrancisQRCreateBulgeTest {
    
    @JacobiInject(1)
    public Matrix input;
    
    @JacobiResult(2)
    public Matrix reflector;
    
    @Test
    @JacobiImport("5x5")
    @JacobiEquals(expected = 2, actual = 2)
    public void test5x5() {
        this.reflector = new FrancisQR().getDoubleShift1stCol(input, 0, input.getRowCount());
    }

    @Test
    @JacobiImport("5x5(2)")
    @JacobiEquals(expected = 2, actual = 2)
    public void test2nd5x5() {
        this.reflector = new FrancisQR().getDoubleShift1stCol(input, 0, input.getRowCount());
    }
    
    @Test
    @JacobiImport("Step2 6x6")
    @JacobiEquals(expected = 2, actual = 2)
    public void testStepTwo6x6() {
        this.reflector = new FrancisQR().getDoubleShift1stCol(input, 1, input.getRowCount() - 1);
    }
}
