/*
 * The MIT License
 *
 * Copyright (c) 2018 Y.K. Chan
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

package jacobi.core.signal.ma;

import jacobi.api.ma.Initial;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class AbstractMovingAverageTest {

    @Test(expected = IllegalArgumentException.class)
    public void testShouldNotAcceptNegativeLag() {
        this.mock(Initial.ADAPT).compute(new double[0], -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testShouldNotAcceptZeroLag() {
        this.mock(Initial.ADAPT).compute(new double[0], 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLagLargerThanSignalLength() {
        this.mock(Initial.ADAPT).compute(new double[12], 16);
    }

    @Test
    public void testProtectedFieldIsTheConstructorParam() {
        AtomicInteger counter = new AtomicInteger(0);
        for(Initial m : Initial.values()){
            new AbstractMovingAverage(m) {

                @Override
                protected void apply(double[] signal, double[] output, int lag) {
                    counter.incrementAndGet();
                    Assert.assertEquals(m, this.mode);
                }

            }.compute(new double[10], 10);
        }
        Assert.assertEquals(Initial.values().length, counter.get());
    }

    @Test
    public void testSignalLengthShouldBeTheSameAsOutputLengthNotInShrinkMode() {
        for(Initial mode : Initial.values()){
            if(mode == Initial.SHRINK){
                continue;
            }
            AtomicInteger counter = new AtomicInteger(0);
            int len = 10;
            new AbstractMovingAverage(mode) {

                @Override
                protected void apply(double[] signal, double[] output, int lag) {
                    counter.incrementAndGet();
                    Assert.assertEquals(len, output.length);
                }

            }.compute(new double[len], 3);
            Assert.assertEquals(1, counter.get());
        }
    }

    @Test
    public void testShrunkMALengthIsEnoughForEveryMAElement() {
        AtomicInteger counter = new AtomicInteger(0);
        int len = 10;
        new AbstractMovingAverage(Initial.SHRINK) {

            @Override
            protected void apply(double[] signal, double[] output, int lag) {
                counter.incrementAndGet();

                for(int i = 0; i < output.length; i++){
                    int first = i;
                    int last = i + lag - 1;
                    Assert.assertTrue(first >= 0 && first < signal.length);
                    Assert.assertTrue(last >= 0 && last < signal.length);
                }
            }

        }.compute(new double[len], 3);
        Assert.assertEquals(1, counter.get());
    }

    private AbstractMovingAverage mock(Initial mode){
        return this.mock(mode, new AtomicInteger(0));
    }

    private AbstractMovingAverage mock(Initial mode, AtomicInteger counter){
        return new AbstractMovingAverage(mode) {

            @Override
            protected void apply(double[] signal, double[] output, int lag) {
                counter.incrementAndGet();
            }

        };
    }

}