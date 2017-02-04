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

package jacobi.core.decomp.qr.step;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.core.givens.GivensPair;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.Jacobi;
import jacobi.test.util.JacobiJUnit4ClassRunner;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Y.K. Chan
 */
@JacobiImport("/jacobi/test/data/FrancisQRTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class FrancisQRTest {
    
    @JacobiInject(0)
    public Matrix input;
    
    @JacobiInject(1)
    public Matrix bulged;
    
    @JacobiInject(2)
    public Matrix before;
    
    @JacobiResult(10)
    public Matrix output;
    
    @JacobiResult(11)
    public Matrix partner;
    
    @Test
    @SuppressWarnings("InfiniteRecursion") 
    public void testFallThroughOnSmallMatrices() {
        AtomicInteger call1x1 = new AtomicInteger(0);
        AtomicInteger call2x2 = new AtomicInteger(0);
        AtomicInteger call3x3 = new AtomicInteger(0);
        new FrancisQR( (m, p, i, j, up) -> { call1x1.incrementAndGet(); return 0; })
                .compute(Matrices.zeros(3, 3), null, 2, 3, true);
        new FrancisQR( (m, p, i, j, up) -> { call2x2.incrementAndGet(); return 0; })
                .compute(Matrices.zeros(3, 3), null, 1, 3, true);
        new FrancisQR( (m, p, i, j, up) -> { call3x3.incrementAndGet(); return 0; })
                .compute(Matrices.zeros(3, 3), null, 0, 3, true);
        Assert.assertEquals(1, call1x1.get());
        Assert.assertEquals(1, call2x2.get());
        Assert.assertEquals(1, call3x3.get());
    }
    
    @Test
    @JacobiImport("6x6")
    @JacobiEquals(expected = 10, actual = 10)
    @JacobiEquals(expected = 11, actual = 11)
    public void test6x6() {        
        this.partner = Matrices.identity(6);
        this.mock().compute(input, partner, 0, 6, true);
        this.output = this.input;
    }    
    
    protected FrancisQR mock() {
        return new FrancisQR(new DefaultQRStep()){

            @Override
            protected GivensPair createBulge(Matrix matrix, int begin, int end, boolean full) {
                GivensPair giv = super.createBulge(matrix, begin, end, full);
                Jacobi.assertEquals(bulged, matrix);
                return giv;
            } 
            
        };
    }
}
