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

public class WeightedMovingAverage extends AbstractMovingAverage {

    public WeightedMovingAverage(Initial mode) {
        super(mode);
    }

    @Override
    protected void apply(double[] signal, double[] output, int lag) {
        double[] win = this.init(signal, output, lag);
        double sum = win[0], wSum = win[1];
        int begin = this.mode == Initial.SHRINK ? 1 : lag;
        int delta = this.mode == Initial.SHRINK ? lag - 1 : 0;
        int denom = this.sumOfArthSeq(lag);
        for(int i = begin; i < output.length; i++){
            wSum += lag * signal[i + delta] - sum;
            sum += signal[i + delta] - signal[i + delta - lag];
            output[i] = wSum / denom;
        }
    }

    protected double[] init(double[] sig, double[] out, int lag) {
        int denom = this.mode == Initial.ADAPT ? 0 : this.sumOfArthSeq(lag);
        double sum = this.mode == Initial.PAD ? lag * sig[0] : 0.0;
        double wSum = this.mode == Initial.PAD ? denom * sig[0] : 0.0;
        for(int i = 0; i < lag; i++){
            sum += sig[i];
            wSum += (i + 1) * sig[i];
            switch(this.mode){
                case SHRINK:
                    break;
                case ADAPT:
                    out[i] = wSum / (denom += (i + 1));
                    break;
                case PAD:
                    wSum -= sum;
                    sum -= sig[0];
                case ZERO:
                    out[i] = wSum / denom;
                    break;
                default :
                    throw new IllegalStateException("Unknown initial mode " + this.mode);
            }
        }
        if(this.mode == Initial.SHRINK){
            out[0] = wSum / denom;
        }
        return new double[]{sum, wSum};
    }

    protected int sumOfArthSeq(int num) {
        return num % 2 == 0 ? (num / 2) * (num + 1) : num * ((num + 1) / 2);
    }

}
