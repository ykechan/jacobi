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

import jacobi.api.Matrices;
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
@JacobiImport("/jacobi/test/data/DeterminantTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class DeterminantTest {
    
    @JacobiInject(1)
    public Matrix matrix3x3;
    
    @JacobiInject(41)
    public Matrix matrix4x4;
    
    @JacobiInject(2)
    public Matrix matrix2x2Top;
    
    @JacobiInject(3)
    public Matrix matrix2x2Mid;
    
    @JacobiInject(4)
    public Matrix matrix2x2Btm;
    
    @JacobiResult(11)
    public Matrix det3x3;
    
    @JacobiResult(12)
    public Matrix det2x2Top;
    
    @JacobiResult(13)
    public Matrix det2x2Mid;
    
    @JacobiResult(14)
    public Matrix det2x2Btm; 
    
    @JacobiResult(401)
    public Matrix det4x4; 

    @Test
    @JacobiImport("3x3_1")
    //@JacobiEquals(expected = 11, actual = 11)
    @JacobiEquals(expected = 12, actual = 12)
    @JacobiEquals(expected = 13, actual = 13)
    @JacobiEquals(expected = 14, actual = 14)
    public void test2x2One() {        
        this.det2x2Top = this.single(new Determinant().compute(this.matrix2x2Top));
        this.det2x2Mid = this.single(new Determinant().compute(this.matrix2x2Mid));
        this.det2x2Btm = this.single(new Determinant().compute(this.matrix2x2Btm));
    }
    
    @Test
    @JacobiImport("3x3_1")
    //@JacobiEquals(expected = 11, actual = 11)
    public void test3x3One() {        
        this.det3x3 = this.single(new Determinant().compute(this.matrix3x3));
    }
    
    @Test
    @JacobiImport("3x3_2")
    //@JacobiEquals(expected = 11, actual = 11)
    @JacobiEquals(expected = 12, actual = 12)
    @JacobiEquals(expected = 13, actual = 13)
    @JacobiEquals(expected = 14, actual = 14)
    public void test2x2Two() {        
        this.det2x2Top = this.single(new Determinant().compute(this.matrix2x2Top));
        this.det2x2Mid = this.single(new Determinant().compute(this.matrix2x2Mid));
        this.det2x2Btm = this.single(new Determinant().compute(this.matrix2x2Btm));
    }
    
    @Test
    @JacobiImport("3x3_2")
    @JacobiEquals(expected = 11, actual = 11)
    public void test3x3Two() {        
        this.det3x3 = this.single(new Determinant().compute(this.matrix3x3));
    }
    
    @Test
    @JacobiImport("4x4_1")
    @JacobiEquals(expected = 401, actual = 401)
    public void test4x4One() {        
        this.det4x4 = this.single(new Determinant().compute(this.matrix4x4));
    }
    
    @Test
    @JacobiImport("4x4_2")
    @JacobiEquals(expected = 401, actual = 401)
    public void test4x4Two() {        
        this.det4x4 = this.single(new Determinant().compute(this.matrix4x4));
    }
    
    @Test(expected = RuntimeException.class)
    public void testNullMatrix() {
        new Determinant().compute(null);
    }
    
    @Test(expected = RuntimeException.class)
    public void testNonSquareMatrix() {
        new Determinant().compute(Matrices.zeros(5, 4));
    }
    
    private Matrix single(double elem) {
        return Matrices.diag(new double[]{elem});
    }
}
