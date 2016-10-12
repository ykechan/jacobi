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

package jacobi.core.prop;

import jacobi.api.Matrix;
import jacobi.core.impl.ColumnVector;
import jacobi.core.impl.Empty;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.JacobiJUnit4ClassRunner;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Y.K. Chan
 */
@JacobiImport("/jacobi/test/data/TransposeTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class TransposeTest {
    
    @JacobiInject(1)
    public Matrix input;
    
    @JacobiInject(2)
    public Matrix expects;
    
    @JacobiResult(2)
    public Matrix output;
    
    @Test
    @JacobiImport("5x5_1")
    @JacobiEquals(expected = 2, actual = 2)
    public void test5x5_1() {
        this.output = new Transpose().compose(this.input);
    }

    @Test
    @JacobiImport("5x5_2")
    @JacobiEquals(expected = 2, actual = 2)
    public void test5x5_2() {
        this.output = new Transpose().compose(this.input);
    }
    
    @Test
    @JacobiImport("3x5_1")
    @JacobiEquals(expected = 2, actual = 2)
    public void test3x5_1() {
        this.output = new Transpose().compose(this.input);
    }
    
    @Test
    @JacobiImport("5x3_1")
    @JacobiEquals(expected = 2, actual = 2)
    public void test5x3_1() {
        this.output = new Transpose().compose(this.input);
    }
    
    @Test
    @JacobiImport("7x1_1")
    @JacobiEquals(expected = 2, actual = 2)
    public void test7x1_1() {
        this.output = new Transpose().compose(this.input);
    }
    
    @Test
    @JacobiImport("1x7_1")
    @JacobiEquals(expected = 2, actual = 2)
    public void test1x7_1() {
        this.output = new Transpose().compose(this.input);        
    }
    
    @Test
    @JacobiImport("11x9")
    @JacobiEquals(expected = 2, actual = 2)
    public void test11x9() {
        this.output = new Transpose().compose(this.input);        
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testNull() {
        new Transpose().compose(null);
    }
    
    @Test
    public void testEmpty() {
        Assert.assertTrue(new Transpose().compose(Empty.getInstance()) == Empty.getInstance());
    }
}
