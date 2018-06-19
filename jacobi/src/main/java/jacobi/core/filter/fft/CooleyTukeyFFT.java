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

import jacobi.api.annotations.Pure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.stream.IntStream;

/**
 * Implementation of Fast Fourier Transform in Cooley-Tukey algorithm.
 *
 * @author Y.K. Chan
 */
@Pure
public class CooleyTukeyFFT {

    public CooleyTukeyFFT(int[] baselines, CooleyTukeyMerger[] mergers) {
        this.mergers = Arrays.copyOf(mergers, mergers.length);
        this.radices = IntStream.range(0, mergers.length)
            .filter(i -> i > 1)
            .filter(i -> mergers[i] != null)
            .toArray();
        this.baselines = Arrays.copyOf(baselines, baselines.length);
    }

    public BinaryOperator<ComplexVector> of(int len) {
        int[] factors = this.factorize(len, this.radices, this.baselines);
        return (in, buf) -> this.compute(in, buf, factors);
    }

    protected ComplexVector compute(ComplexVector in, ComplexVector buf, int[] factors) {
        ComplexVector vector = this.split(in, buf, factors);
        int base = factors[factors.length - 1];
        this.transform(vector, base);
        for(int i = factors.length - 2; i >= 0; i--){
            int f = factors[i];
            this.merge(vector, base * f, base);
            base *= f;
        }
        return vector;
    }

    protected void merge(ComplexVector vector, int width, int radix) {
        CooleyTukeyMerger merger = this.mergers[radix];
        for(int i = 0; i < vector.length(); i += width){
            merger.merge(vector, i, width);
        }
    }

    /**
     * Transform every partition of the input vector.
     * @param vector  Input vector
     * @param baseline  Length of partition
     */
    protected void transform(ComplexVector vector, int baseline) {
        if(baseline == 1){
            return;
        }
        CooleyTukeyMerger merger = this.mergers[0];
        for(int i = 0; i < vector.length(); i += baseline){
            merger.merge(vector, i, baseline);
        }
    }

    /**
     * Split the vector all the way by given factors, exclude the baseline value.
     * @param vector  Input vector
     * @param buf  Working buffer
     * @param factors  
     * @return  Split vector. The instance is either vector or buf.
     */
    protected ComplexVector split(ComplexVector vector, ComplexVector buf, int[] factors) {
        int length = vector.length();
        for(int i = 0; i < factors.length; i++){
            ComplexVector from = i % 2 == 0 ? vector : buf;
            ComplexVector to = from == vector ? buf : vector;
            for(int j = 0; j < vector.length(); j += length){
                this.split(from, to, j, length, factors[i]);
            }
            length /= factors[i];
        }
        return factors.length % 2 == 0 ? vector : buf;
    }

    /**
     * Split part of the input vector to output vector in given radix.
     * For example, if radix is 2, the method split by even and odd indices, and place even elements in the 1st half
     * and odd elements in the 2nd half.
     * @param in  Input vector
     * @param out  Output Vector
     * @param offset  Offset index of the part of vector
     * @param length  Length of the part of vector
     * @param radix  Split radix
     * @return  This
     */
    protected CooleyTukeyFFT split(ComplexVector in, ComplexVector out, int offset, int length, int radix) {
        int period = length / radix;
        for(int k = 0; k < length; k++){
            out.real[offset + (k % radix) * period + (k / radix)] = in.real[offset + k];
            out.imag[offset + (k % radix) * period + (k / radix)] = in.imag[offset + k];
        }
        return this;
    }

    /**
     * Factorize an integer with given factors, until a baseline value is hit.
     * 1 is automatically considered to be a baseline value.
     * @param number   Integer value
     * @param factor   Supported factors. The ordering represents the priority of factorization.
     * @param baseline  Supported baseline values.
     * @return  A list of factors with a baseline value at the end
     */
    protected int[] factorize(int number, int[] factor, int[] baseline) {
        int left = number;
        List<Integer> factors = new ArrayList<>(16);
        int base = 0;
        for(int i = 0; i < baseline.length; i++){
            if(left % baseline[i] == 0){
                base = baseline[i];
                left /= base;
                break;
            }
        }
        while(left > 1){
            int radix = 0;
            for(int i = 0; i < factor.length; i++){
                if(left % factor[i] == 0){
                    radix = factor[i];
                    break;
                }
            }
            if(radix == 0){
                if(base > 0){
                    left *= base;
                    base = 0;
                    continue;
                }
                break;
            }
            factors.add(radix);
            left /= radix;
        }
        factors.add(base > 0 ? base : left);
        return factors.stream().mapToInt(n -> n).toArray();
    }

    private CooleyTukeyMerger[] mergers;
    private int[] radices, baselines;
}
