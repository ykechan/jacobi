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

package jacobi.core.decomp.gauss;

import jacobi.api.Matrix;
import jacobi.core.util.Triplet;
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
@JacobiImport("/jacobi/test/data/GaussianDecompTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class GaussainDecompTest {
    
    @JacobiInject(0) 
    public Matrix input;
    
    @JacobiResult(100)
    public Matrix perm;
    
    @JacobiResult(101)
    public Matrix lower;
    
    @JacobiResult(102)
    public Matrix upper;
    
    private GaussianDecomp gaussDecomp;

    public GaussainDecompTest() {
        this.gaussDecomp = new GaussianDecomp();
    }
    
    @Test
    @JacobiImport("5x5")
    @JacobiEquals(expected = 100, actual = 100)
    @JacobiEquals(expected = 101, actual = 101)
    @JacobiEquals(expected = 102, actual = 102)
    public void test5x5() {
        Triplet plu = this.gaussDecomp.compute(this.input);
        this.perm = plu.getLeft();
        this.lower = plu.getMiddle();
        this.upper = plu.getRight();
    }
    
    @Test
    @JacobiImport("Degen 4x4")
    @JacobiEquals(expected = 100, actual = 100)
    @JacobiEquals(expected = 101, actual = 101)
    @JacobiEquals(expected = 102, actual = 102)
    public void testDegen4x4() {
        Triplet plu = this.gaussDecomp.compute(this.input);
        this.perm = plu.getLeft();
        this.lower = plu.getMiddle();
        this.upper = plu.getRight();
    }

}
