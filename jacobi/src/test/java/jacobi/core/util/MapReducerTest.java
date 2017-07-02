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

import java.util.Random;
import java.util.stream.DoubleStream;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Y.K. Chan
 */
public class MapReducerTest {
    
    @Test
    public void testLinearSearch() {
        Random rand = new Random(1000);
        double[] elem = DoubleStream.generate(() -> rand.nextDouble())
                .sequential()
                .limit(100000)
                .toArray();
        double max = MapReducer.of(0, elem.length)
                .limit(1000)
                .map((begin, end) -> {
                    double ans = -1.0;
                    for(int i = begin; i < end; i++){
                        if(elem[i] > ans){
                            ans = elem[i];
                        }
                    }
                    return ans;
                })
                .reduce((a, b) -> a > b ? a : b)
                .get();
        Assert.assertEquals(DoubleStream.of(elem).max().orElse(-1.0), max, 1e-24);
    }        
    
    @Test(expected = IllegalArgumentException.class)
    public void testLimitTooSmall() {
        new MapReducer((a, b) -> a, (a, b) -> a, 0, 16, 1).get();
    }

}
