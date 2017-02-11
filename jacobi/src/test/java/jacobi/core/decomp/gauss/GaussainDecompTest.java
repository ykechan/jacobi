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
package jacobi.core.decomp.gauss;

import jacobi.api.Matrix;
import jacobi.core.util.Triplet;
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
@JacobiImport("/jacobi/test/data/GaussianDecompTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class GaussainDecompTest {
    
    @JacobiInject(0) 
    public Matrix input;
    
    @JacobiResult(100)
    public Matrix perm;
    
    @JacobiResult(101)
    public Matrix lower;
    
    @JacobiResult(102)
    public Matrix upper;
    
    private GaussianDecomp gaussDecomp;

    public GaussainDecompTest() {
        this.gaussDecomp = new GaussianDecomp();
    }
    
    @Test
    @JacobiImport("5x5")
    @JacobiEquals(expected = 100, actual = 100)
    @JacobiEquals(expected = 101, actual = 101)
    @JacobiEquals(expected = 102, actual = 102)
    public void test5x5() {
        Triplet plu = this.gaussDecomp.compute(this.input);
        this.perm = plu.getLeft();
        this.lower = plu.getMiddle();
        this.upper = plu.getRight();
    }
    
    @Test
    @JacobiImport("5x5")
    @JacobiEquals(expected = 100, actual = 100)
    @JacobiEquals(expected = 101, actual = 101)
    @JacobiEquals(expected = 102, actual = 102)
    public void test5x5ByStream() {        
        Triplet plu = new GaussianDecomp(new GenericGaussianElim(){

            @Override
            protected int serial(ElementaryOperator op, int from, double pivot, double next) {
                return this.stream(op, from, pivot, next);
            }
            
        }).compute(this.input);
        this.perm = plu.getLeft();
        this.lower = plu.getMiddle();
        this.upper = plu.getRight();
    }
    
    @Test
    @JacobiImport("Degen 4x4")
    @JacobiEquals(expected = 100, actual = 100)
    @JacobiEquals(expected = 101, actual = 101)
    @JacobiEquals(expected = 102, actual = 102)
    public void testDegen4x4() {
        Triplet plu = this.gaussDecomp.compute(this.input);
        this.perm = plu.getLeft();
        this.lower = plu.getMiddle();
        this.upper = plu.getRight();
    }

}
