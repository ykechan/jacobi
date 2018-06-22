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

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.api.annotations.Pure;
import jacobi.core.impl.ImmutableMatrix;
import jacobi.core.signal.ComplexVector;
import jacobi.core.util.Pair;
import jacobi.core.util.ParallelSupplier;
import jacobi.core.util.Throw;

import java.util.Arrays;
import java.util.function.BinaryOperator;
import java.util.function.IntConsumer;

@Pure
public abstract class DiscreteFourierTransform {

    public static final int DEFAULT_MIN_TASK = 2 * ParallelSupplier.DEFAULT_NUM_THREADS;

    public static final int DEFAULT_MIN_FLOP = ParallelSupplier.DEFAULT_FLOP_THRESHOLD;

    public static class Forward extends DiscreteFourierTransform {

        public Forward() {
            super(DEFAULT_FORWARD, DEFAULT_MIN_TASK, DEFAULT_MIN_FLOP);
        }

    }

    protected DiscreteFourierTransform(CooleyTukeyFFT fft, int minTasks, long minFlop) {
        this.fft = fft;
        this.minTasks = minTasks;
        this.minFlop = minFlop;
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
        if(this.isHeavy(matRe.getRowCount(), matRe.getColCount())){
            ParallelSupplier.cyclic(func, 0, dftRe.getRowCount());
        }else{
            for(int i = 0; i < dftRe.getRowCount(); i++){
                func.accept(i);
            }
        }
        return Pair.of(dftRe, dftIm);
    }

    /**
     * Check if the work load is heavy enough to use parallelism.
     * @param rowCount  Number of vectors
     * @param colCount  Vector dimension
     * @return  True if vectors are numerous and each transform is complicated enough, false otherwise
     */
    protected boolean isHeavy(int rowCount, int colCount) {
        return rowCount > this.minTasks && this.fft.estimateCost(colCount) > this.minFlop;
    }

    private int minTasks;
    private long minFlop;
    private CooleyTukeyFFT fft;

    private static final CooleyTukeyFFT DEFAULT_FORWARD = new CooleyTukeyFFT(new int[]{}, new CooleyTukeyMerger[]{
        new BasisDft(6),
        new BasisDft(1024),
        new CooleyTukeyRadix2(true),
        new CooleyTukeyRadix3(),
        null,
        new CooleyTukeyRadixN(5)
    });

    private static final CooleyTukeyFFT DEFAULT_INVERSE = null;
}
