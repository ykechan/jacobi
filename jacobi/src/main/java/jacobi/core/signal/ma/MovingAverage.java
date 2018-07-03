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

/**
 * Implementation of different basic types of Moving-Averages.
 *
 * <p></p>
 *
 * @author Y.K. Chan
 */
public class MovingAverage {

    public double[] simple(double[] signal, double[] output, int win) {
        double sum = this.prefixSum(signal, win);
        int length = signal.length - win;
        for(int i = 0; i < length; i++){
            output[i] = sum / win;
            sum += signal[i + win] - signal[i];
        }
        output[length] = sum / win;
        return output;
    }

    public double[] weighted(double[] signal, double[] output, int win) {
        int denom = win % 2 == 0 ? (win / 2) * (win + 1) : win * ((win + 1) / 2);
        double sum = this.prefixSum(signal, win);
        double wSum = this.weightedPrefixSum(signal, win);
        int length = signal.length - win;
        for(int i = 0; i < length; i++){
            output[i] = sum / denom;
            wSum += win * signal[i + win] - sum;
            sum += signal[i + win] - signal[i];
        }
        output[length] = sum / denom;
        return output;
    }

    public double[] exp(double[] signal, double[] output, double rate) {
        double p = rate;
        double q = 1 - rate;
        output[0] = signal[0];
        for(int i = 1; i < output.length; i++){
            output[i] = p * signal[i] + q * output[i - 1];
        }
        return output;
    }

    protected double prefixSum(double[] signal, int len) {
        double sum = 0.0;
        for(int i = 0; i < len; i++){
            sum += signal[i];
        }
        return sum;
    }

    protected double weightedPrefixSum(double[] signal, int len) {
        double sum = 0.0;
        for(int i = 0; i < len; i++){
            sum += (i + 1) * signal[i];
        }
        return sum;
    }

}
