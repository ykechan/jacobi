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

package jacobi.core.signal.smooth;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.core.util.ParallelSupplier;
import jacobi.core.util.Throw;

public class SimpleMovingAvg {

    public SimpleMovingAvg(long minFlop) {
        this.minFlop = minFlop;
    }

    public Matrix compute(Matrix signals, int radius) {
        Throw.when()
             .isNull(() -> signals, () -> "Missing data.")
             .isTrue(() -> radius < 0, () -> "Invalid radius.")
             .isTrue(
                     () -> 2 * radius + 1 >= signals.getColCount(),
                     () -> "Vector length " + signals.getColCount() + " is shorter than window width " + (2 * radius + 1)
             );
        Matrix result = Matrices.zeros(signals.getRowCount(), signals.getColCount());
        if(this.cost(signals) > this.minFlop){
            ParallelSupplier.cyclic(i -> result.setRow(i, this.compute(signals.getRow(i), result.getRow(i), radius)),
                    0, signals.getRowCount(), Math.min(signals.getRowCount(), ParallelSupplier.DEFAULT_NUM_THREADS));
        }else{
            for(int i = 0; i < signals.getRowCount(); i++){
                result.setRow(i, this.compute(signals.getRow(i), result.getRow(i), radius));
            }
        }
        return result;
    }

    protected long cost(Matrix signals) {
        return signals.getRowCount() * signals.getColCount();
    }

    protected double[] compute(double[] seq, double[] avg, int radius) {
        double sum = radius * seq[0];
        for(int i = 1; i < radius; i++){
            sum += seq[i];
        }
        int width = 2 * radius + 1;
        int begin = radius;
        avg[0] = sum / width;
        for(int i = 1; i < begin; i++){
            sum -= seq[0] + seq[i + radius];
            avg[i] = sum / width;
        }
        int end = seq.length - radius;
        for(int i = begin; i < end; i++){
            sum -= seq[i - radius] + seq[i + radius];
            avg[i] = sum / width;
        }
        double last = seq[seq.length - 1];
        for(int i = end; i < seq.length; i++){
            sum -= seq[i - radius] + last;
            avg[i] = sum / width;
        }
        return avg;
    }

    private long minFlop;
}
