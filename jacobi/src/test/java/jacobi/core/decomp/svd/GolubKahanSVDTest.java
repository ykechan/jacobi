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
package jacobi.core.decomp.svd;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.Jacobi;
import jacobi.test.util.JacobiJUnit4ClassRunner;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Y.K. Chan
 */
@JacobiImport("/jacobi/test/data/GolubKahanSVDTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class GolubKahanSVDTest {
    
    @JacobiInject(0)
    public Matrix input;
    
    @JacobiResult(10)
    public Matrix uMat;
    
    @JacobiResult(11)
    public Matrix vMat;
    
    @JacobiInject(-1)
    public Map<Integer, Matrix> steps;
    
    @JacobiResult(100)
    public Matrix result;
    
    @Test
    @JacobiImport("Wilkinson 5x5")
    @JacobiEquals(expected = 100, actual = 100)
    public void testWilkinson5x5() {
        double shift = new GolubKahanSVD().wilkinson(this.input.getRow(0), this.input.getRow(1), 0, 5);
        this.result = Matrices.scalar(shift);
    }
    
    @Test
    @JacobiImport("Wilkinson 2x2 in 4x4")
    @JacobiEquals(expected = 100, actual = 100)
    public void testWilkinson2x2In4x4() {
        double shift = new GolubKahanSVD().wilkinson(this.input.getRow(0), this.input.getRow(1), 2, 4);
        this.result = Matrices.scalar(shift);
    }
    
    @Test
    @JacobiImport("Wilkinson 2x2 in 4x4 (2)")
    @JacobiEquals(expected = 100, actual = 100)
    public void testWilkinson2x2In4x4Two() {
        double shift = new GolubKahanSVD().wilkinson(this.input.getRow(0), this.input.getRow(1), 0, 2);
        this.result = Matrices.scalar(shift);
    }
    
    @Test
    @JacobiImport("5x5")
    public void test5x5() {
        this.mock().compute(this.input.getRow(0), this.input.getRow(1), 0, 5, null, null);
        this.result = this.input;
    }
    
    @Test
    @JacobiImport("5x5(2)")
    public void test5x5Two() {
        this.mock().compute(this.input.getRow(0), this.input.getRow(1), 0, 5, null, null);
        this.result = this.input;
    }
    
    @Test
    @JacobiImport("5x5(3)")
    public void test5x5Three() {
        this.mock().compute(this.input.getRow(0), this.input.getRow(1), 0, 5, null, null);
        this.result = this.input;
    }
    
    @Test
    @JacobiImport("5x5(4)")
    public void test5x5Four() {
        this.mock().compute(this.input.getRow(0), this.input.getRow(1), 0, 5, null, null);
        this.result = this.input;
    }
    
    @Test
    @JacobiImport("Full 5x5")
    @JacobiEquals(expected = 10, actual = 10)
    @JacobiEquals(expected = 11, actual = 11)
    public void testFull5x5() {
        this.uMat = Matrices.identity(5);
        this.vMat = Matrices.identity(5);
        this.mock().compute(this.input.getRow(0), this.input.getRow(1), 0, 5, uMat, vMat);
        this.result = this.input;
    }
    
    @Test
    @JacobiImport("3x3 In 6x6")
    public void test3x3In6x6() {
        this.mock().compute(this.input.getRow(0), this.input.getRow(1), 1, 4, null, null);
        this.result = this.input;
    }
    
    @Test
    @JacobiImport("3x3 In 6x6 (2)")
    public void test3x3In6x6Two() {
        this.mock().compute(this.input.getRow(0), this.input.getRow(1), 1, 4, null, null);
        this.result = this.input;
    }
    
    @Test
    @JacobiImport("Full 4x4 In 6x6")
    @JacobiEquals(expected = 10, actual = 10)
    @JacobiEquals(expected = 11, actual = 11)
    public void testFull4x4In6x6() {
        this.uMat = Matrices.identity(6);
        this.vMat = Matrices.identity(6);
        this.mock().compute(this.input.getRow(0), this.input.getRow(1), 1, 5, uMat, vMat);
        this.result = this.input;
    }
    
    private GolubKahanSVD mock() {
        AtomicInteger count = new AtomicInteger(0);
        return new GolubKahanSVD() {

            @Override
            protected GolubKahanSVD.Step createBulge(double[] diag, double[] supDiag, int at, double shift) {
                Step step = super.createBulge(diag, supDiag, at, shift);
                double[] padded = Arrays.copyOf(supDiag, supDiag.length);
                padded[padded.length - 1] = step.bulge;
                Jacobi.assertEquals(steps.get(count.incrementAndGet()), Matrices.of(new double[][]{diag, padded}));
                return step;
            }

            @Override
            protected GolubKahanSVD.Step pushDown(double[] diag, double[] supDiag, int at, double bulge) {
                Step step = super.pushDown(diag, supDiag, at, bulge); 
                double[] padded = Arrays.copyOf(supDiag, supDiag.length);
                padded[padded.length - 1] = step.bulge;
                Jacobi.assertEquals(steps.get(count.incrementAndGet()), Matrices.of(new double[][]{diag, padded}));
                return step;
            }                        
            
        };
    }
}
