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
import jacobi.api.Matrix;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.util.JacobiJUnit4ClassRunner;
import java.util.Arrays;
import java.util.stream.IntStream;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Y.K. Chan
 */
@JacobiImport("/jacobi/test/data/BasicQRTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class BasicQRTest {
    
    @JacobiInject(1)
    public Matrix input5x5;
    
    @JacobiInject(2)
    public Matrix input7x7;
    
    @Test
    @JacobiImport("Data")
    public void test5x5() {
        QRStrategy impl = new BasicQR(this.mockStep(
           1, 2, 3, 4
        ));
        impl.compute(input5x5, null, true);
        for(int i = 1; i < this.input5x5.getRowCount(); i++){
            Assert.assertEquals(0.0, this.input5x5.get(i, i - 1), 1e-12);
        }
    }
   

    private QRStep mockStep(int... order) {
        return (m, p, begin, end, full) -> {
            for(int i = 0; i < order.length; i++){
                if(order[i] >= begin && order[i] < end){ 
                    int k = order[i];
                    order[i] = -1;
                    m.set(k, k - 1, 0.0);
                    return;
                }
            }
            throw new UnsupportedOperationException("Unable to find converge point in " 
                    + Arrays.toString(order)
                    + " within [" + begin + "," + end + ").");
        };
    }
}
