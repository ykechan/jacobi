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

package jacobi.core.decomp.svd;

import jacobi.api.Matrix;
import jacobi.core.decomp.qr.Householder;
import jacobi.core.util.Throw;
import java.util.function.Consumer;

/**
 * Implementation of Golub-Kahan bi-diagonal decomposition.
 * 
 * <p>This is similar to Hessenberg decomposition only the orthogonal matrix for left and right are allowed
 * to be different such that a bi-diagonal matrix can be resulted.</p>
 * 
 * @author Y.K. Chan
 */
public class GolubKahanBDD implements BiDiagDecomp {
    
    @Override
    public double[] compute(Mode mode, Matrix input, Consumer<Householder> qFunc, Consumer<Householder> vFunc) {
        Throw.when()
                .isNull(() -> input, () -> "No matrix to compute.")
                .isNull(() -> qFunc, () -> "No left listener function.")
                .isNull(() -> vFunc, () -> "No right listener function.");
        double[] row = new double[input.getColCount()];
        double[] col = new double[input.getRowCount()];        
        int n = Math.min(input.getRowCount(), input.getColCount());
        double[] elem = new double[2*n];
        for(int i = 0; i < n; i++){
            if(mode == Mode.UPPER){
                elem[2*i] = this.applyLeft(mode, input, i, col, qFunc);
                elem[2*i + 1] = this.applyRight(mode, input, i, row, vFunc);
            }else{
                elem[2*i] = this.applyRight(mode, input, i, row, vFunc);
                elem[2*i + 1] = this.applyLeft(mode, input, i, col, qFunc);
            } 
        }
        return elem;
    }
    
    /**
     * Apply Householder reflector on the left.
     * @param mode  Indicate E to be upper or lower triangular matrix
     * @param input  Input matrix A
     * @param at  Index of target diagonal element
     * @param col  Column buffer
     * @param qFunc  Accepts Householder reflection applied to A
     * @return  First element of reflection resultant
     */
    protected double applyLeft(Mode mode, Matrix input, int at, double[] col, Consumer<Householder> qFunc) { 
        int offset = mode == Mode.UPPER ? 0 : 1;        
        this.getColumn(input, at + offset, at, col); 
        Householder hh = new Householder(col, at + offset);
        double norm = hh.normalize();        
        if(norm == 0.0){
            return at + offset < col.length ? col[at + offset] : 0.0;
        }
        if(at < Math.min(input.getRowCount(), input.getColCount())){
            hh.applyLeft(input, at + 1);
        }
        qFunc.accept(hh);
        return norm;
    }
    
    /**
     * Apply Householder reflector on the right.
     * @param mode  Indicate E to be upper or lower triangular matrix
     * @param input  Input matrix A
     * @param at  Index of target diagonal element
     * @param row  Row buffer
     * @param vFunc  Accepts Householder reflection applied to A
     * @return  First element of reflection resultant
     */
    protected double applyRight(Mode mode, Matrix input, int at, double[] row, Consumer<Householder> vFunc) {
        int offset = mode == Mode.UPPER ? 1 : 0;
        int index = at + offset;
        System.arraycopy(input.getRow(at), index, row, index, input.getColCount() - index);
        Householder hh = new Householder(row, index);
        double norm = hh.normalize();
        if(norm == 0.0){
            return index < row.length ? row[index] : 0.0;
        }
        if(at < Math.min(input.getRowCount(), input.getColCount())){
            hh.applyRight(input, at + 1);
        }
        vFunc.accept(hh);
        return norm;
    }
    
    /**
     * Get the Householder reflector of a column.
     * @param matrix  Matrix A
     * @param fromRow  Start row index of reflection
     * @param colIndex  Column index of column to be reflected
     * @param column  Column reflector buffer
     */
    protected void getColumn(Matrix matrix, int fromRow, int colIndex, double[] column) {        
        for(int i = fromRow; i < matrix.getRowCount(); i++){
            column[i] = matrix.get(i, colIndex);
        }
    }
    
}
