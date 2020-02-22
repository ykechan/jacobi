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

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Y.K. Chan
 */
public class WeightedTest {
    
    @Test
    public void testAscSorting() {
        List<Weighted<Void>> weights = new Random(1024)
                .doubles()
                .limit(256)
                .mapToObj((v) -> new Weighted<Void>(null, v))
                .collect(Collectors.toList());
        Collections.sort(weights, Weighted.asc());
        for(int i = 1; i < weights.size(); i++){
            Assert.assertTrue(weights.get(i).weight > weights.get(i-1).weight);
        }
    }
    
    @Test
    public void testDescSorting() {
        List<Weighted<Void>> weights = new Random(1024)
                .doubles()
                .limit(256)
                .mapToObj((v) -> new Weighted<Void>(null, v))
                .collect(Collectors.toList());
        Collections.sort(weights, Weighted.desc());
        for(int i = 1; i < weights.size(); i++){
            Assert.assertTrue(weights.get(i).weight < weights.get(i-1).weight);
        }
    }
    
    @Test
    public void testToString() {
        Weighted<String> w = new Weighted<>("item", Math.PI);
        String str = w.toString();
        Assert.assertTrue(str.contains("item"));
        Assert.assertTrue(str.contains("3.14"));
    }
    
}
