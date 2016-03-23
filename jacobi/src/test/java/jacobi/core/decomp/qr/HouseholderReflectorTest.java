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

package jacobi.core.decomp.qr;

import jacobi.api.Matrix;
import jacobi.api.ext.Prop;
import jacobi.core.prop.Transpose;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.Jacobi;
import jacobi.test.util.JacobiJUnit4ClassRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Y.K. Chan
 */
@JacobiImport("/jacobi/test/data/HouseholderReflectorTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class HouseholderReflectorTest {
    
    @JacobiInject(1)
    public Matrix input;
    
    @JacobiResult(2)
    public Matrix output;
    
    @JacobiInject(3)
    public Matrix column;
    
    @JacobiResult(4)
    public Matrix verify;
    
    @Test
    @JacobiImport("5x5")
    @JacobiEquals(expected = 2, actual = 2, epsilon = 1e-12)
    public void test5x5Elements() {
        this.output = new HouseholderReflector(new Transpose().compose(this.input).getRow(0), 0);
        Jacobi.assertEquals(this.output, this.output.copy());
        Jacobi.assertEquals(this.output, this.output.ext(Prop.class).inv());
    }

    @Test
    @JacobiImport("Unit 5x5")
    @JacobiEquals(expected = 2, actual = 2, epsilon = 1e-12)
    public void testUnit5x5Elements() {
        this.output = new HouseholderReflector(new Transpose().compose(this.input).getRow(0), 0);
    }
    
    @Test
    @JacobiImport("7x7 From 2")
    @JacobiEquals(expected = 2, actual = 2, epsilon = 1e-12)
    public void test7x7From2Elements() {
        this.output = new HouseholderReflector(new Transpose().compose(this.input).getRow(0), 2);
    }
    
    @Test
    @JacobiImport("Apply Left 5x1")
    @JacobiEquals(expected = 2, actual = 2, epsilon = 1e-12)
    @JacobiEquals(expected = 4, actual = 4, epsilon = 1e-12)
    public void testApply5x1() {
        HouseholderReflector h = new HouseholderReflector(new Transpose().compose(this.input).getRow(0), 0);
        this.output = h;
        this.verify = h.mul(this.column);
    }
    
    @Test
    @JacobiImport("Apply Left 7x1")
    @JacobiEquals(expected = 2, actual = 2, epsilon = 1e-12)
    @JacobiEquals(expected = 4, actual = 4, epsilon = 1e-12)
    public void testApply7x1() {
        HouseholderReflector h = new HouseholderReflector(new Transpose().compose(this.input).getRow(0), 0);
        this.output = h;
        this.verify = h.mul(this.column);
    }
}
