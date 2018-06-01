/*
 * The MIT License
 *
 * Copyright (c) 2018 Y.K. Chan.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package jacobi.core.filter.fft;

import org.junit.Assert;
import org.junit.Test;

import java.util.stream.IntStream;

import static org.junit.Assert.*;

public class CooleyTukeyRadix2Test {

    @Test
    public void testSelectingTheBestPivotForArcLength1To128() {
        ComplexVector[] pivots = IntStream.range(0, 7)
                .mapToObj(n -> ComplexVector.of(new double[n], new double[n]))
                .toArray(n -> new ComplexVector[n]);
        // no special value for 3/5
        pivots[3] = pivots[1];
        pivots[5] = pivots[1];
        CooleyTukeyRadix2 fft = new CooleyTukeyRadix2(pivots);
        for(int i = 1; i < 129; i++){
            ComplexVector pivot = fft.select(i);
            Assert.assertEquals("Pivot " + pivot.length() + " for " + i + " is not correct.", 0, i % pivot.length());
            for(ComplexVector piv : pivots){
                if(piv.length() > pivot.length()){
                    Assert.assertTrue("Pivot " + pivot.length() + " for " + i + " is not optimal: "
                            + piv.length() + " is better.", i % piv.length() != 0);
                }
            }
        }
    }

}