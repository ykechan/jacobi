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

package jacobi.core.prop;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.core.decomp.gauss.FullMatrixOperator;
import jacobi.core.decomp.gauss.GenericGaussianElim;
import jacobi.core.impl.DefaultMatrix;
import jacobi.core.solver.Substitution;
import jacobi.core.util.Throw;

/**
 * Implementation for finding inverse.
 * 
 * Currently it uses Gaussian Elimination against an identity matrix.
 * 
 * @author Y.K. Chan
 */
public class Inverse {
    
    public Matrix compute(Matrix a) {
        Throw.when()
            .isNull(() -> a, () -> "No matrix to invert.")
            .isTrue(
                () -> a.getRowCount() != a.getColCount(), 
                () -> "Non-square matrix is not invertible.")
            .isTrue(
                () -> a.getRowCount() == 0,
                () -> "Empty matrix has no inverse.");
        switch(a.getRowCount()){
            case 0 : throw new IllegalStateException();
            case 1 : return this.inverse1x1(a);
            case 2 : return this.inverse2x2(a);
            case 3 : return this.inverse3x3(a);
            default :
                break;
        }
        Matrix y = Matrices.identity(a.getRowCount());
        new GenericGaussianElim<>(
            a, (op) -> new FullMatrixOperator(op, y)
        ).compute(null);
        
        return new Substitution(Substitution.Mode.BACKWARD, a).compute(y);
    }

    private Matrix inverse1x1(Matrix a) {
        return Matrices.scalar(1.0 / a.get(0, 0));
    }
    
    private Matrix inverse2x2(Matrix a) {
        double[] r0 = a.getRow(0);
        double[] r1 = a.getRow(0);
        double det = r0[0] * r1[1] - r0[1] * r1[0];
        if(Math.abs(det) < 1e-12){
            throw new UnsupportedOperationException("Matrix is not invertible.");
        }
        return new DefaultMatrix(new double[][]{
            {-r1[1] / det,  r0[0] / det },
            { r1[0] / det, -r0[1] / det }
        });
    }
    
    private Matrix inverse3x3(Matrix a) {
        double det = new Determinant().compute3x3(a);
        if(Math.abs(det) < 1e-12){
            throw new UnsupportedOperationException("Matrix is not invertible.");
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
}
