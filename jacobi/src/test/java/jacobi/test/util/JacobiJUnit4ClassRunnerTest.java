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
package jacobi.test.util;

import jacobi.api.Matrix;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Y.K. Chan
 */
@RunWith(JacobiJUnit4ClassRunner.class)
@JacobiImport("/jacobi/test/data/JacobiJUnit4ClassRunnerTest.xlsx")
public class JacobiJUnit4ClassRunnerTest {
    
    @JacobiInject(0)
    public Matrix z;
    
    @JacobiInject(1)
    public Matrix a;
    
    @JacobiInject(2)
    @JacobiResult(3)
    public Matrix b;
    
    @JacobiInject(7)
    @JacobiResult(4)
    public Matrix c;
    
    @JacobiInject(0)
    @JacobiResult(0)
    public Matrix u;
    
    @Test
    @JacobiImport("testReadEquals3x3")
    @JacobiEquals(expected = 1, actual = 3)
    public void testReadEquals3x3() {
        
    }
    
    @Test
    @JacobiImport("testReadEquals5x5")
    @JacobiEquals(expected = 0, actual = 4)
    public void testReadEquals5x5() {
        System.out.println(this.c);
    }
    
    @Test
    @JacobiImport("testReadEquals5x5")
    @JacobiEquals(expected = 0, actual = 0)
    public void testReadDuplicates() {
        System.out.println(this.c);
    }
}
