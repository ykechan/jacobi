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
    }
    
    @Test
    public void testReAllocateShouldNotHappenIfInitialCapacityIsSufficient() {
        IntStack stack = new IntStack(30);
        Assert.assertEquals(30, stack.capacity());
        stack
            .push(1).push(3).push(5).push(7).push(11)
            .push(13).push(17).push(19).push(23).push(29);
        Assert.assertEquals(10, stack.size());
        Assert.assertEquals(30, stack.capacity());
        
        Assert.assertEquals(29, stack.pop());
        Assert.assertEquals(23, stack.pop());
        Assert.assertEquals(19, stack.pop());
        
        Assert.assertEquals(7, stack.size());
        Assert.assertEquals(30, stack.capacity());
        
        stack.push(31).push(37).push(41);
        Assert.assertEquals(10, stack.size());
        Assert.assertEquals(30, stack.capacity());
    }
    
    @Test
    public void testShouldExpendIfInitialCapacityIsInsufficient() {
        IntStack stack = new IntStack(29);
        Assert.assertEquals(29, stack.capacity());
        
        stack
            .push(1).push(3).push(5).push(7).push(11)
            .push(13).push(17).push(19).push(23).push(29)
            .push(31).push(37).push(41).push(43).push(47)
            .push(53).push(59).push(61).push(67).push(71)
            .push(73).push(79).push(83).push(89).push(97)
            .push(101).push(103).push(107).push(109).push(113)
            ;
        Assert.assertEquals(30, stack.size());
        
        Assert.assertEquals(113, stack.pop());
        Assert.assertEquals(109, stack.pop());
        Assert.assertEquals(107, stack.pop());
        
        Assert.assertEquals(27, stack.size());
        
        stack.push(31).push(37).push(41);
        Assert.assertEquals(30, stack.size());
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
    

}
