/*
 * The MIT License
 *
 * Copyright 2016 Y.K. Chan.
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
 * The Givens rotation is of the form
 * [ c  -s ][ a ]   [ r ]
 * [       ][   ] = [   ]
 * [ s   c ][ b ]   [ 0 ]
 * 
 * @author Y.K. Chan
 */
public class Givens {
    
    /**
     * Get Givens rotation for reducing [a, b] -&gt; [r, 0].
     * @param a  Upper element
     * @param b  Lower element
     * @return  Givens rotation
     */
    public static Givens of(double a, double b) {
        double r = Math.hypot(a, b);
        return new Givens(r, a / r, -b / r);
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
     * Compute G^t * [a b]^t or [a b] * G and return the upper/left element.
     * @param a  Upper/Left vector element
     * @param b  Lower/Right vector element
     * @return  Upper element of G^t * [a b]^t
     */
    public double reverseRotX(double a, double b) {
        return this.getCos() * a + this.getSin() * b;
    }
    
    /**
     * Compute G^t * [a b]^t or [a b] * G and return the lower/right element.
     * @param a  Upper/Left vector element
     * @param b  Lower/Right vector element
     * @return  Lower element of G^t * [a b]^t
     */
    public double reverseRotY(double a, double b) {
        return this.getCos() * b - this.getSin() * a;
    }
    
    @Override
    public String toString() {
        return "[" + cos + "," + sin + "]";
    }
    
    private double mag, cos, sin;    
}