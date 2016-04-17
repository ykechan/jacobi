/*
 * Copyright (C) 2016 Y.K. Chan
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
