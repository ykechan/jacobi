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

package jacobi.core.signal;

import jacobi.core.util.Throw;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * A vector in complex field.
 * For all intents and purposes this class is NOT immutable.
 *
 * @author Y.K. Chan
 */
public class ComplexVector {

    /**
     * Get the n roots of unity as a complex vector.
     * For some n special values may be used for numerical accuracy.
     * @param deg  Degree n
     * @return  Roots of unity as a complex vector.
     * @throws  IllegalArgumentException if n is not positive
     */
    public static ComplexVector rootsOfUnity(int deg) {
        Throw.when().isFalse(() -> deg > 0, () -> "Invalid degree of roots.");
        if(COS_12.length % deg == 0){
            int period = COS_12.length / deg;
            return ComplexVector.of(
                    IntStream.range(0, deg).mapToDouble(i -> COS_12[i * period]).toArray(),
                    IntStream.range(0, deg).mapToDouble(i -> SIN_12[i * period]).toArray()
            );
        }
        if(deg == 8){
            return ComplexVector.of(COS_8, SIN_8).slice(0, deg);
        }
        double arc = 2 * Math.PI / deg;
        return ComplexVector.of(
            IntStream.range(0, deg).mapToDouble(i -> Math.cos(i * arc)).toArray(),
            IntStream.range(0, deg).mapToDouble(i -> Math.sin(i * arc)).toArray()
        );
    }

    /**
     * Create a complex vector with the given backing arrays.
     * @param real  Real parts
     * @param imag  Imaginary parts
     * @return  Complex vector
     * @throws  IllegalArgumentException is real / imaginary part is null, are the same array or not in the same size
     */
    public static ComplexVector of(double[] real, double[] imag) {
        return new ComplexVector(real, imag);
    }

    /**
     * Real parts and imaginary parts.
     */
    public final double[] real, imag;

    /**
     * Constructor.
     * @param real  Real parts
     * @param imag  Imaginary parts
     * @throws  IllegalArgumentException is real / imaginary part is null, are the same array or not in the same size
     */
    private ComplexVector(double[] real, double[] imag) {
        if(real == null || imag == null){
            throw new IllegalArgumentException("No real or imaginary part.");
        }
        if(real == imag || real.length != imag.length){
            throw new IllegalArgumentException("Dimension mismatch.");
        }
        this.real = real;
        this.imag = imag;
    }

    /**
     * Get the dimension of this vector.
     * @return  Number of elements.
     */
    public int length() {
        return this.real.length;
    }

    /**
     * Get the sub-vector of this vector.
     * @param begin  Begin index
     * @param end  End index, exclusive
     * @return  Sub-vector
     */
    public ComplexVector slice(int begin, int end) {
        return ComplexVector.of(
                Arrays.copyOfRange(this.real, begin, end),
                Arrays.copyOfRange(this.imag, begin, end)
        );
    }

    /**
     * Find the complex conjugate of this vector.
     * @return  Complex conjugate
     */
    public ComplexVector conj() {
        return ComplexVector.of(
                Arrays.copyOf(this.real, this.real.length),
                Arrays.stream(this.imag).map(v -> -v).toArray()
        );
    }

    private static final double D_SQ3_OVER_2 = Math.sqrt(3.0) / 2.0;

    private static final double D_1_OVER_SQ2 = 1.0 / Math.sqrt(2.0);

    private static final double[] SIN_12 = {
        0.0,  0.5,  D_SQ3_OVER_2,  1.0,  D_SQ3_OVER_2,  0.5,
        0.0, -0.5, -D_SQ3_OVER_2, -1.0, -D_SQ3_OVER_2, -0.5
    };

    private static final double[] COS_12 = IntStream.range(0, SIN_12.length)
            .mapToDouble(i -> SIN_12[(3 + i) % SIN_12.length])
            .toArray();

    private static final double[] SIN_8 = {
            0.0,  D_1_OVER_SQ2,  1.0,  D_1_OVER_SQ2,
            0.0, -D_1_OVER_SQ2, -1.0, -D_1_OVER_SQ2
    };

    private static final double[] COS_8 = IntStream.range(0, SIN_8.length)
            .mapToDouble(i -> SIN_8[(2 + i) % SIN_8.length])
            .toArray();
}
