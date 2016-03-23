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

package jacobi.core.op;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.api.annotations.NonPerturbative;
import jacobi.core.util.Throw;
import java.util.stream.IntStream;

/**
 *
 * @author Y.K. Chan
 */
@NonPerturbative
public class Mul {
    
    public Matrix compute(Matrix a, Matrix b) {
        Throw.when()
            .isNull(() -> a, () -> "First operand is missing.")
            .isNull(() -> b, () -> "Second operand is missing.")
            .isTrue(
                () -> a.getColCount() != b.getRowCount(), 
                () -> "Dimension mismatch. Unable to multiply a "
                    + a.getRowCount()+ "x" + a.getColCount()
                    + " matrix with a "
                    + b.getRowCount()+ "x" + b.getColCount()
                    + " matrix.");
        Matrix ans = Matrices.zeros(a.getRowCount(), b.getColCount());
        this.compute(a, b, ans);
        return ans;
    }

    protected void compute(Matrix a, Matrix b, Matrix ans) {
        for(int i = 0; i < ans.getRowCount(); i++){
            double[] u = a.getRow(i);
            double[] v = ans.getRow(i);
            this.compute(u, b, v);
        }
    }
    
    protected void compute(double[] u, Matrix b, double[] v) {
        if(b.getRowCount() > DEFAULT_THRESHOLD){
            System.arraycopy(this.stream(u, b), 0, v, 0, v.length);
        }else{
            this.serial(u, b, v);
        }
    }
    
    protected void serial(double[] u, Matrix b, double[] v) {
        for(int i = 0; i < b.getRowCount(); i++){
            double k = u[i];
            double[] row = b.getRow(i);
            for(int j = 0; j < v.length; j++){
                v[j] += k * row[j];
            }
        }
    }
    
    protected double[] stream(double[] u, Matrix b) {
        return IntStream.range(0, b.getRowCount())
                .parallel()
                .mapToObj((i) -> {
                    double k = u[i];
                    double[] w = new double[b.getColCount()];
                    double[] v = b.getRow(i);
                    for(int j = 0; j < v.length; j++){
                        w[j] = k * v[j];
                    }
                    return v;
                })
                .reduce((x, y) -> {
                    for(int j = 0; j < x.length; j++){
                        x[j] += y[j];
                    }
                    return x;
                })
                .orElseThrow(() -> new IllegalStateException());
        
    }
    
    private static final int DEFAULT_THRESHOLD = 1024;
}
