/* 
 * The MIT License
 *
 * Copyright 2016 Y.K. Chan
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
package jacobi.test.util;

import jacobi.api.Matrix;
import org.junit.Assert;

/**
 *
 * @author Y.K. Chan
 */
public final class Jacobi {
    
    public static void assertEquals(Matrix expects, Matrix actual) {
        Jacobi.assertEquals(expects, actual, 1e-8);
    }
    
    public static void assertEquals(Matrix expects, Matrix actual, double epilson) {
        Assert.assertNotNull("No expected result.", expects);
        Assert.assertNotNull("No actual result.", actual);
        Assert.assertEquals("Row count mismatch.", expects.getRowCount(), actual.getRowCount());
        Assert.assertEquals("Column count mismatch.", expects.getColCount(), actual.getColCount());
        for(int i = 0; i < actual.getRowCount(); i++){
            for(int j = 0; j < actual.getColCount(); j++){
                Assert.assertEquals(
                    " Element (" + i + "," + j + ") mismatch.",
                    expects.getRow(i)[j],
                    actual.getRow(i)[j],
                    epilson );
            }
        }
    }

}
