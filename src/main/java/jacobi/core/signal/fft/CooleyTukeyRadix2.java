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

package jacobi.core.signal.fft;

import jacobi.core.signal.ComplexVector;
import jacobi.core.util.Throw;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * Implementation of merging part of vector that has split by 2 by Cooley-Tukey algorithm.
 *
 * <p>
 * To enhance numerical stability, this class uses pre-defined values to calibrate as the loop progress through
 * the half circle in the Argand plane, if the underlying points coincide. This makes this class suitable for processing
 * a larger number of elements than some other radix, e.g. 5.
 * </p>
 *
 * @author Y.K. Chan
 */
public class CooleyTukeyRadix2 implements CooleyTukeyMerger {

    /**
     * Constructor.
     */
    public CooleyTukeyRadix2(boolean forward) {
        this(forward ? 1 : -1, DEFAULT_PIVOTS
                .stream()
                .map(p -> forward ? p.slice(0, p.length()) : p.conj())
                .toArray(n -> new ComplexVector[n]));
    }

    /**
     * Constructor.
     * @param sign  1 for forward transform, -1 for inverse transform
     * @param pivots  Array of pivots for calibration to obtain higher numerical stability.
     * @throws  IllegalArgumentException  if pivots is null or length less than 7
     */
    protected CooleyTukeyRadix2(int sign, ComplexVector[] pivots) {
        Throw.when()
                .isNull(() -> pivots, () -> "No pivot")
                .isTrue(() -> pivots.length < 7, () -> "Too few pivots. Expected to support minimum 6 partitions.");
        this.sign = sign;
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
                throw new IllegalArgumentException("Invalid length " + len);
        }        
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
        double s = -this.sign * Math.sin(Math.PI / num);
        int arc = num / pivot.length();
        int mid = offset + num;
        for(int k = 0; k < pivot.length(); k++){ 
            double re = pivot.real[k];
            double im = pivot.imag[k];
            int i = offset + k * arc;
            int j = mid + k * arc;
            for(int n = 0; n < arc; n++, i++, j++){
                double tmpRe = re * vector.real[j] - im * vector.imag[j];
                double tmpIm = re * vector.imag[j] + im * vector.real[j];

                vector.real[j] = vector.real[i] - tmpRe;
                vector.imag[j] = vector.imag[i] - tmpIm;

                vector.real[i] += tmpRe;
                vector.imag[i] += tmpIm;

                double tmp = re * c - im * s;
                im = re * s + im * c;
                re = tmp;
            }
        }
    }

    private int sign;
    private ComplexVector[] pivots;
    
    private static final List<ComplexVector> DEFAULT_PIVOTS = IntStream.rangeClosed(0, 6)
    		.boxed()
            .map(n -> (ComplexVector)( n < 2 
                    ? ComplexVector.rootsOfUnity(1)
                    : 12 % n == 0 
                        ? ComplexVector.rootsOfUnity(2 * n).slice(0, n)
                        :  ComplexVector.rootsOfUnity(1)
            ))
            .map(p -> p.conj())
            .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
}
