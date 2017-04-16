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
package jacobi.core.linprog;

import jacobi.api.Matrix;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.JacobiJUnit4ClassRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Y.K. Chan
 */
@JacobiImport("/jacobi/test/data/ElementaryPivotingTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class ElementaryPivotingTest {
    
    @JacobiInject(0)
    @JacobiResult(100)
    public Matrix matrix;
    
    @Test
    @JacobiImport("swap 2,3")
    @JacobiEquals(expected = 100, actual = 100)
    public void testSwap2And3() {
        new ElementaryPivoting().run(this.matrix, 2, 3);
    }
    
    @Test
    @JacobiImport("swap 1,5")
    @JacobiEquals(expected = 100, actual = 100)
    public void testSwap1And5() {
        new ElementaryPivoting().run(this.matrix, 1, 5);
    }
    
    @Test
    @JacobiImport("swap 4,2")
    @JacobiEquals(expected = 100, actual = 100)
    public void testSwap4And2() {
        new ElementaryPivoting().run(this.matrix, 4, 2);
    }
    
}
