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
package jacobi.core.data;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.core.impl.ColumnVector;
import jacobi.test.util.Jacobi;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Y.K. Chan
 */
public class AugmentedTest {
    
    @Test
    public void test() {
        Augmented matrix = new Augmented(Matrices.zeros(5));
        Assert.assertEquals(5, matrix.getRowCount());   
        Jacobi.assertEquals(Matrices.zeros(5), matrix.get());
        Matrix output = matrix.insert(1, (r) -> 1.0).get();
        Jacobi.assertEquals(Matrices.of(new double[][]{
            {0.0, 1.0, 0.0, 0.0, 0.0, 0.0},
            {0.0, 1.0, 0.0, 0.0, 0.0, 0.0},
            {0.0, 1.0, 0.0, 0.0, 0.0, 0.0},
            {0.0, 1.0, 0.0, 0.0, 0.0, 0.0},
            {0.0, 1.0, 0.0, 0.0, 0.0, 0.0}
        }), output);
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void testGet() {
        new Augmented(Matrices.zeros(5)).getRow(0);
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void testGetColCount() {
        new Augmented(Matrices.zeros(5)).getColCount();
    }
    
    @Test
    public void testEmpty() {
        Assert.assertEquals(0, new Augmented(Matrices.zeros(0)).prepend((r) -> 1.0).get().getRowCount());
    }
    
    @Test
    public void testColumnVector() {
        Assert.assertTrue(new Augmented(Matrices.zeros(7, 3)).select(1).get() instanceof ColumnVector);
    }
    
}
