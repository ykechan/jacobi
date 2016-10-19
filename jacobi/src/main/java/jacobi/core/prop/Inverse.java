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
package jacobi.core.prop;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.core.decomp.gauss.FullMatrixOperator;
import jacobi.core.decomp.gauss.GenericGaussianElim;
import jacobi.core.impl.DefaultMatrix;
import jacobi.core.solver.Substitution;
import jacobi.core.util.Throw;
import java.util.Optional;

/**
 * Implementation for finding inverse.
 * 
 * Currently it uses Gaussian Elimination against an identity matrix.
 * 
 * @author Y.K. Chan
 */
public class Inverse {

    public Inverse() {
        this.gaussElim = new GenericGaussianElim();
    }
    
    public Optional<Matrix> computeMaybe(Matrix a) {
        return Optional.ofNullable(this.compute(a));
    }
    
    public Matrix compute(Matrix a) {
        Throw.when()
            .isNull(() -> a, () -> "No matrix to invert.");
        if(a.getRowCount() != a.getColCount()
        || a.getRowCount() == 0){
            return null;
        }
        switch(a.getRowCount()){
            case 0 : throw new IllegalStateException();
            case 1 : return this.inverse1x1(a);
            case 2 : return this.inverse2x2(a);
            case 3 : return this.inverse3x3(a);
            default :
                break;
        }
        Matrix y = Matrices.identity(a.getRowCount());
        this.gaussElim.compute(a, (op) -> new FullMatrixOperator(op, y));
        
        return new Substitution(Substitution.Mode.BACKWARD, a).compute(y);
    }

    private Matrix inverse1x1(Matrix a) {
        return Math.abs(a.get(0, 0)) < 1e-12 ? null : Matrices.scalar(1.0 / a.get(0, 0));
    }
    
    private Matrix inverse2x2(Matrix a) {
        double[] r0 = a.getRow(0);
        double[] r1 = a.getRow(0);
        double det = r0[0] * r1[1] - r0[1] * r1[0];
        if(Math.abs(det) < 1e-12){
            return null;
        }
        return new DefaultMatrix(new double[][]{
            {-r1[1] / det,  r0[0] / det },
            { r1[0] / det, -r0[1] / det }
        });
    }
    
    private Matrix inverse3x3(Matrix a) {
        double det = new Determinant().compute3x3(a);
        if(Math.abs(det) < 1e-12){
            return null;
        }
        double[] r0 = a.getRow(0);
        double[] r1 = a.getRow(1);
        double[] r2 = a.getRow(2);
        return new DefaultMatrix(new double[][]{
            
            { 
                (r1[1]*r2[2] - r2[1]*r1[2]) / det ,
               //-(r0[1]*r2[2] - r2[1]*r1[2]) / det ,
                (r2[1]*r1[2] - r0[1]*r2[2]) / det ,
                (r0[1]*r2[2] - r1[1]*r1[2]) / det ,
            },
            {
               //-(r1[0]*r2[2] - r2[0]*r1[2]) / det ,
                (r2[0]*r1[2] - r1[0]*r2[2]) / det ,
                (r0[0]*r2[2] - r2[0]*r1[2]) / det ,
               //-(r0[0]*r2[2] - r1[0]*r1[2]) / det 
                (r1[0]*r1[2] - r0[0]*r2[2]) / det 
            },
            {
                (r1[0]*r2[1] - r2[0]*r1[1]) / det ,
               //-(r0[0]*r2[1] - r2[0]*r1[1]) / det ,
                (r2[0]*r1[1] - r0[0]*r2[1]) / det ,
                (r0[0]*r2[1] - r1[0]*r1[1]) / det 
            }
        });
    }
    
    private GenericGaussianElim gaussElim;
}
