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

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.api.annotations.Pure;
import jacobi.core.impl.ImmutableMatrix;
import jacobi.core.util.Pair;
import jacobi.core.util.ParallelSupplier;
import jacobi.core.util.Throw;

import java.util.Arrays;
import java.util.function.BinaryOperator;
import java.util.function.IntConsumer;

@Pure
public abstract class DiscreteFourierTransform {

    public static class Forward extends DiscreteFourierTransform {

        public Forward() {
            super(new CooleyTukeyFFT(new int[]{6, 2, 3}, getImpl()));
        }

    }

    protected DiscreteFourierTransform(CooleyTukeyFFT fft) {
        this.fft = fft;
    }

    public Pair compute(Matrix matRe) {
        return this.compute(matRe, new ImmutableMatrix(){

            @Override
            public int getRowCount() {
                return matRe.getRowCount();
            }

            @Override
            public int getColCount() {
                return matRe.getColCount();
            }

            @Override
            public double[] getRow(int index) {
                return new double[this.getColCount()];
            }
        });
    }

    public Pair compute(Matrix matRe, Matrix matIm) {
        Throw.when()
            .isNull(() -> matRe, () -> "No real part.")
            .isNull(() -> matIm, () -> "No imaginary part.")
            .isTrue(() -> matRe.getRowCount() != matIm.getRowCount(), () -> "Vector count mismatch.")
            .isTrue(() -> matRe.getColCount() != matIm.getColCount(), () -> "Dimension mismatch.");
        Matrix dftRe = Matrices.zeros(matRe.getRowCount(), matRe.getColCount());
        Matrix dftIm = Matrices.zeros(matRe.getRowCount(), matRe.getColCount());
        int len = matRe.getColCount();
        BinaryOperator<ComplexVector> dft = this.fft.of(len);
        IntConsumer func = (i) -> {
            ComplexVector sig = ComplexVector.of(Arrays.copyOf(matRe.getRow(i), len), Arrays.copyOf(matIm.getRow(i), len));
            ComplexVector buf = ComplexVector.of(dftRe.getRow(i), dftIm.getRow(i));
            ComplexVector res = dft.apply(sig, buf);
            dftRe.setRow(i, res.real);
            dftIm.setRow(i, res.imag);
        };
        this.compute(func, matRe.getRowCount(), matRe.getColCount());
        return Pair.of(dftRe, dftIm);
    }

    protected void compute(IntConsumer func, int rowCount, int colCount) {
        if(this.runInParallel(rowCount, colCount)){
            ParallelSupplier.cyclic(func, 0, rowCount);
            return;
        }
        for(int i = 0; i < rowCount; i++){
            func.accept(i);
        }
    }

    protected boolean runInParallel(int rowCount, int colCount) {
        int numFlop = (int) Math.ceil(Math.log(colCount)) * colCount * rowCount;
        return rowCount > ParallelSupplier.DEFAULT_NUM_THREADS
            && numFlop > ParallelSupplier.DEFAULT_FLOP_THRESHOLD;
    }

    private CooleyTukeyFFT fft;

    protected static CooleyTukeyMerger[] getImpl() {
        return new CooleyTukeyMerger[]{
            new BasisDft(12),
            new BasisDft(Short.MAX_VALUE),
            new CooleyTukeyRadix2(),
            new CooleyTukeyRadix3(),
            null,
            new CooleyTukeyRadixN(5)
        };
    }
}
