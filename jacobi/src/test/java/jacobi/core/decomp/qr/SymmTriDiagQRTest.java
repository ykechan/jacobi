/* 
 * The MIT License
 *
 * Copyright 2016 Y.K. Chan
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
        double[][] triDiag = new SymmTriDiagQR((m, p, up) -> m).toTriDiag(this.input);
        Jacobi.assertEquals(this.output, Matrices.of(triDiag));
    }
    
    @Test
    @JacobiImport("ToTriDiag Non-Symm 5x5")
    public void testToTriDiagNonSymm5x5() {
        double[][] triDiag = new SymmTriDiagQR((m, p, up) -> m).toTriDiag(this.input);        
        Assert.assertNull(triDiag);
    }
    
    @Test
    @JacobiImport("ToTriDiag Non-Symm 4x4")
    public void testToTriDiagNonSymm4x4() {
        double[][] triDiag = new SymmTriDiagQR((m, p, up) -> m).toTriDiag(this.input);        
        Assert.assertNull(triDiag);
    }
    
    @Test
    @JacobiImport("Step 6x6")
    public void testStep6x6() {
        double[] diag = this.input.getRow(0);
        double[] subDiag = this.input.getRow(1);
        this.mockForStepTest(shifted, intermit, output).step(diag, subDiag, null, 0, diag.length);
    }
    
    @Test
    @JacobiImport("Step 5x5")
    public void testStep5x5One() {
        double[] diag = this.input.getRow(0);
        double[] subDiag = this.input.getRow(1);
        this.mockForStepTest(shifted, intermit, output).step(diag, subDiag, null, 0, diag.length);
    }
    
    @Test
    @JacobiImport("Step 5x5 (2)")
    public void testStep5x5Two() {
        double[] diag = this.input.getRow(0);
        double[] subDiag = this.input.getRow(1);
        this.mockForStepTest(shifted, intermit, output).step(diag, subDiag, null, 0, diag.length);
    }
    
    @Test
    @JacobiImport("Step 5x5 (3)")
    public void testStep5x5Three() {
        double[] diag = this.input.getRow(0);
        double[] subDiag = this.input.getRow(1);
        this.mockForStepTest(shifted, intermit, output).step(diag, subDiag, null, 0, diag.length);
    }
    
    @Test
    @JacobiImport("Step 5x5 (4)")
    public void testStep5x5Four() {
        double[] diag = this.input.getRow(0);
        double[] subDiag = this.input.getRow(1);
        this.mockForStepTest(shifted, intermit, output).step(diag, subDiag, null, 0, diag.length);
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
    
    private SymmTriDiagQR mock() {
        return new SymmTriDiagQR(new BasicQR(new DefaultQRStep()));
    }
    
    private SymmTriDiagQR mockForStepTest(Matrix afterShift, Matrix afterQR, Matrix result) {
        return new SymmTriDiagQR((m, p, up) -> m){

            @Override
            protected double preCompute(double[] diag, double[] subDiag, int begin, int end) {
                double shift = super.preCompute(diag, subDiag, begin, end);
                Jacobi.assertEquals(afterShift, Matrices.of(new double[][]{diag, subDiag}));
                return shift;
            }

            @Override
            protected List<Givens> qrDecomp(double[] diag, double[] subDiag, int begin, int end) {
                List<Givens> rotList = super.qrDecomp(diag, subDiag, begin, end);
                Jacobi.assertEquals(afterQR, Matrices.of(new double[][]{diag, subDiag}));
                return rotList;
            }

            @Override
            protected void postCompute(double[] diag, double[] subDiag, int begin, int end, double shift) {
                super.postCompute(diag, subDiag, begin, end, shift);
                Jacobi.assertEquals(result, Matrices.of(new double[][]{diag, subDiag}));
            }            
            
        };
    }    
}
