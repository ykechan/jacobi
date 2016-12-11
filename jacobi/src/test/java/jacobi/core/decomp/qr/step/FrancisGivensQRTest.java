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
import jacobi.core.decomp.qr.step.shifts.BatchGivens;
import jacobi.core.decomp.qr.step.shifts.DoubleShift;
import jacobi.core.decomp.qr.step.shifts.GivensPair;
import jacobi.core.decomp.qr.step.shifts.SingleBulgeChaser;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.Jacobi;
import jacobi.test.util.JacobiJUnit4ClassRunner;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Y.K. Chan
 */
@JacobiImport("/jacobi/test/data/FrancisGivensQRTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class FrancisGivensQRTest {
    
    @JacobiInject(0)
    @JacobiResult(10)
    public Matrix input;
     
    @JacobiResult(11)
    public Matrix result;
    
    @JacobiInject(-1)
    public Map<Integer, Matrix> steps;
    
    @Test
    @JacobiImport("6x6")
    @JacobiEquals(expected = 10, actual = 10)
    @JacobiEquals(expected = 11, actual = 11)
    public void test6x6() {
        this.result = Matrices.identity(6);
        this.mock().compute(input, result, 0, 6, true);
    }
    
    @Test
    @JacobiImport("6x6(2)")
    @JacobiEquals(expected = 10, actual = 10)
    @JacobiEquals(expected = 11, actual = 11)
    public void test6x6At2nd() {
        this.result = Matrices.identity(6);
        this.mock().compute(input, result, 0, 6, true);
    }
    
    @Test
    @JacobiImport("6x6(3)")
    @JacobiEquals(expected = 10, actual = 10)
    @JacobiEquals(expected = 11, actual = 11)
    public void test6x6At3rd() {
        this.result = Matrices.identity(6);
        this.mock().compute(input, result, 0, 6, true);
    }
    
    @Test
    @JacobiImport("6x6(4)")
    @JacobiEquals(expected = 10, actual = 10)
    @JacobiEquals(expected = 11, actual = 11)
    public void test6x6At4th() {
        this.result = Matrices.identity(6);
        this.mock().compute(input, result, 0, 6, true);
    }
    
    @Test
    @JacobiImport("6x6(5)")
    @JacobiEquals(expected = 10, actual = 10)
    @JacobiEquals(expected = 11, actual = 11)
    public void test6x6At5th() {
        this.result = Matrices.identity(6);
        int deflated = this.mock().compute(input, result, 0, 6, true);
        Assert.assertEquals(5, deflated);
    }
    
    protected FrancisGivensQR mock() {
        AtomicInteger k = new AtomicInteger(0);
        return new FrancisGivensQR(new DefaultQRStep(), new SingleBulgeChaser(){

            @Override
            protected GivensPair createBulge(Arguments args, DoubleShift shift, Runnable barrier) {
                try {
                    return super.createBulge(args, shift, barrier);
                } finally {
                    Jacobi.assertEquals(steps.get(k.incrementAndGet()), args.matrix);
                }
            }

            @Override
            protected GivensPair pushBulge(Arguments args, int atRow, Runnable barrier) {
                try {
                    return super.pushBulge(args, atRow, barrier);
                } finally {
                    Jacobi.assertEquals(steps.get(k.incrementAndGet()), args.matrix);
                }
            }                        
            
        }, new BatchGivens(1024));
    }
    
}
