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
 * Implementation of a flipping step in Dqds algorithm.
 * 
 * Dqds, being a variant of QR algorithm, has a tendency to converge first at the bottom. In cases
 * that it is close to convergence at the top, it takes a few iterations to push the converging value down.
 * 
 * To save these few iterations, it would be better to reverse the elements, i.e.
 * 
 * {a1, b1, a2, b2, ..., aN} -&gt; {aN, bN-1, aN-1, bN-2, ..., b1, a1}
 * 
 * Such operations can be carried out by orthogonal transformation on the underlying tri-diagonal matrix, and
 * thus singular values are invariant. For example
 * 
 * [a1 b1  0  0  0]       [ 0  0  0 b4 a5]       [a5  0  0 b4  0]
 * [b1 a2 b2  0  0]       [b1 a2 b2  0  0]       [ 0 a2 b2  0 b1]
 * [ 0 b2 a3 b3  0] =&lt; [ 0 b2 a3 b3  0] =&lt; [ 0 b2 a3 b3  0]
 * [ 0  0 b3 a4 b4]       [ 0  0 b3 a4 b4]       [b4  0 b3 a4  0]
 * [ 0  0  0 b4 a5]       [a1 b1  0  0  0]       [ 0 b1  0  0 a1]
 * 
 *                        [a5  0  0 b4  0]       [a5 b4  0  0  0]
 *                        [b4  0 b3 a4  0]       [b4 a4 b3  0  0]
 *                  =&lt; [ 0 b2 a3 b3  0] =&lt; [ 0 b3 a3 b2  0]
 *                        [ 0 a2 b2  0 b1]       [ 0  0 b2 a2 b1]
 *                        [ 0 b1  0  0 a1]       [ 0  0  0 b1 a1]
 * 
 * @author Y.K. Chan
 */
public class FlippingDqds implements DqdsStep {

    /**
     * Constructor.
     * @param base  Base implementation
     * @param factor  Factor of the first element that last element must exceed for flipping, 
     *                i.e. last &gt;= factor * fist to flip.
     */
    public FlippingDqds(DqdsStep base, double factor) {
        this.base = base;
        this.factor = factor;
    }

    @Override
    public State compute(double[] zElem, int begin, int end, boolean forward, State prev) {
        int curr = forward ? 0 : 2;
        int next = Math.abs(curr - 2);
        int last = end - 1;
        if(Math.abs(zElem[curr + 4*last]) < factor * Math.abs(zElem[curr + 4*begin])){
            return this.base.compute(zElem, begin, end, forward, prev);
        }
        for(int i = curr + 4*last, j = next + 4*begin; j < next + 4*last; i-=4, j+=4){
            zElem[j] = zElem[i];
            zElem[j + 1] = zElem[i - 3];
        }
        zElem[next + 4*last] = zElem[curr + 4*begin];
        return State.empty();
    }

    private DqdsStep base;
    private double factor;
}
