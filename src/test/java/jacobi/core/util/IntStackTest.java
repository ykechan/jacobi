/*
 * The MIT License
 *
 * Copyright 2018 Y.K. Chan
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Y.K. Chan
 */
public class IntStackTest {
    
    @Test
    public void testShouldBeAbleToPassSimpleRun() {
        IntStack stack = new IntStack(0);
        stack.push(1).push(3).push(5).push(7).push(11);
        
        Assert.assertEquals(5, stack.size());
        
        Assert.assertEquals(11, stack.pop());
        Assert.assertEquals(7, stack.pop());
        Assert.assertEquals(5, stack.pop());
        
        Assert.assertEquals(2, stack.size());
        
        stack.push(13).push(17);
        
        Assert.assertEquals(4, stack.size());
        
        Assert.assertEquals(9, stack.capacity());                
    }
    
    @Test
    public void testReAllocateShouldNotHappenIfInitialCapacityIsSufficient() {
        IntStack stack = new IntStack(10);
        Assert.assertEquals(10, stack.capacity());
        stack
            .push(1).push(3).push(5).push(7).push(11)
            .push(13).push(17).push(19).push(23).push(29);
        Assert.assertEquals(10, stack.size());
        Assert.assertEquals(10, stack.capacity());
        
        Assert.assertEquals(29, stack.pop());
        Assert.assertEquals(23, stack.pop());
        Assert.assertEquals(19, stack.pop());
        
        Assert.assertEquals(7, stack.size());
        Assert.assertEquals(10, stack.capacity());
        
        stack.push(31).push(37).push(41);
        Assert.assertEquals(10, stack.size());
        Assert.assertEquals(10, stack.capacity());
    }
    
    @Test
    public void testShouldExpendIfInitialCapacityIsInsufficient() {
        IntStack stack = new IntStack(6);
        Assert.assertEquals(6, stack.capacity());
        stack
            .push(1).push(3).push(5).push(7).push(11)
            .push(13).push(17).push(19).push(23).push(29);
        Assert.assertEquals(10, stack.size());
        
        Assert.assertEquals(29, stack.pop());
        Assert.assertEquals(23, stack.pop());
        Assert.assertEquals(19, stack.pop());
        
        Assert.assertEquals(7, stack.size());
        
        stack.push(31).push(37).push(41);
        Assert.assertEquals(10, stack.size());
    }
    
    @Test
    public void testValuesShouldBeRetrievedByAFirstInLastOutBasis() {
        IntStack stack = new IntStack(0);
        Random rand = new Random(Double.doubleToLongBits(-Math.PI));
        
        List<Integer> elements = new ArrayList<>();
        for(int i = 0; i < 1000; i++){
            int elem = rand.nextInt();
            elements.add(elem);
            stack.push(elem);
        }
        Collections.reverse(elements);
        for(int elem : elements){
            Assert.assertEquals(elem, stack.pop());
        }
    }
    
    @Test
    public void testByDefaultCapacityShouldAlwaysBeAPerfectSquare() {
        IntStack stack = new IntStack(0);
        Random rand = new Random(Double.doubleToLongBits(Math.PI));
        
        for(int i = 0; i < 256; i++){
            stack.push(rand.nextInt());
            double ans = Math.sqrt(stack.capacity());
            Assert.assertTrue(Real.isNegl(ans - Math.floor(ans)));
        }        
    }
    
    @Test
    public void testShouldReturn3ForNumbersLessThenOrEqualsTo4() {
        IntStack stack = new IntStack(0);
        Assert.assertEquals(1, stack.sqrt(0));
        Assert.assertEquals(1, stack.sqrt(1));
        Assert.assertEquals(1, stack.sqrt(2));
        Assert.assertEquals(1, stack.sqrt(3));
                
        Assert.assertEquals(1, stack.sqrt(-1));
        Assert.assertEquals(1, stack.sqrt(-99));
    }
    
    @Test
    public void testShouldReturnAnsForPerfectSquares() {
        IntStack stack = new IntStack(0);
        Random rand = new Random(Double.doubleToLongBits(-Math.E));
        for(int i = 0; i < 1000; i++){
            int num = 4 + rand.nextInt(Short.MAX_VALUE - 4);
            Assert.assertEquals(num, stack.sqrt(num * num));
        }
    }
    
    @Test
    public void testShouldReturnClosestUpperBoundForNonSquares() {
        IntStack stack = new IntStack(0);
        Random rand = new Random(Double.doubleToLongBits(-Math.E));
        for(int i = 0; i < 1000; i++){
            int num = rand.nextInt();
            if(num <= 4){
                Assert.assertEquals(1, stack.sqrt(num));
                continue;
            }
            int ans = stack.sqrt(num);
            Assert.assertTrue(ans * ans >= num);
            Assert.assertTrue((ans - 1) * (ans - 1) < num);
        }
    }

}
