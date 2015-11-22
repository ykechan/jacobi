/*
 * Copyright (C) 2015 Y.K. Chan
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
package jacobi.test.util;

import jacobi.api.Matrix;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Y.K. Chan
 */
@RunWith(JacobiJUnit4ClassRunner.class)
@JacobiImport("/jacobi/test/data/JacobiJUnit4ClassRunnerTest.xlsx")
public class JacobiJUnit4ClassRunnerTest {
    
    @JacobiInject(0)
    public Matrix z;
    
    @JacobiInject(1)
    public Matrix a;
    
    @JacobiInject(2)
    @JacobiResult(3)
    public Matrix b;
    
    @JacobiInject(7)
    @JacobiResult(4)
    public Matrix c;
    
    @Test
    @JacobiImport("testReadEquals3x3")
    @JacobiEquals(expected = 1, actual = 3)
    public void testReadEquals3x3() {
        
    }
    
    @Test
    @JacobiImport("testReadEquals5x5")
    @JacobiEquals(expected = 0, actual = 4)
    public void testReadEquals5x5() {
        System.out.println(this.c);
    }
    
}
