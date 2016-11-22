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
package jacobi.core.decomp.eigen;

import jacobi.api.Matrix;
import jacobi.core.decomp.qr.QRStrategy;
import jacobi.core.util.Pair;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.JacobiJUnit4ClassRunner;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Y.K. Chan
 */
@JacobiImport("/jacobi/test/data/EigenFinderTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class EigenFinderTest {
    
    @JacobiInject(0)
    public Matrix input;
    
    @JacobiInject(1)
    @JacobiResult(1)
    public Matrix real;
    
    @JacobiInject(2)
    @JacobiResult(2)
    public Matrix img;
    
    public EigenFinderTest() {
    }

    @Test
    @JacobiImport("Find Eig 5x5")
    @JacobiEquals(expected = 1, actual = 1)
    @JacobiEquals(expected = 2, actual = 2)
    public void testFindEig5x5() {
        Pair pair = this.mockDummy().compute(this.input);
        this.real = pair.getLeft();
        this.img = pair.getRight();
    }
    
    @Test
    @JacobiImport("Find Eig 6x6")
    @JacobiEquals(expected = 1, actual = 1)
    @JacobiEquals(expected = 2, actual = 2)
    public void testFindEig6x6() {
        Pair pair = this.mockDummy().compute(this.input);
        this.real = pair.getLeft();
        this.img = pair.getRight();
    }
    
    @Test
    @JacobiImport("7x7")
    public void test7x7() {
        Pair pair = new EigenFinder().compute(this.input);
    }
        
    protected EigenFinder mockDummy() { 
        // assume matrices are already solved
        return new EigenFinder((matrix, partner, fullUpper) -> matrix);
    }
    
    
    
}
