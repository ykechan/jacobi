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

/**
 * Implementation of merging part of vector that has split by 3 by Cooley-Tukey algorithm.
 *
 * <p>Similar to radix 2, this is only differed by partitioning the vector into 3 parts.</p>
 *
 * <p>
 *     Consider the DFT of a vector of length 3N, i.e.<br>
 *     X[k] = <sup>3N-1</sup>&sum;x[n] e<sup>-i*nk*2&pi;/3N</sup><br>
 *          = <sup>N-1</sup>&sum;
 *             x[3n] e<sup>-i*3nk*2&pi;/3N</sup>
 *           + x[3n + 1] e<sup>-i*(3n + 1)k*2&pi;/3N</sup>
 *           + x[3n + 2] e<sup>-i*(3n + 2)k*2&pi;/3N</sup>
 *          <br>
 *          = <sup>N-1</sup>&sum;
 *              x[3n] e<sup>-i*nk*2&pi;/N</sup>
 *            + x[3n + 1] e<sup>-i*nk*2&pi;/N</sup> e<sup>-i*k*2&pi;/3N</sup>
 *            + x[3n + 2] e<sup>-i*nk*2&pi;/N</sup> e<sup>-i*2k*2&pi;/3N</sup>
 *          <br>
 *          = F<sub>0</sub>
 *            + e<sup>-i*k*2&pi;/3N</sup> F<sub>1</sub>
 *            + e<sup>-i*2k*2&pi;/3N</sup> F<sub>2</sub>
 *           <br>
 *    <br>
 *    X[k + N] = <sup>3N-1</sup>&sum;x[n] e<sup>-i*n(k + N)*2&pi;/3N</sup><br>
 *             = <sup>3N-1</sup>&sum;x[n] e<sup>-i*nk*2&pi;/3N</sup> e<sup>-i*n*2&pi;/3</sup><br>
 *             = <sup>N-1</sup>&sum;
 *               x[3n] e<sup>-i*nk*2&pi;/N</sup> e<sup>-i*n*2&pi;</sup><br>
 *             + x[3n + 1] e<sup>-i*nk*2&pi;/N</sup> e<sup>-i*k*2&pi;/3N</sup> e<sup>-i*(3n + 1)*2&pi;/3</sup><br>
 *             + x[3n + 2] e<sup>-i*nk*2&pi;/N</sup> e<sup>-i*2k*2&pi;/3N</sup> e<sup>-i*(3n + 2)*2&pi;/3</sup><br>
 *             <br>
 *            = <sup>N-1</sup>&sum;
 *                x[3n] e<sup>-i*nk*2&pi;/N</sup>
 *              + x[3n + 1] e<sup>-i*nk*2&pi;/N</sup> e<sup>-i*k*2&pi;/3N</sup> e<sup>-i*2&pi;/3</sup><br>
 *              + x[3n + 2] e<sup>-i*nk*2&pi;/N</sup> e<sup>-i*2k*2&pi;/3N</sup> e<sup>-i*4&pi;/3</sup><br>
 *              <br>
 *            =   F<sub>0</sub>
 *              + F<sub>1</sub> e<sup>-i*k*2&pi;/3N</sup> e<sup>-i*2&pi;/3</sup><br>
 *              + F<sub>2</sub> e<sup>-i*2k*2&pi;/3N</sup> e<sup>-i*4&pi;/3</sup><br>
 *              <br>
 *    Similarly,
 *    X[k + 2N] =  F<sub>0</sub>
 *               + F<sub>1</sub> e<sup>-i*k*2&pi;/3N</sup> e<sup>-i*4&pi;/3</sup><br>
 *               + F<sub>2</sub> e<sup>-i*2k*2&pi;/3N</sup> e<sup>-i*2&pi;/3</sup><br>
 *               <br>
 * </p>
 *
 * @author Y.K Chan
 */
public class CooleyTukeyRadix3 implements CooleyTukeyMerger {

