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

import java.util.Arrays;

/**
 * Implementation of merging part of vector that has split by 2 by Cooley-Tukey algorithm.
 *
 * @author Y.K. Chan
 */
public class CooleyTukeyRadix2 implements CooleyTukeyMerger {

    /**
     * Constructor.
     */
    public CooleyTukeyRadix2() {
        this(new ComplexVector[]{
            ComplexVector.rootsOfUnity(1),
            ComplexVector.rootsOfUnity(1),
            ComplexVector.rootsOfUnity(4).slice(0, 2),
            ComplexVector.rootsOfUnity(6).slice(0, 3),
            ComplexVector.rootsOfUnity(8).slice(0, 4),
            ComplexVector.rootsOfUnity(1),
            ComplexVector.rootsOfUnity(12).slice(0, 6)
        });
    }

    /**
     * Constructor.
     * @param pivots  Array of pivots for calibration to obtain higher numerical stability.
     * @throws  IllegalArgumentException  if pivots is null or length less than 7
     */
    protected CooleyTukeyRadix2(ComplexVector[] pivots) {
        Throw.when()
                .isNull(() -> pivots, () -> "No pivot")
                .isTrue(() -> pivots.length < 7, () -> "Too few pivots. Expected to support 6 partitions.");
        this.pivots = pivots;
    }

    @Override
    public void merge(ComplexVector vector, int offset, int length) {
        this.merge(vector, offset, length, this.select(length / 2));
    }

    /**
     * Select a pivot to use given the number of partition.
     * @param len  Number of partition
     * @return  Pivot as a complex vector
     */
    protected ComplexVector select(int len) {
        int mod = len % 6;
        switch(mod){
            case 0 :
                return this.pivots[6];
            case 1 :
            case 3 :
            case 5 :
                return this.pivots[mod];
            case 2 :
            case 4 :
                return len % 4 == 0 ? this.pivots[4] : this.pivots[2];
            default :
                break;
        }
        throw new UnsupportedOperationException();
    }

    /**
     * Merge a part of the vector as two equal partition.
     * As the merging process moves on and passes certain checkpoint, the pivot value is used for higher
     * numerical stability.
     * @param vector   Input vector
     * @param offset   Offset index of the part of vector
     * @param length   Length of the part of vector
     * @param pivot  Pivot values
     */
    protected void merge(ComplexVector vector, int offset, int length, ComplexVector pivot) {
        int num = length / 2;
        double c =  Math.cos(Math.PI / num);
        double s = -Math.sin(Math.PI / num);
        int arc = num / pivot.length();
        int mid = offset + num;
        for(int k = 0; k < pivot.length(); k++){
            double re = pivot.real[k];
            double im = pivot.imag[k];
            int i = offset + k * arc;
            int j = mid + k * arc;
            for(int n = 0; n < arc; n++){
                double tmpRe = re * vector.real[j] - im * vector.imag[j];
                double tmpIm = re * vector.imag[j] + im * vector.real[j];

                vector.real[j] = vector.real[i] - tmpRe;
                vector.imag[j] = vector.real[i] - tmpIm;

                vector.real[i] += tmpRe;
                vector.imag[i] += tmpIm;

                double tmp = re * c - im * s;
                im = re * s + im * c;
                re = tmp;
            }
        }
    }

    private ComplexVector[] pivots;
}
