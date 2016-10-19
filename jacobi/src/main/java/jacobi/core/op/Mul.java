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
package jacobi.core.op;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.api.annotations.Immutate;
import jacobi.core.util.Throw;
import java.util.stream.IntStream;

/**
 *
 * @author Y.K. Chan
 */
@Immutate
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
            ans.setRow(i, v);
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
                    return w;
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
