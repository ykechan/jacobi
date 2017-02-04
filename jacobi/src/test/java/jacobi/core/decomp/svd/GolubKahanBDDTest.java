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
import jacobi.core.impl.ImmutableMatrix;
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
@JacobiImport("/jacobi/test/data/GolubKahanBDDTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class GolubKahanBDDTest {
    
    @JacobiInject(0)
    public Matrix input;
    
    @JacobiInject(-1)
    public Map<Integer, Matrix> steps;
    
    @JacobiResult(100)
    public Matrix output;
    
    public GolubKahanBDDTest() {
    }
    
    @Test
    @JacobiImport("Upper Bi-Diag 5x5")
    @JacobiEquals(expected = 1, actual = 100)
    public void testUpperBiDiag5x5() {
        this.output = this.biDiag(Mode.UPPER, this.input.getRow(0));
    }
    
    @Test
    @JacobiImport("Lower Bi-Diag 7x7")
    @JacobiEquals(expected = 1, actual = 100)
    public void testLowerBiDiag7x7() {
        this.output = this.biDiag(Mode.LOWER, this.input.getRow(0));
    }
    
    @Test
    @JacobiImport("Upper Thin 10x3")
    @JacobiEquals(expected = 100, actual = 100)
    public void testUpperThin10x3() {
        double[] biDiag = new GolubKahanBDD().compute(Mode.UPPER, input);
        this.output = this.biDiag(Mode.UPPER, biDiag);
    }
    
    @Test
    @JacobiImport("Upper Fat 3x5")
    @JacobiEquals(expected = 100, actual = 100)
    public void testUpperFat3x5() {
        double[] biDiag = new GolubKahanBDD().compute(Mode.UPPER, input);
        this.output = Matrices.of(new double[][]{biDiag});
    }
    
    @Test
    @JacobiImport("Lower Thin 5x4")
    @JacobiEquals(expected = 100, actual = 100)
    public void testLowerThin5x4() {
        double[] biDiag = new GolubKahanBDD().compute(Mode.LOWER, input);
        this.output = Matrices.of(new double[][]{biDiag});
    }
    
    @Test
    @JacobiImport("Lower Fat 4x6")
    @JacobiEquals(expected = 100, actual = 100)
    public void testLowerFat4x6() {
        double[] biDiag = new GolubKahanBDD().compute(Mode.LOWER, input);
        this.output = this.biDiag(Mode.LOWER, biDiag);
    }
    
    @Test
    @JacobiImport("Upper 5x5")
    @JacobiEquals(expected = 100, actual = 100)
    public void testUpper5x5() {
        double[] biDiag = new GolubKahanBDD().compute(Mode.UPPER, input);
        this.output = this.biDiag(Mode.UPPER, biDiag);
    }
    
    @Test
    @JacobiImport("Lower 5x5")
    @JacobiEquals(expected = 100, actual = 100)
    public void testLower5x5() {
        double[] biDiag = new GolubKahanBDD().compute(Mode.LOWER, input);
        this.output = this.biDiag(Mode.LOWER, biDiag);
    }
    
    private Matrix biDiag(BiDiagDecomp.Mode mode, double[] elem) {
        return new ImmutableMatrix() {

            @Override
            public int getRowCount() {
                return elem.length / 2;
            }

            @Override
            public int getColCount() {
                return elem.length / 2;
            }

            @Override
            public double[] getRow(int index) {
                double[] row = new double[this.getColCount()];
                int srcIdx = 2*index - (mode == Mode.UPPER ? 0 : 1);
                int destIdx = index - (mode == Mode.UPPER ? 0 : 1);
                if(destIdx >= 0){
                    row[destIdx] = elem[srcIdx];
                }                
                if(++destIdx < row.length){
                    row[destIdx] = elem[++srcIdx];
                }
                return row;
            }
        };
    }
    
    private GolubKahanBDD upper, lower;
}
