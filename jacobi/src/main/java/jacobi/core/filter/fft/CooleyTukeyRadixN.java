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

import jacobi.core.util.Throw;

/**
 * Implementation of merging part of vector that has split by N by Cooley-Tukey algorithm.
 *
 * <p>
 *     In the general case, suppose the length of input is N * M for some integer N and M, consider the DFT<br>
 *     X[k] = <sup>NM-1</sup>&sum;x[n] e<sup>-i*nk*2&pi;/NM</sup><br>
 *          = <sup>M-1</sup>&sum;<sup>N-1</sup>&sum;x[m*N+r] e<sup>-i(m*N+r)k*2&pi;/NM</sup><br>
 *          = <sup>M-1</sup>&sum;<sup>N-1</sup>&sum;x[m*N+r]
 *             e<sup>-imk*2&pi;/M</sup>
 *             e<sup>-irk*2&pi;/NM</sup>
 *          = <sup>N-1</sup>&sum;e<sup>-irk*2&pi;/NM</sup>F<sub>r</sub><br>
 *     where F<sub>r</sub> is the DFT of elements with index mod N = r.
 * </p>
 * <p>
 *     Consider the periodity of DFT,<br>
 *     X[k + pM] = <sup>N-1</sup>&sum;<sup>M-1</sup>&sum;x[m*N+r]
 *                 e<sup>-im(k + pM)*2&pi;/M</sup>
 *                 e<sup>-ir(k + pM)*2&pi;/NM</sup><br>
 *               = <sup>N-1</sup>&sum;<sup>M-1</sup>&sum;x[m*N+r]
 *                 e<sup>-imk*2&pi;/M</sup>
 *                 e<sup>-imp*2&pi;</sup>
 *                 e<sup>-irk*2&pi;/NM</sup>
 *                 e<sup>-irp*2&pi;/M</sup><br>
 *               = <sup>N-1</sup>&sum;
 *                 e<sup>-irk*2&pi;/NM</sup>
 *                 e<sup>-irp*2&pi;/M</sup>
 *                 F<sub>r</sub><br>
 *     where F<sub>r</sub> is the DFT of elements with index mod N = r.
 * </p>
 *
 * @author Y.K. Chan
 */
public class CooleyTukeyRadixN implements CooleyTukeyMerger {

    public CooleyTukeyRadixN(ComplexVector radix) {
        this.radix = radix;
    }

    @Override
    public void merge(ComplexVector vector, int offset, int length) {
        int width = length / this.radix.length();
        ComplexVector tmp = ComplexVector.of(new double[this.radix.length()], new double[this.radix.length()]);
        double c = Math.cos(2 * Math.PI / length);
        double s = -Math.sin(2 * Math.PI / length);
        for(int k = 0; k < width; k++){

        }
    }

    protected void merge(ComplexVector in, ComplexVector out, int offset, int length, int mod) {
        int width = length / out.length();
        for(int r = 0; r < out.length(); r++){
            out.real[r] = in.real[offset + mod];
            out.imag[r] = in.imag[offset + mod];
            for(int p = 1; p < out.length(); p++){

            }
        }
    }

    private ComplexVector radix;
}
