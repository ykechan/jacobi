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

public class SimpleMovingAverage extends AbstractMovingAverage {

    public SimpleMovingAverage(Initial mode) {
        super(mode);
    }

    @Override
    protected void apply(double[] signal, double[] output, int lag) {
        int delta = this.mode == Initial.SHRINK ? lag - 1 : 0;
        int begin = lag - delta;
        double sum = this.init(signal, output, lag);
        for(int i = begin; i < output.length; i++){
            sum += signal[i + delta] - signal[i + delta - lag];
            output[i] = sum / lag;
        }
    }

    protected double init(double[] sig, double[] out, int lag) {
        double sum = 0.0;
        switch(this.mode){
            case SHRINK:
                for(int i = 0; i < lag; i++){
                    sum += sig[i];
                }
                out[0] = sum / lag;
                return sum;
            case ADAPT:
                for(int i = 0; i < lag; i++){
                    sum += sig[i];
                    out[i] = sum / (i + 1);
                }
                return sum;
            case PAD:
                for(int i = 0; i < lag; i++){
                    sum += sig[i];
                    out[i] = (sum + (lag - i - 1) * sig[0]) / lag;
                }
                return sum;
            case ZERO:
                for(int i = 0; i < lag; i++){
                    sum += sig[i];
                    out[i] = sum / lag;
                }
                return sum;
            default :
                break;
        }
        throw new IllegalStateException("Unknown initial mode " + this.mode);
    }

}
