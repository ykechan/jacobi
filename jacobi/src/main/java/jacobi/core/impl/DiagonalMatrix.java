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

package jacobi.core.impl;

import jacobi.api.Matrix;
import jacobi.api.annotations.Delegate;
import jacobi.api.ext.Op;
import jacobi.api.ext.Prop;
import jacobi.core.util.Throw;
import java.util.Arrays;
import java.util.Optional;

/**
 *
 * Implementation of a diagonal matrix. This class stores the diagonal values
 * only, which saves many un-necessary zero elements. To maintain being
 * diagonal, this class is immutable.
 * 
 * 
 * 
 * @author Y.K. Chan
 */
public class DiagonalMatrix extends ImmutableMatrix {

    public DiagonalMatrix(double[] vector) {
        this.vector = Arrays.copyOf(vector, vector.length);
    }

    @Override
    public int getRowCount() {
        return this.vector.length;
    }

    @Override
    public int getColCount() {
        return this.vector.length;
    }

    @Override
    public double[] getRow(int index) {
        return new double[]{this.vector[index]};
    }        

    @Override
    public double get(int i, int j) {
        return i == j ? this.vector[i] : 0.0;
    }

    @Override
    public Matrix copy() {
        return CopyOnWriteMatrix.of(new DiagonalMatrix(this.vector));
    }
    
    @Delegate(facade = Prop.class, method = "det")
    public double det() {
        double ans = 1.0;
        for(int i = 0; i < this.vector.length; i++){
            ans *= this.vector[i];
        }
        return ans;
    }
    
    @Delegate(facade = Prop.class, method = "inv")
    public Optional<Matrix> inv() {
        double[] diag = new double[this.vector.length];
        for(int i = 0; i < diag.length; i++){
            if(Math.abs(this.vector[i]) < 1e-12){
                return Optional.empty();
            }
            diag[i] = 1.0 / this.vector[i];
        }
        return Optional.of(CopyOnWriteMatrix.of(new DiagonalMatrix(diag)));
    }
    
    @Delegate(facade = Op.class, method = "mul")
    public Matrix mul(Matrix b) {
        Throw.when()
            .isNull(() -> b, () -> "Missing 2nd operand.")
            .isTrue(
                () -> b.getRowCount() != this.getRowCount(),
                () -> "Dimension mismatch. Expects " 
                        + this.getRowCount() 
                        + " rows, actual operand has " 
                        + b.getRowCount()
                        + " rows.");
        if(b instanceof DiagonalMatrix){
            return this.mul((DiagonalMatrix) b);
        }
        if(b instanceof ColumnVector){
            return this.mul((ColumnVector) b);
        }
        Matrix ans = new DefaultMatrix(b);
        for(int i = 0; i < this.vector.length; i++){
            double[] row = ans.getRow(i);
            double k = this.vector[i];
            for(int j = 0; j < row.length; j++){
                row[j] *= k;
            }
            ans.setRow(i, vector);
        }
        return ans;
    }
    
    private Matrix mul(DiagonalMatrix diag) {
        return CopyOnWriteMatrix.of(new DiagonalMatrix(
                this.mul(this.vector, diag.vector)
        ));
    }
    
    private Matrix mul(ColumnVector v) {
        return new ColumnVector(this.mul(this.vector, v.getVector()));
    }

    private double[] mul(double[] u, double[] v) {
        double[] w = new double[this.vector.length];
        for(int i = 0; i < w.length; i++){
            w[i] = u[i] * v[i];
        }
        return w;
    }
    
    private double[] vector;
}
