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
 * Implementation of dqds based on the paper B.N. Parlett and O.A. Marques. 
 * "An implementation of the dqds algorithm (positive case)".
 * 
 * For an overview of the algorithm, @see {@link jacobi.core.decomp.svd.dqds.DqdsStep}
 * 
 * For other details, please refer to the paper.
 * 
 * @author Y.K. Chan
 */
public class DqdsImpl implements DqdsStep {

    @Override
    public State compute(double[] zElem, int begin, int end, boolean forward, State result) { 
        return this.unsafe(zElem, begin, end, forward, result.guess);
    } 
    
    protected State unsafe(double[] zElem, int begin, int end, boolean forward, double shift) {
        int curr = forward ? 0 : 2;
        int next = Math.abs(curr - 2);
        double delta = zElem[curr + 4*begin] - shift;
        double maxElem = Double.MIN_VALUE;
        double minElem = Double.MAX_VALUE;
        double minErr = Double.MAX_VALUE; 
        double minDelta = delta;
        
        return State.empty();
    }
    
    protected State safe() {
        return State.empty();
    }

    private static final double SAFEMIN = 1e-20;
}
