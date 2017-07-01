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
package jacobi.core.decomp.qr;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.core.decomp.qr.step.DefaultQRStep;
import jacobi.core.givens.Givens;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.util.Jacobi;
import jacobi.test.util.JacobiJUnit4ClassRunner;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Y.K. Chan
 */
@JacobiImport("/jacobi/test/data/SymmTriDiagQRTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class SymmTriDiagQRTest {
    
    @JacobiInject(1)
    public Matrix input;
    
    @JacobiInject(2)
    public Matrix shifted;
    
    @JacobiInject(3)
    public Matrix intermit;
    
    @JacobiInject(4)
    public Matrix output;
    
    @JacobiInject(10)
    public Matrix ans;
    
    @Test
    @JacobiImport("ToTriDiag 6x6")
    public void testToTriDiag6x6() {
        double[] diags = new SymmTriDiagQR((m, p, up) -> m).toTriDiag(this.input).get();
        Jacobi.assertEquals(this.output, this.diagsToRows(diags));
    }
    
    @Test
    @JacobiImport("ToTriDiag Non-Symm 5x5")
    public void testToTriDiagNonSymm5x5() {     
        Assert.assertFalse(new SymmTriDiagQR((m, p, up) -> m).toTriDiag(this.input).isPresent());
    }
    
    @Test
    @JacobiImport("ToTriDiag Non-Symm 4x4")
    public void testToTriDiagNonSymm4x4() {
        Assert.assertFalse(new SymmTriDiagQR((m, p, up) -> m).toTriDiag(this.input).isPresent());
    }
    
    @Test
    @JacobiImport("Step 6x6")
    public void testStep6x6() {
        double[] diags = this.toZNotation(this.input.getRow(0), this.input.getRow(1));
        this.mockForStepTest(shifted, intermit, output).step(diags, null, 0, diags.length / 2);
    }
    
    @Test
    @JacobiImport("Step 5x5")
    public void testStep5x5One() {
        //double[] diag = this.input.getRow(0);
        //double[] subDiag = this.input.getRow(1);
        //this.mockForStepTest(shifted, intermit, output).step(diag, subDiag, null, 0, diag.length);
        double[] diags = this.toZNotation(this.input.getRow(0), this.input.getRow(1));
        this.mockForStepTest(shifted, intermit, output).step(diags, null, 0, diags.length / 2);
    }
    
    @Test
    @JacobiImport("Step 5x5 (2)")
    public void testStep5x5Two() {
        //double[] diag = this.input.getRow(0);
        //double[] subDiag = this.input.getRow(1);
        double[] diags = this.toZNotation(this.input.getRow(0), this.input.getRow(1));
        this.mockForStepTest(shifted, intermit, output).step(diags, null, 0, diags.length / 2);
    }
    
    @Test
    @JacobiImport("Step 5x5 (3)")
    public void testStep5x5Three() {
        double[] diag = this.input.getRow(0);
        double[] subDiag = this.input.getRow(1);
        double[] diags = this.toZNotation(this.input.getRow(0), this.input.getRow(1));
        this.mockForStepTest(shifted, intermit, output).step(diags, null, 0, diags.length / 2);
    }
    
    @Test
    @JacobiImport("Step 5x5 (4)")
    public void testStep5x5Four() {
        //double[] diag = this.input.getRow(0);
        //double[] subDiag = this.input.getRow(1);
        double[] diags = this.toZNotation(this.input.getRow(0), this.input.getRow(1));
        this.mockForStepTest(shifted, intermit, output).step(diags, null, 0, diags.length / 2);
    }
    
    @Test
    @JacobiImport("6x6")
    public void test6x6() {
        new HessenbergDecomp().compute(this.input);
        Matrix values = this.mock().compute(this.input, null, true);
        
        List<Double> eigs = new ArrayList<>(values.getRowCount());
        for(int i = 0; i < values.getRowCount(); i++){
            eigs.add(values.get(i, i));
        }
        List<Double> exp = new ArrayList<>(ans.getRowCount());
        for(int i = 0; i < ans.getRowCount(); i++){
            exp.add(ans.get(i, 0));
        }
        Jacobi.assertEquals(exp, eigs, 1e-12);
    }
    
    @Test
    @SuppressWarnings("InfiniteRecursion") // false positive
    public void testFallThroughUnder3x3() {
        AtomicBoolean marker = new AtomicBoolean(false);
        new SymmTriDiagQR((mat, par, full) -> {
            marker.set(true);
            return mat;
        }).compute(Matrices.zeros(2, 2), null, true);
        Assert.assertTrue(marker.get());
    }
    
    private Matrix diagsToRows(double[] diags) {
        double[] diag = new double[diags.length / 2];
        double[] supDiag = new double[diag.length];
        for(int i = 0; i < diag.length; i++){
            diag[i] = diags[2*i];
            supDiag[i] = diags[2*i + 1];
        }
        return Matrices.unsafe(new double[][]{diag, supDiag});
    }
    
    private double[] toZNotation(double[] diags, double[] supDiags) {
        return IntStream.range(0, 2 * diags.length)
                .mapToDouble((i) -> i % 2 == 0 ? diags[i/2] : supDiags[i/2])
                .toArray();
    }
    
    private SymmTriDiagQR mock() {
        return new SymmTriDiagQR(new BasicQR(new DefaultQRStep()));
    }
    
    private SymmTriDiagQR mockForStepTest(Matrix afterShift, Matrix afterQR, Matrix result) {
        return new SymmTriDiagQR((m, p, up) -> m){           

            @Override
            protected List<Givens> qrDecomp(double[] diags, int begin, int end, double shift) {
                List<Givens> giv = super.qrDecomp(diags, begin, end, shift);
                Jacobi.assertEquals(afterQR, diagsToRows(diags));
                return giv;    
            }

            @Override
            protected int step(double[] diags, Matrix partner, int begin, int end) {
                int split = super.step(diags, partner, begin, end);
                Jacobi.assertEquals(result, diagsToRows(diags));
                return split;
            }
        };
    }    
}
