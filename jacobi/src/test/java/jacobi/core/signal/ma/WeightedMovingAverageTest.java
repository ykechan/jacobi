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

package jacobi.core.signal.ma;

import jacobi.api.ma.Initial;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

public class WeightedMovingAverageTest {

    @Test
    public void testSumOfArithmeticSeq() {
        for(int i = 1; i < 256; i++){
            int sum = 0;
            for(int j = 1; j <= i; j++){
                sum += j;
            }
            Assert.assertEquals(sum, this.mock(Initial.ZERO).sumOfArthSeq(i));
        }
    }

    @Test
    public void testIntermediateResultOverflowsButSumDoesntOverflow() {
        long num = 2 * Short.MAX_VALUE + 1;
        long sum = 0;
        for(long i = 1; i <= num; i++){
            sum += i;
        }
        Assert.assertTrue(Integer.MAX_VALUE > sum);
        Assert.assertTrue(2 * sum > Integer.MAX_VALUE);
        Assert.assertEquals(sum, this.mock().sumOfArthSeq(2 * Short.MAX_VALUE + 1));
    }

    @Test
    public void testPositiveRandomSignal() {
        Random rand = new Random(Double.doubleToLongBits(-Math.E));
        double[] signal = IntStream.range(0, 17).mapToDouble(i -> rand.nextDouble()).toArray();
        int lag = 5;
        Assert.assertArrayEquals(this.shrink(signal, 5), this.mock(Initial.SHRINK).compute(signal, 5), 1e-12);
        Assert.assertArrayEquals(
                this.shrink(this.padLeft(signal, 4, 0.0), 5),
                this.mock(Initial.ZERO).compute(signal, 5), 1e-12);
    }

    @Test
    public void testRandomSignalInShrinkMode() {
        Random rand = new Random(Double.doubleToLongBits(-Math.E));
        double[] signal = IntStream.range(0, 17).mapToDouble(i -> rand.nextDouble()).toArray();
        int lag = 5;
        Assert.assertArrayEquals(this.shrink(signal, lag), this.mock(Initial.SHRINK).compute(signal, lag), 1e-12);
    }

    @Test
    public void testZeroModeIsEquivToPaddingZeroBeforeShrink() {
        Random rand = new Random(Double.doubleToLongBits(-Math.E));
        double[] signal = IntStream.range(0, 17).mapToDouble(i -> rand.nextDouble()).toArray();
        int lag = 5;
        Assert.assertArrayEquals(
                this.shrink(this.padLeft(signal, lag-1, 0.0), lag),
                this.mock(Initial.ZERO).compute(signal, lag),
                1e-12
        );
    }

    @Test
    public void testPadModeIsEquivToPadding1stElementBeforeShrink() {
        Random rand = new Random(Double.doubleToLongBits(-Math.E));
        double[] signal = IntStream.range(0, 17).mapToDouble(i -> rand.nextDouble()).toArray();
        int lag = 5;
        Assert.assertArrayEquals(
                this.shrink(this.padLeft(signal, lag-1, signal[0]), lag),
                this.mock(Initial.PAD).compute(signal, lag),
                1e-12
        );
    }

    @Test
    public void testImplShouldBeEquivToOracleImplForVariousLengths() {
        Random rand = new Random(Double.doubleToLongBits(Math.E * Math.PI));
        int limit = 64;
        double max = 100.0;
        for(int n = 3; n < limit; n++){
            double[] signal = IntStream.range(0, n).mapToDouble(i -> max * rand.nextDouble()).toArray();
            for(int lag = 2; lag < n; lag++){
                Assert.assertArrayEquals(
                        "Length = " + n + ", Lag = " + lag + ", Shrink",
                        this.shrink(signal, lag),
                        this.mock(Initial.SHRINK).compute(signal, lag),
                        1e-8);
                Assert.assertArrayEquals(
                        "Length = " + n + ", Lag = " + lag + ", Zero",
                        this.shrink(this.padLeft(signal, lag-1, 0.0), lag),
                        this.mock(Initial.ZERO).compute(signal, lag),
                        1e-8);
                Assert.assertArrayEquals(
                        "Length = " + n + ", Lag = " + lag + ", Pad",
                        this.shrink(this.padLeft(signal, lag-1, signal[0]), lag),
                        this.mock(Initial.PAD).compute(signal, lag),
                        1e-8);
            }
        }
    }

    private double[] shrink(double[] signal, int lag) {
        int len = 0;
        double[] out = new double[signal.length];
        for(len = 0; len < out.length; len++){
            if(len + lag - 1 >= signal.length){
                break;
            }
            out[len] = 0.0;
            long total = 0;
            for(int i = 0; i < lag; i++){
                total += (i + 1);
                out[len] += (i + 1) * signal[len + i];
            }
            out[len] /= total;
        }
        return Arrays.copyOf(out, len);
    }

    private double[] padLeft(double[] input, int num, double defaultValue){
        double[] output = new double[input.length + num];
        System.arraycopy(input, 0, output, num, input.length);
        Arrays.fill(output, 0, num, defaultValue);
        return output;
    }

    private WeightedMovingAverage mock() {
        return this.mock(Initial.ZERO);
    }

    private WeightedMovingAverage mock(Initial mode) {
        return new WeightedMovingAverage(mode);
    }

}