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
package jacobi.core.decomp.svd;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.core.decomp.svd.BiDiagDecomp.Mode;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.JacobiJUnit4ClassRunner;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Y.K. Chan
 */
@JacobiImport("/jacobi/test/data/BiDiagDecompTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class BiDiagDecompTest {
    
    @JacobiInject(0)
    public Matrix input;
    
    @JacobiInject(1)
    public Map<Integer, Matrix> steps;
    
    @JacobiResult(100)
    public Matrix output;
    
    public BiDiagDecompTest() {
        this.upper = new BiDiagDecomp(Mode.UPPER);
        this.lower = new BiDiagDecomp(Mode.LOWER);
    }
    
    @Test
    @JacobiImport("Upper Thin 10x3")
    @JacobiEquals(expected = 100, actual = 100)
    public void testUpperThin10x3() {
        double[] biDiag = this.upper.compute(input, (hh) -> {}, (hh) -> {});
        this.output = Matrices.of(new double[][]{biDiag});
    }
    
    @Test
    @JacobiImport("Upper Fat 3x5")
    @JacobiEquals(expected = 100, actual = 100)
    public void testUpperFat3x5() {
        double[] biDiag = this.upper.compute(input, (hh) -> {}, (hh) -> {});
        this.output = Matrices.of(new double[][]{biDiag});
    }
    
    @Test
    @JacobiImport("Lower Thin 5x4")
    @JacobiEquals(expected = 100, actual = 100)
    public void testLowerThin5x4() {
        double[] biDiag = this.lower.compute(input, (hh) -> {}, (hh) -> {});
        this.output = Matrices.of(new double[][]{biDiag});
    }
    
    @Test
    @JacobiImport("Lower Fat 4x6")
    @JacobiEquals(expected = 100, actual = 100)
    public void testLowerFat4x6() {
        double[] biDiag = this.lower.compute(input, (hh) -> {}, (hh) -> {});
        this.output = Matrices.of(new double[][]{biDiag});
    }
    
    @Test
    @JacobiImport("Upper 5x5")
    @JacobiEquals(expected = 100, actual = 100)
    public void testUpper5x5() {
        double[] biDiag = this.upper.compute(input, (hh) -> {}, (hh) -> {});
        this.output = Matrices.of(new double[][]{biDiag});
    }
    
    @Test
    @JacobiImport("Lower 5x5")
    @JacobiEquals(expected = 100, actual = 100)
    public void testLower5x5() {
        double[] biDiag = this.lower.compute(input, (hh) -> {}, (hh) -> {});
        this.output = Matrices.of(new double[][]{biDiag});
    }
    
    private BiDiagDecomp upper, lower;
}
