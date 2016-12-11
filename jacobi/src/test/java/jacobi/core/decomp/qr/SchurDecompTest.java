/*
 * The MIT License
 *
 * Copyright 2016 Y.K. Chan.
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
package jacobi.core.decomp.qr;

import jacobi.api.Matrix;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.JacobiJUnit4ClassRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * The answer used in this test is computed by SchurDecomp itself, and verified by spreadsheet. Since Schur form is
 * not unique, for fail cases compute the answer and change the spreadsheet itself. (Of course it should pass the
 * spreadsheet verification)
 * 
 * @author Y.K. Chan
 */
@JacobiImport("/jacobi/test/data/SchurDecompTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class SchurDecompTest {
    
    @JacobiInject(0)
    @JacobiResult(1)
    public Matrix input;    
    
    @JacobiResult(2)
    public Matrix ortho;
    
    @Test
    @JacobiImport("4x4")
    @JacobiEquals(expected = 1, actual = 1)
    @JacobiEquals(expected = 2, actual = 2)
    public void test4x4() {
        this.ortho = new SchurDecomp().computeBoth(input).getLeft();        
    }
    
    @Test
    @JacobiImport("5x5")
    @JacobiEquals(expected = 1, actual = 1)
    @JacobiEquals(expected = 2, actual = 2)
    public void test5x5() {
        this.ortho = new SchurDecomp().computeBoth(input).getLeft();        
    }
    
    @Test
    @JacobiImport("6x6")
    @JacobiEquals(expected = 1, actual = 1)
    @JacobiEquals(expected = 2, actual = 2)
    public void test6x6() {
        this.ortho = new SchurDecomp().computeBoth(input).getLeft(); 
    }
    
}
