/*
 * The MIT License
 *
 * Copyright 2016 Y.K. Chan.
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
import jacobi.core.decomp.qr.HouseholderReflector;
import jacobi.core.util.Throw;
import java.util.function.Consumer;

/**
 * Bi-Diagonal decomposition is to decompose a m-by-n matrix A into Q*E*V s.t. Q and V are othogonal, and E
 * is a bi-diagonal matrix. E can be chosen to be upper triangular and lower triangular.
 * 
 * This class disturb the value of the input matrix A and the resultant value is in-determined. Only the returned
 * bi-diagonal elements should be relied upon.
 * 
 * @author Y.K. Chan
 */
public class BiDiagDecomp {

    /**
     * Constructor.
     * @param mode  Specify whether upper or lower triangular matrix is desired.
     */
    public BiDiagDecomp(Mode mode) {
        this.mode = mode;
    }
    
    /**
     * Compute bi-diagonal decomposition A = Q*E*V.
     * @param input  Input matrix A
     * @return  Bi-diagonal elements in Z-notation.
     */
    public double[][] compute(Matrix input) {
        return this.compute(input, (hh) -> {}, (hh) -> {});
    }

    /**
     * Compute bi-diagonal decomposition A = Q*E*V.
     * @param input  Input matrix A
     * @param qFunc  Accepts Householder reflection applied to A on the left, i.e. a component of Q^t.
     * @param vFunc  Accepts Householder reflection applied to A on the right, i.e. a component of V^t.
     * @return  Bi-diagonal elements in Z-notation.
     */
    protected double[][] compute(Matrix input, Consumer<HouseholderReflector> qFunc, Consumer<HouseholderReflector> vFunc) {
        Throw.when()
                .isNull(() -> input, () -> "No matrix to compute.")
                .isNull(() -> qFunc, () -> "No left listener function.")
                .isNull(() -> vFunc, () -> "No right listener function.");
        double[] row = new double[input.getColCount()];
        double[] col = new double[input.getRowCount()];        
        //double[] biDiag = new double[2*Math.min(input.getRowCount(), input.getColCount())];
        int n = Math.min(input.getRowCount(), input.getColCount());
        double[] diag = new double[n];
        double[] offDiag = new double[n];
        for(int i = 0; i < n; i++){
            if(this.mode == Mode.UPPER){
                diag[i] = this.applyLeft(input, i, col, qFunc);
                offDiag[i] = this.applyRight(input, i, row, vFunc);
            }else{
                diag[i] = this.applyRight(input, i, row, qFunc);
                offDiag[i] = this.applyLeft(input, i, col, qFunc);
            }
        }
        return new double[][]{diag, offDiag};
    }
    
    /**
     * Apply Householder reflector on the left.
     * @param input  Input matrix A
     * @param at  Index of target diagonal element
     * @param col  Column buffer
     * @param qFunc  Accepts Householder reflection applied to A
     * @return  First element of reflection resultant
     */
    protected double applyLeft(Matrix input, int at, double[] col, Consumer<HouseholderReflector> qFunc) { 
        int offset = this.mode == Mode.UPPER ? 0 : 1;
        this.getColumn(input, at + offset, at, col);        
        HouseholderReflector hh = new HouseholderReflector(col, at + offset);
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
     * @param input  Input matrix A
     * @param at  Index of target diagonal element
     * @param row  Row buffer
     * @param vFunc  Accepts Householder reflection applied to A
     * @return  First element of reflection resultant
     */
    protected double applyRight(Matrix input, int at, double[] row, Consumer<HouseholderReflector> vFunc) {
        int offset = this.mode == Mode.UPPER ? 1 : 0;
        int index = at + offset;
        System.arraycopy(input.getRow(at), index, row, index, input.getColCount() - index);
        HouseholderReflector hh = new HouseholderReflector(row, index);
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
    
    private Mode mode;
    
    public enum Mode {
        UPPER, LOWER
    }    
    
}
