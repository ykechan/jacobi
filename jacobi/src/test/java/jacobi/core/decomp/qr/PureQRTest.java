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

import jacobi.core.decomp.qr.step.QRStep;
import jacobi.core.decomp.qr.step.PureQR;
import jacobi.api.Matrix;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.util.Jacobi;
import jacobi.test.util.JacobiJUnit4ClassRunner;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Y.K. Chan
 */
@JacobiImport("/jacobi/test/data/PureQRTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class PureQRTest {
        
    @JacobiInject(-1)
    public Map<Integer, Matrix> steps;
    
    @Test
    @JacobiImport("3x3")
    public void test3x3() {
        QRStep qrStep = new PureQR();
        Matrix input = this.steps.get(1);
        int k = 1;
        while(steps.containsKey(++k)){
            qrStep.compute(input, null, 0, input.getRowCount(), true);
            Jacobi.assertEquals(steps.get(k), input);
        }
    }
    
    @Test
    @JacobiImport("3x3(2)")
    public void test3x3Two() {
        QRStep qrStep = new PureQR();
        Matrix input = this.steps.get(1);
        qrStep.compute(input, null, 0, input.getRowCount(), true);
        Jacobi.assertEquals(steps.get(2), input);
    }

    @Test
    @JacobiImport("4x4")
    public void test4x4() {
        QRStep qrStep = new PureQR();
        Matrix input = this.steps.get(0);
        int k = 0;
        while(steps.containsKey(++k)){
            qrStep.compute(input, null, 0, input.getRowCount(), true);
            Jacobi.assertEquals(steps.get(k), input);
        }
    }
    
}
