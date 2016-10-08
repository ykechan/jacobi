/*
 * Copyright (C) 2016 Y.K. Chan
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package jacobi.core.decomp.qr;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.core.decomp.qr.step.GivensQR;
import jacobi.core.decomp.qr.step.GivensQR.Givens;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.Jacobi;
import jacobi.test.util.JacobiJUnit4ClassRunner;
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
