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

package jacobi.core.decomp.gauss;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.api.annotations.Delegate;
import jacobi.api.annotations.NonPerturbative;
import jacobi.api.ext.Op;
import jacobi.api.ext.Prop;
import jacobi.core.util.Throw;
import java.util.Arrays;

/**
 *
 * @author Y.K. Chan
 */
public class Permutation implements Matrix {

    public Permutation(int length) {
        this.indices = new int[length];
        for(int i = 0; i < this.indices.length; i++){
            this.indices[i] = i;
        }
    }

    private Permutation(int[] indices, int order) { // NOPMD - private usage
        this.indices = indices; 
        this.order = order;
    }

    @Override
    public Matrix swapRow(int i, int j) {
        if(i != j){            
            int temp = this.indices[i];
            this.indices[i] = this.indices[j];
            this.indices[j] = temp;
            this.order *= -1;
        }
        return this;
    }
    
    @NonPerturbative
    @Delegate(facade = Prop.class, method = "det")
    public double det() {
        return this.order;
    }
    
    @NonPerturbative
    @Delegate(facade = Prop.class, method = "inv")
    public Matrix inv() {
        int[] inverse = new int[this.indices.length];
        for(int i = 0; i < inverse.length; i++){
            inverse[ this.indices[i] ] = i;
        }
        return new Permutation(inverse, this.order);
    }
    
    @NonPerturbative
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
        Matrix ans = Matrices.zeros(b.getRowCount(), b.getColCount());
        for(int i = 0; i < ans.getRowCount(); i++){
            ans.setRow(i, b.getRow(this.indices[i]));
        }
        return ans;
    }

    protected double[] getLogicalRow(int index) {
        double[] row = new double[this.indices.length];
        row[ this.indices[index] ] = 1.0;
        return row;
    }

    @Override
    public int getRowCount() {
        return this.indices.length;
    }

    @Override
    public int getColCount() {
        return this.indices.length;
    }
    
    @Override
    public double[] getRow(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Matrix setRow(int index, double[] values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Matrix copy() {
        return new Permutation(Arrays.copyOf(this.indices, this.getRowCount()), order);
    }

    @Override
    public <T> T ext(Class<T> clazz) {
        throw new UnsupportedOperationException();
    }

    private int[] indices;
    private int order;
}
