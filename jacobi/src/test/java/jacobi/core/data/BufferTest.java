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

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Y.K. Chan
 */
public class BufferTest {
    
    @Test
    public void testPrependAppend() {
        Buffer buf = new Buffer(3, 7);
        buf.fill(new double[]{1.0, 2.0, 3.0});
        Assert.assertArrayEquals(new double[]{1.0, 2.0, 3.0}, buf.getArray(), 1e-16);
        buf.insert(0, -1.0);
        buf.insert(0, -2.0);
        Assert.assertArrayEquals(new double[]{-2.0, -1.0, 1.0, 2.0, 3.0}, buf.getArray(), 1e-16);
        buf.insert(Integer.MAX_VALUE, 11.0);        
        Assert.assertArrayEquals(new double[]{-2.0, -1.0, 1.0, 2.0, 3.0, 11.0}, buf.getArray(), 1e-16);
        buf.insert(0, -3.0);
        Assert.assertArrayEquals(new double[]{-3.0, -2.0, -1.0, 1.0, 2.0, 3.0, 11.0}, buf.getArray(), 1e-16);
        Assert.assertEquals(7, buf.size());
    }

    @Test
    public void testInsertion() {
        Buffer buf = new Buffer(4, 10);
        buf.fill(new double[]{0.0, 1.0, 2.0, 3.0});
        buf.insert(0, 1.0);
        buf.insert(0, 2.0);
        buf.insert(0, 3.0);
        Assert.assertArrayEquals(new double[]{3.0, 2.0, 1.0, 0.0, 1.0, 2.0, 3.0}, buf.getArray(), 1e-16);
        buf.insert(3, 10.0);
        Assert.assertArrayEquals(new double[]{3.0, 2.0, 1.0, 10.0, 0.0, 1.0, 2.0, 3.0}, buf.getArray(), 1e-16);
    }
    
    @Test
    public void testSelection() {
        Buffer buf = new Buffer(2, 10);
        buf.fill(new double[]{1.0, 2.0, 3.0, 4.0, 5.0, 6.0});
        buf.select(new int[]{4, 2, 3, 5});
        Assert.assertArrayEquals(new double[]{5.0, 3.0, 4.0, 6.0}, buf.getArray(), 1e-16);
    }
    
}
