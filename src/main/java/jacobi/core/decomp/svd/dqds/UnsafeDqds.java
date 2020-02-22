/*
 * The MIT License
 *
 * Copyright 2017 Y.K. Chan
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package jacobi.core.decomp.svd.dqds;

/**
 * An implementation of dqds step that is prone to overflow or underflow in extreme situation.
 * 
 * <p>Refer to the paper B.N. Parlett &amp; O.A. Marques, "An implementation of dqds algorithm (a positive case)".</p>
 * 
 * @author Y.K. Chan
 */
public class UnsafeDqds implements DqdsStep {

    @Override
    public State compute(double[] zElem, int begin, int end, boolean forward, State prev) {
        int curr = forward ? 0 : 2;
        int next = Math.abs(curr - 2);
        double maxElem = Double.MIN_VALUE;
        double minElem = Double.MAX_VALUE;
        double minErr = Double.MAX_VALUE;
        double delta = zElem[curr + 4*begin] - prev.guess;
        int last = end - 1;
        for(int i = curr + 4*begin, j = next + 4*begin; i < curr + 4*last; i+=4, j+=4){
            zElem[j] = delta + zElem[i + 1];
            double temp = zElem[i + 4] / zElem[j];
            zElem[j + 1] = zElem[i + 1] * temp;
            delta = delta * temp - prev.guess;
            
            maxElem = Double.max(maxElem, zElem[j]);
            minElem = Double.min(minElem, zElem[j]);
            minErr = Double.min(minErr, zElem[j + 1]);
        }
        zElem[next + 4*last] = delta;
        return minElem < 0.0 ? State.failed(0.0) : State.empty();
    }

}
