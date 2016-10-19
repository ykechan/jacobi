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
package jacobi.core.impl;

import jacobi.api.Matrix;
import jacobi.test.util.Jacobi;
import org.junit.Test;

/**
 *
 * @author Y.K. Chan
 */
public class CopyOnWriteMatrixTest {
    
    @Test
    public void testNormal() {
        Matrix base = new DiagonalMatrix(new double[]{Math.E, Math.PI});
        Matrix diag = new CopyOnWriteMatrix(base);
        Jacobi.assertEquals(base, diag);
        
        diag.getApplySet(1, (r) -> r[0] = Math.sqrt(2.0));
         
        Jacobi.assertEquals(new DefaultMatrix(new double[][]{
            {Math.E, 0.0},
            {Math.sqrt(2.0), Math.PI}
        }), diag);
        
        Jacobi.assertEquals(new DefaultMatrix(new double[][]{
            {Math.E, 0.0},
            {0.0, Math.PI}
        }), base);
    }

    
}
