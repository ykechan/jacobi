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

package jacobi.core.util;

/**
 * Utility class for double-precision real values.
 * 
 * @author Y.K. Chan
 */
public enum Real {
    ;
    
    /**
     * Machine epsilon value. Magnitude smaller than this will be treated as zero.
     */
    public static final double EPSILON = 1e-12;
    
    /**
     * Machine tolerance value. Consider this as a more lenient machine epsilon.
     */
    public static final double TOLERANCE = 1e-8;
    
    /**
     * Pseudo-value to approximate ln(0). The exponential of this value is practically zero.
     */
    public static final double LN_ZERO = -256.0;
    
    /**
     * Check if a value is negligible, i.e.&nbsp;with absolute value smaller than EPSILON.
     * @param value  Value to be checked
     * @return   True if negligible, false otherwise
     */
    public static boolean isNegl(double value) {
        return value > -EPSILON && value < EPSILON;
    }
    
    /**
     * Zero-checked natural log. If parameter is zero, LN_ZERO is returned.
     * @param x  Function parameter
     * @return  Ln(x) is x != 0, LN_ZERO otherwise.
     * @throws  IllegalArgumentException  when x < 0
     */
    public static double pseudoLn(double x) {
    	if(x < 0.0){
    		throw new IllegalArgumentException("Ln is not defined at " + x);
    	}
    	
    	return x == 0.0 ? LN_ZERO : Math.log(x);
    }

}
