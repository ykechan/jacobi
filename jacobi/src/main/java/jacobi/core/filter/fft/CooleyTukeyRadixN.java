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

/**
 * Implementation of merging part of vector that has split by N by Cooley-Tukey algorithm.
 * 
 * <p>
 * Consider the DFT of length NM for some radix N and some width length M.<br>
 * <br>
 * X[k + pM] = <sup>NM-1</sup>&sum; x[j] e<sup>-ij(k + pM)*2&pi;/NM</sup><br>
 *           = <sup>N-1</sup>&sum;
 *             <sup>M-1</sup>&sum;
 *             x[n + mN] e<sup>-i(n + mN)(k + pM)*2&pi;/NM</sup><br>
 *           = <sup>N-1</sup>&sum;
 *             <sup>M-1</sup>&sum;
 *             x[n + mN] e<sup>-i(nk + npM + mkN + mpMN)*2&pi;/NM</sup><br>
 *           = <sup>N-1</sup>&sum;
 *             <sup>M-1</sup>&sum;
 *             x[n + mN] e<sup>-i(nk/NM + np/N + mk/M + mp)*2&pi;</sup><br>
 *           = <sup>N-1</sup>&sum;
 *             <sup>M-1</sup>&sum;
 *             e<sup>-i(nk/NM)2&pi;</sup>e<sup>-i(np/N)2&pi;</sup>F[n]<br>
 * where F[n] is the DFT of the elements which index mod N is n.
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
        double zRe = Math.cos(2 * Math.PI / length);
        double zIm = -Math.sin(2 * Math.PI / length);
    }
    
    
    private ComplexVector radix;
    
    protected static class Slice {
        
        public final ComplexVector vector;
        
        public final int offset;
        
        public final int length;

        public Slice(ComplexVector vector, int offset, int length) {
            this.vector = vector;
            this.offset = offset;
            this.length = length;
        }
        
    }
}
