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

package jacobi.core.givens;

/**
 * Representation of a pair of Givens rotation.
 * 
 * <p>For 3 elements [a b c]^t, the upper Givens rotation refers to G s.t.
 * G*[a b c]^t = [x 0 c], and the lower Givens rotation refers to H s.t.
 * H*[x 0 c]^t = [y 0 0].
 * </p>
*/
public final class GivensPair {        
    
    /**
     * Create a pair of Givens rotation &lt;G, H&gt; s.t.&nbsp;H*G*[a b c]^t = [r 0 0].
     * @param a  Value of a in the above formula
     * @param b  Value of b in the above formula
     * @param c  Value of c in the above formula
     * @return &lt;G, H&gt;
     */
    public static GivensPair of(double a, double b, double c) {
        Givens upper = Givens.of(a, b);
        Givens lower = Givens.of(upper.getMag(), c);
        return new GivensPair(upper, lower);
    }

    /**
     * Constructor.
     * @param upper  Givens rotation for eliminating upper row frontal element
     * @param lower  Givens rotation for eliminating lower row frontal element
     */
    public GivensPair(Givens upper, Givens lower) {
        this.upper = upper;
        this.lower = lower;
    }

    /**
     * Get the Givens rotation G in H*G*[u v w]^t = [y 0 0].
     * @return  Givens rotation G
     */
    public Givens getUpper() {
        return upper;
    }

    /**
     * Get the Givens rotation H in H*G*[u v w]^t = [y 0 0].
     * @return  Givens rotation H
     */
    public Givens getLower() {
        return lower;
    }
    
    /**
     * Get the value of y G in H*G*[u v w]^t = [y 0 0].
     * @return  Value of y
     */
    public double getAnchor() {
        return this.getLower().getMag();
    }
    
    /**
     * Apply a pair of Givens rotation to 3 vectors, i.e.&nbsp;H*G*[u v w]^t
     * @param upper  Upper vector u
     * @param mid  Middle vector v
     * @param lower  Lower vector w
     * @param begin  Begin of columns of interest
     * @param end  End of columns of interest
     * @return  This
     */
    public GivensPair applyLeft(double[] upper, double[] mid, double[] lower, int begin, int end) {
        this.upper.applyLeft(upper, mid, begin, end);
        this.lower.applyLeft(upper, lower, begin, end);
        return this;
    }
    
    /**
     * Apply a pair of Givens rotation to a row vector on the right at certain column.
     * @param vector  Row vector
     * @param at  Column index to apply the pair of Givens rotation
     * @return  This
     */
    public GivensPair applyRight(double[] vector, int at) {
        double a = vector[at];
        double b = vector[at + 1];
        double c = vector[at + 2];
        
        double x = this.upper.rotateX(a, b);
        double y = this.upper.rotateY(a, b);
        
        vector[at] = this.lower.rotateX(x, c);
        vector[at + 1] = y;
        vector[at + 2] = this.lower.rotateY(x, c);
        return this;
    }

    @Override
    public String toString() {
        return this.upper + ";" + this.lower;
    }

    private Givens upper, lower;
}
