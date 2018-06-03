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

public class CooleyTukeyFFT {

    public void compute(ComplexVector in, ComplexVector out, int offset, int length) {

    }

    protected ComplexVector split(ComplexVector vector, ComplexVector buf, int[] radices) {
        int length = vector.length();
        for(int i = 0; i < radices.length; i++){
            ComplexVector from = i % 2 == 0 ? vector : buf;
            ComplexVector to = from == vector ? buf : vector;
            for(int j = 0; j < vector.length(); j += length){
                this.split(from, to, j, length, radices[i]);
            }
            length /= radices[i];
        }
        return radices.length % 2 == 0 ? vector : buf;
    }

    protected CooleyTukeyFFT split(ComplexVector in, ComplexVector out, int offset, int length, int radix) {
        int period = length / radix;
        for(int k = 0; k < length; k++){
            out.real[offset + (k % radix) * period + (k / radix)] = in.real[offset + k];
            out.imag[offset + (k % radix) * period + (k / radix)] = in.imag[offset + k];
        }
        return this;
    }
}
