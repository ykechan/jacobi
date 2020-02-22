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
 * Data object for Givens rotation for reducing [a, b] &gt; [r, 0].
 * 
 * <p>The Givens rotation is of the form</p>
 * <pre>
 * [ c  -s ][ a ]   [ r ]
 * [       ][   ] = [   ]
 * [ s   c ][ b ]   [ 0 ]
 * </pre>
 * 
 * <p>This class is immutable.</p>
 * 
 * @author Y.K. Chan
 */
public final class Givens {
    
    /**
     * Get Givens rotation for reducing [a, b] -&gt; [r, 0].
     * @param a  Upper element
     * @param b  Lower element
     * @return  Givens rotation
     */
    public static Givens of(double a, double b) {
        double r = Math.hypot(a, b);
        return Math.abs(r) < EPSILON
                ? IDENTITY
                : new Givens(r, a / r, -b / r);
    }
    
    /**
     * Constructor.
     * @param mag  Value of r
     * @param cos  Value of c
     * @param sin  Value of s
     */
    public Givens(double mag, double cos, double sin) {
        this.mag = mag;
        this.cos = cos;
        this.sin = sin;
    }

    /**
     * @return  Value of r
     */
    public double getMag() {
        return mag;
    }

    /**
     * @return  Value of c
     */
    public double getCos() {
        return cos;
    }

    /**
     * @return  Value of s
     */
    public double getSin() {
        return sin;
    }
    
    /**
     * Compute G * [a b]^t and return the upper element.
     * @param a  Upper vector element 
     * @param b  Lower vector element
     * @return  Upper element of G * [a b]'
     */
    public double rotateX(double a, double b) {
        return this.getCos() * a - this.getSin() * b;
    }
    
    /**
     * Compute G * [a b]^t and return the lower element.
     * @param a  Upper vector element 
     * @param b  Lower vector element
     * @return  Lower element of G * [a b]'
     */
    public double rotateY(double a, double b) {
        return this.getSin() * a + this.getCos() * b;
    }    
    
    /**
     * Apply Givens rotation to a pair of vector, i.e.&nbsp;G*[u v]^t
     * @param upper  Upper vector u
     * @param lower  Lower vector v
     * @param begin  Begin of columns of interest
     * @param end  End of columns of interest
     * @return  This
     */
    public Givens applyLeft(double[] upper, double[] lower, int begin, int end) {
        for(int i = begin; i < end; i++){
            double a = upper[i];
            double b = lower[i];
            upper[i] = this.rotateX(a, b);
            lower[i] = this.rotateY(a, b);
        }
        return this;
    }
    
    /**
     * Apply Givens rotation to a row vector at certain column
     * @param vector  Row vector
     * @param at  Column index to apply rotation
     * @return   This
     */
    public Givens applyRight(double[] vector, int at) {
        double a = vector[at];
        double b = vector[at + 1]; 
        vector[at]    =  this.rotateX(a, b);
        vector[at + 1] = this.rotateY(a, b);
        return this;
    }
    
    @Override
    public String toString() {
        return "[" + cos + "," + sin + "]";
    }
    
    private double mag, cos, sin; 
    
    private static final Givens IDENTITY = new Givens(0.0, 1.0, 0.0);
    
    private static final double EPSILON = 2e-24;
}
