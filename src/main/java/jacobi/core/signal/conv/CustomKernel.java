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

package jacobi.core.signal.conv;

import jacobi.core.util.Throw;

import java.util.Arrays;

public class CustomKernel implements Kernel {

    public CustomKernel(double[] kernel) {
        Throw.when()
             .isNull(() -> kernel, () -> "No kernel.")
             .isTrue(() -> kernel.length % 2 == 0, () -> "Kernel size must be odd.");
        this.kernel = Arrays.copyOf(kernel, kernel.length);
    }

    @Override
    public int size() {
        return this.kernel.length;
    }

    @Override
    public double[] apply(double[] signal, double[] output) {
        int delta = this.size() / 2;
        int begin = delta;
        int end = output.length - begin;
        for(int i = 0; i < begin; i++){
            double u = 0.0;
            for(int k = -delta; k < 0; k++){
                u += this.kernel[delta + k] * signal[0];
            }
            for(int k = delta; k < this.kernel.length; k++){
                u += this.kernel[k] * signal[i - delta + k];
            }
            output[i] = u;
        }
        for(int i = begin; i < end; i++){
            double u = 0.0;
            for(int k = 0; k < this.kernel.length; k++){
                u += this.kernel[k] * signal[i - delta + k];
            }
            output[i] = u;
        }
        for(int i = end; i < output.length; i++){
            double u = 0.0;
            double tail = signal[signal.length - 1];
            for(int k = -delta; k < 0; k++){
                u += this.kernel[delta + k] * signal[i + k];
            }
            for(int k = delta; k < this.kernel.length; k++){
                u += this.kernel[k] * (i - delta + k < signal.length ? signal[i - delta + k] : tail);
            }
            output[i] = u;
        }
        return output;
    }

    private double[] kernel;
}