    protected CooleyTukeyRadix3(ComplexVector pivot2, ComplexVector pivot3) {
        this.pivot1 = ComplexVector.of(new double[]{1.0}, new double[]{0.0});
        this.pivot2 = pivot2;
        this.pivot3 = pivot3;
    }

    @Override
    public void merge(ComplexVector vector, int offset, int length) {
        int num = length / 3;
        double c =  Math.cos(2 * Math.PI / length);
        double s = -Math.sin(2 * Math.PI / length);

        ComplexVector pivot = num % 3 == 0 ? this.pivot3 : num % 2 == 0 ? this.pivot2 : this.pivot1;
        int arc = num / pivot.length();

        double re = pivot.real[0];
        double im = pivot.imag[0];

        for(int q = 0; q < pivot.length(); q++) {
            int i = offset + q * arc;
            int j = i + num;
            int k = j + num;
            for (int n = 0; n < arc; n++, i++, j++, k++) {
                double re2 = re * re - im * im;
                double im2 = 2.0 * re * im;

                double f1Re = re * vector.real[j] - im * vector.imag[j];
                double f1Im = re * vector.imag[j] + im * vector.real[j];

                double f2Re = re2 * vector.real[k] - im2 * vector.imag[k];
                double f2Im = re2 * vector.imag[k] + im2 * vector.real[k];

                vector.real[j] = vector.real[i] + this.crossMultRe(f1Re, f1Im, f2Re, f2Im);
                vector.imag[j] = vector.imag[i] + this.crossMultIm(f1Re, f1Im, f2Re, f2Im);

                vector.real[k] = vector.real[i] + this.crossMultRe(f2Re, f2Im, f1Re, f1Im);
                vector.imag[k] = vector.imag[i] + this.crossMultIm(f2Re, f2Im, f1Re, f1Im);

                vector.real[i] += f1Re + f2Re;
                vector.imag[i] += f1Im + f2Im;

                double tmp = re * c - im * s;
                im = re * s + im * c;
                re = tmp;
            }
        }
    }

    /**
     * Compute the real part of f * e^-i*2&pi;/3 + g * e^-i*4&pi;/3, where f and g are complex numbers.
     * @param fRe  Real part of f
     * @param fIm  Imaginary part of f
     * @param gRe  Real part of g
     * @param gIm  Imaginary part of g
     */
    protected double crossMultRe(double fRe, double fIm, double gRe, double gIm) {
        // e^-i*2pi/3 = (-1 - sqrt(3)i)/2
        // e^-i*4pi/3 = (-1 + sqrt(3)i)/2

        //double re = -fRe + fIm * SQRT3 - gRe - gIm *SQRT3;
        //double re = -fRe - gRe + (fIm - gIm) * SQRT3;
        //double im = -fIm - SQRT3 * fIm;

        return (fRe + gRe + (gIm - fIm) * SQRT3) / -2.0;
    }

    /**
     * Compute the imaginary part of f * e^-i*2&pi;/3 + g * e^-i*4&pi;/3, where f and g are complex numbers.
     * @param fRe  Real part of f
     * @param fIm  Imaginary part of f
     * @param gRe  Real part of g
     * @param gIm  Imaginary part of g
     */
    protected double crossMultIm(double fRe, double fIm, double gRe, double gIm) {
        // e^-i*2pi/3 = (-1 - sqrt(3)i)/2
        // e^-i*4pi/3 = (-1 + sqrt(3)i)/2

        //double re = -fRe + fIm * SQRT3 - gRe - gIm *SQRT3;
        //double im = -fIm - SQRT3 * fRe -gIm + SQRT3 * gRe;
        //          = -fIm - gIm + (gRe - fRe)* SQRT3
        return (fIm + gIm + (fRe - gRe) * SQRT3) / -2.0;
    }

    private ComplexVector pivot1, pivot2, pivot3;

    private static final double SQRT3 = Math.sqrt(3.0);
}
