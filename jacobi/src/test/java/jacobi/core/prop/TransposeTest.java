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
package jacobi.core.prop;

import jacobi.api.Matrix;
import jacobi.core.impl.ColumnVector;
import jacobi.core.impl.Empty;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.JacobiJUnit4ClassRunner;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Y.K. Chan
 */
@JacobiImport("/jacobi/test/data/TransposeTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class TransposeTest {
    
    @JacobiInject(1)
    public Matrix input;
    
    @JacobiInject(2)
    public Matrix expects;
    
    @JacobiResult(2)
    public Matrix output;
    
    @Test
    @JacobiImport("5x5_1")
    @JacobiEquals(expected = 2, actual = 2)
    public void test5x5_1() {
        this.output = new Transpose().compute(this.input);
    }

    @Test
    @JacobiImport("5x5_2")
    @JacobiEquals(expected = 2, actual = 2)
    public void test5x5_2() {
        this.output = new Transpose().compute(this.input);
    }
    
    @Test
    @JacobiImport("3x5_1")
    @JacobiEquals(expected = 2, actual = 2)
    public void test3x5_1() {
        this.output = new Transpose().compute(this.input);
    }
    
    @Test
    @JacobiImport("5x3_1")
    @JacobiEquals(expected = 2, actual = 2)
    public void test5x3_1() {
        this.output = new Transpose().compute(this.input);
    }
    
    @Test
    @JacobiImport("7x1_1")
    @JacobiEquals(expected = 2, actual = 2)
    public void test7x1_1() {
        this.output = new Transpose().compute(this.input);
    }
    
    @Test
    @JacobiImport("1x7_1")
    @JacobiEquals(expected = 2, actual = 2)
    public void test1x7_1() {
        this.output = new Transpose().compute(this.input);        
    }
    
    @Test
    @JacobiImport("11x9")
    @JacobiEquals(expected = 2, actual = 2)
    public void test11x9() {
        this.output = new Transpose().compute(this.input);        
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testNull() {
        new Transpose().compute(null);
    }
    
    @Test
    public void testEmpty() {
        Assert.assertTrue(new Transpose().compute(Empty.getInstance()) == Empty.getInstance());
    }
}
