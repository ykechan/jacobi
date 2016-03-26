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
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.util.Jacobi;
import jacobi.test.util.JacobiJUnit4ClassRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Y.K. Chan
 */
@JacobiImport("/jacobi/test/data/HessenbergTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class HessenbergTest {
    
    @JacobiInject(0)
    public Matrix input;
    
    @JacobiInject(1)
    public Matrix step1;
    
    @JacobiInject(2)
    public Matrix step2;
    
    @JacobiInject(3)
    public Matrix step3;
    
    @JacobiInject(4)
    public Matrix step4;
    
    @JacobiInject(5)
    public Matrix step5;
    
    @JacobiInject(6)
    public Matrix step6;
    
    @Test
    @JacobiImport("4x4")
    public void test4x4() {
        this.assertByStep(step1, step2, step3, step4).compute(this.input);
    }
    
    @Test
    @JacobiImport("5x5")
    public void test5x5() {
        this.assertByStep(step1, step2, step3, step4, step5, step6).compute(this.input);
    }

    private Hessenberg assertByStep(Matrix... step) {
        return new Hessenberg(){

            @Override
            protected void applyRight(Matrix matrix, int i, HouseholderReflector hh) {
                for(int k = i + 2; k < matrix.getRowCount(); k++){
                    matrix.set(k, i, 0.0);
                }
                Jacobi.assertEquals(step[2*i], matrix);
                super.applyRight(matrix, i, hh);
                Jacobi.assertEquals(step[2*i + 1], matrix);
            }
            
            
        };
    }
}
