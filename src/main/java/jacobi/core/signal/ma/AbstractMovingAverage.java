/*
 * The MIT License
 *
 * Copyright (c) 2018 Y.K. Chan
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

package jacobi.core.signal.ma;

import jacobi.api.ma.Initial;
import jacobi.core.util.Throw;

import java.util.Arrays;

/**
 * Common super-class of moving average algorithm.
 *
 * <p>
 *     All implementations should support all initial modes and 2 options of window position: trailing, which
 *     uses past data only, and centered, in which both past and future data will be used. For centered window,
 *     the window size must be odd.
 * </p>
 *
 * @author Y.K. Chan
 */
public abstract class AbstractMovingAverage {

    /**
     * Constructor.
     * @param mode  Mode for computing initial (and final for centered window) points.
     */
    public AbstractMovingAverage(Initial mode) {
        if(mode == null){
            throw new IllegalArgumentException("Initial mode not specified");
        }
        this.mode = mode;
    }

    /**
     * Compute the moving average of a given signal. Values of input parameter will not be mutated.
     * @param signal  Input signal.
     * @param lag  Lag length, aka window size.
     * @return  The moving average of the given signal.
     */
    public double[] compute(double[] signal, int lag) {
        if(signal == null){
            throw new IllegalArgumentException("No signal");
        }
        if(lag <= 0){
            throw new IllegalArgumentException("Invalid lag length " + lag);
        }
        if(lag > signal.length){
            throw new IllegalArgumentException("Lag length longer than the signal.");
        }
        double[] output = new double[signal.length - (this.mode == Initial.SHRINK ? lag - 1 : 0)];
        this.apply(signal, output, lag);
        return output;
    }

    /**
     * Implementation of computing the moving average of a given signal. Values of input parameter will not be mutated.
     * @param signal  Input signal.
     * @param output  Output signal. This value will be updated.
     * @param lag  Lag length, aka window size.
     */
    protected abstract void apply(double[] signal, double[] output, int lag);

    protected final Initial mode;
}
