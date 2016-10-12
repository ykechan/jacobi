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
public class PaddingPlanTest {
    
    @Test
    public void testApply() {
        // transfrorm <x, y> into <1.0, x, x^2>
        PaddingPlan pp = PaddingPlan.builder(2)
                .prepend((r) -> 1.0)
                .prepend((r) -> r.get(1) * r.get(1))
                .select(1, 2, 0)
                .build();
        double[] u = {3.0, 4.0};
        double[] v = pp.apply(pp.createBuffer(), u);
        Assert.assertArrayEquals(new double[]{1.0, 3.0, 3.0 * 3.0}, v, 1e-16);
        
        // transform <a, b, y> into <1.0, a, b, a*b, a^2, b^2>
        pp = PaddingPlan.builder(3)
                .append((r) -> 1.0)
                .append((r) -> r.get(0) * r.get(1))
                .append((r) -> r.get(0) * r.get(0))
                .append((r) -> r.get(1) * r.get(1))                
                .select(3, 0, 1, 4, 5, 6)
                .build();
        u = new double[]{2.0, 5.0, 9.0};
        v = pp.apply(pp.createBuffer(), u);
        Assert.assertArrayEquals(new double[]{1.0, 2.0, 5.0, 2.0 * 5.0, 2.0 * 2.0, 5.0 * 5.0}, v, 1e-16);
    }
    
    @Test
    public void testAppendPrependSelect() {
        // transfrorm <x, y> into <1.0, x, x^2>
        PaddingPlan pp = PaddingPlan.builder(2)
                .append((r) -> r.get(0) * r.get(0))
                .prepend((r) -> 1.0)
                .select(0, 1, 3)
                .build();
        double[] u = {3.0, 4.0};
        double[] v = pp.apply(pp.createBuffer(), u);
        Assert.assertArrayEquals(new double[]{1.0, 3.0, 3.0 * 3.0}, v, 1e-16);        
    }
    
    @Test
    public void testPrepareAppendPrependSelect() {
        PaddingPlan pp = PaddingPlan.builder(3)
                .append((r) -> 1.0)
                .prepend((r) -> 2.0)
                .append((r) -> 3.0)
                .prepend((r) -> 4.0)
                .insert(3, (r) -> 5.0)
                .select(2, 3)
                .build();
        // 4 2 ! 5 @ # 1 3 -> ! 5
        Buffer buffer = pp.createBuffer();
        Assert.assertEquals(2, buffer.getStartingPosition());
        Assert.assertEquals(8, buffer.getMaximumLength());
    }

    @Test
    public void testPrepareMultiSelect() {
        PaddingPlan pp = PaddingPlan.builder(3)
                .append((r) -> 1.0)
                .prepend((r) -> 2.0)
                .insert(3, (r) -> 5.0)
                .select(2, 4, 3)
                .append((r) -> 3.0)
                .prepend((r) -> 4.0)                
                .prepend((r) -> 5.0)
                .prepend((r) -> 6.0)
                .select(2, 3)
                .build();
        // 2 ! 5 @ # 1 -> 6 5 4 5 # @ 3 -> # @
        Buffer buffer = pp.createBuffer();
        Assert.assertEquals(3, buffer.getStartingPosition());
        Assert.assertEquals(7, buffer.getMaximumLength());
    }
}
