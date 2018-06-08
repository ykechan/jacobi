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

import jacobi.core.givens.Givens;

import java.util.Arrays;
import java.util.stream.Collectors;

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
 *                 e<sup>-irp*2&pi;/N</sup><br>
 *               = <sup>N-1</sup>&sum;
 *                 e<sup>-irk*2&pi;/NM</sup>
 *                 e<sup>-irp*2&pi;/N</sup>
 *                 F<sub>r</sub><br>
 *     where F<sub>r</sub> is the DFT of elements with index mod N = r.
 * </p>
 *
 * @author Y.K. Chan
 */
public class CooleyTukeyRadixN implements CooleyTukeyMerger {

    public CooleyTukeyRadixN(int deg) {
        this(ComplexVector.rootsOfUnity(deg).conj());
    }

    public CooleyTukeyRadixN(ComplexVector radix) {
        this.radix = radix;
    }

    @Override
    public void merge(ComplexVector vector, int offset, int length) {
        int width = length / this.radix.length();
        ComplexVector tmp = ComplexVector.of(new double[this.radix.length()], new double[this.radix.length()]);
        Slice slice = new Slice(vector, tmp, offset, length);
        Givens giv = new Givens(1.0, Math.cos(2.0 * Math.PI / length), -Math.sin(2.0 * Math.PI / length));
        Givens kGiv = giv;
        for(int k = 0; k < width; k++){
            ComplexVector out = this.merge(slice, k, kGiv);
            for(int p = 0; p < this.radix.length(); p++){
                slice.in.real[offset + k + p * width] = slice.out.real[p];
                slice.in.imag[offset + k + p * width] = slice.out.imag[p];
            }
            kGiv = new Givens(1.0,
                    giv.rotateX(kGiv.getCos(), kGiv.getSin()),
                    giv.rotateY(kGiv.getCos(), kGiv.getSin()));
        }
    }

    protected ComplexVector merge(Slice slice, int mod, Givens giv) {
        int width = slice.length / this.radix.length();
        for(int p = 0; p < this.radix.length(); p++){
            slice.out.real[p] = 0.0;
            slice.out.imag[p] = 0.0;
            double re = 1.0;
            double im = 0.0;
            for(int r = 0; r < this.radix.length(); r++){
                int index = (r * p) % this.radix.length();

                double c = re * this.radix.real[index] - im * this.radix.imag[index];
                double s = re * this.radix.imag[index] + im * this.radix.real[index];

                int target = slice.offset + mod + r * width;
                System.out.println("p=" + p + ", target=" + target + ", c=" + c + ", s=" + s
                        +",real=" + slice.in.real[target] + ", imag=" + slice.in.imag[target]);

                slice.out.real[p] += c * slice.in.real[target] - s * slice.in.imag[target];
                slice.out.imag[p] += c * slice.in.imag[target] + s * slice.in.real[target];

                double tmp = giv.rotateX(re, im);
                im = giv.rotateY(re, im);
                re = tmp;
            }
        }
        System.out.println(Arrays.stream(slice.out.real).mapToObj(v -> String.valueOf(v)).collect(Collectors.joining(",")));
        System.out.println(Arrays.stream(slice.out.imag).mapToObj(v -> String.valueOf(v)).collect(Collectors.joining(",")));
        return slice.out;
    }

    private ComplexVector radix;

    protected static class Slice {

        public final ComplexVector in;

        public final ComplexVector out;

        public final int offset;

        public final int length;

        public Slice(ComplexVector in, ComplexVector out, int offset, int length) {
            this.in = in;
            this.out = out;
            this.offset = offset;
            this.length = length;
        }
    }
}
