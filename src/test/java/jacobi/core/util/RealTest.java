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

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Y.K. Chan
 */
public class RealTest {

    @Test
    public void shouldBeAbleToCheckNegligable() {
        double rt2 = Math.sqrt(2.0);
        Assert.assertTrue(Real.isNegl(rt2 * rt2 - 2.0));
        Assert.assertTrue(Real.isNegl(2.0 - rt2 * rt2));
    }
    
    @Test
    public void shouldBeAbleToComputePseudoLn() {
    	double x = Math.E * Math.E;
    	Assert.assertTrue(Real.isNegl(2.0 - Real.pseudoLn(x)));
    	
    	Assert.assertEquals(Real.LN_ZERO, Real.pseudoLn(0.0), 1e-12);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenPassingNegativeToPseudoLn() {
    	Real.pseudoLn(-1);
    }
    
}
