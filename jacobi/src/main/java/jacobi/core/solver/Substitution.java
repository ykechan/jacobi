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
package jacobi.core.solver;

import jacobi.api.Matrix;
import jacobi.core.util.Throw;
import java.util.stream.IntStream;

/**
 * Implementation of Backward and Forward Substitution.
 * 
 * For practical and performance reasons, this class would check if the input
 * matrix is truly upper or lower triangular. It is up to the caller to ensure
 * the data integrity of the input.
 * 
 * @author Y.K. Chan
 */
public class Substitution {
    
    /**
     * Constructor.
     * @param mode  Forward/Backward substitution
     * @param tri  Lower/Upper triangular matrix
     */
    public Substitution(Mode mode, Matrix tri) {
        Throw.when()
            .isNull(() -> mode, () -> "No substitution mode.")
            .isNull(() -> tri, () -> "No triangular matrix.");
        this.tri = tri;
        this.mode = mode;
    }
    
    /**
     * Compute substitution. This method is perturbative, i.e. it transforms 
     * the input into the output.
     * @param rhs  Right-hand side of the equation
     * @return  Object rhs after substitution, or null if unable to substitute
     */
    public Matrix compute(Matrix rhs) {
        Throw.when()
            .isNull(() -> rhs, () -> "No known values for substitution")
            .isTrue(
                () -> rhs.getRowCount() != this.tri.getRowCount(), 
                () -> "Dimension mismatch. Expects " 
                        + this.tri.getRowCount() 
                        + ", got " 
                        + rhs.getRowCount() 
                        + " known values."
            );        
        return this.tri.getRowCount() < this.tri.getColCount()
                ? null
                : this.mode == Mode.BACKWARD 
                    ? this.backward(rhs) 
                    : this.forward(rhs);
    }
    
    /**
     * Forward substitution. This method is perturbative, i.e. it transforms 
     * the input into the output.
     * @param rhs  Right-hand side of the equation
     * @return  Object rhs after substitution, or null if unable to substitute
     */
    protected Matrix forward(Matrix rhs) {
        int n = Math.min(this.tri.getRowCount(), this.tri.getColCount());
        for(int i = 0; i < n; i++){
            double[] sol = this.normalize(rhs, i);
            if(sol == null){
                return null;
            }
            this.substitute(rhs, sol, i, i + 1, rhs.getRowCount());
            rhs.setRow(i, sol);
        }
        return rhs;
    }
    
    /**
     * Backward substitution. This method is perturbative, i.e. it transforms 
     * the input into the output.
     * @param rhs  Right-hand side of the equation
     * @return  Object rhs after substitution, or null if unable to substitute
     */
    protected Matrix backward(Matrix rhs) {
        int n = Math.min(this.tri.getRowCount(), this.tri.getColCount()) - 1;
        for(int i = n; i >= 0; i--){
            double[] sol = this.normalize(rhs, i);
            if(sol == null){
                return null;
            }
            this.substitute(rhs, sol, i, 0, i);
            rhs.setRow(i, sol);
        }
        return rhs;
    }
    
    /**
     * Substitute into a range of rows after a row solution found.
     * @param rhs  Right-hand side matrix
     * @param subs  Row solution found
     * @param subIndex  Row solution index
     * @param begin  Begin of row to be substituted
     * @param end   End of row to be substituted
     */
    protected void substitute(Matrix rhs, double[] subs, int subIndex, int begin, int end) {
        if( (end - begin) * (subs.length - subIndex) < DEFAULT_THRESHOLD ){
            this.serial(rhs, subs, subIndex, begin, end);
        }else{
            this.stream(rhs, subs, subIndex, begin, end);
        }
    }
    
    /**
     * Substitute into a range of rows after a row solution found in serial.
     * @param rhs  Right-hand side matrix
     * @param subs  Row solution found
     * @param subIndex  Row solution index
     * @param begin  Begin of row to be substituted
     * @param end   End of row to be substituted
     */
    protected void serial(Matrix rhs, double[] subs, int subIndex, int begin, int end) {
        for(int i = begin; i < end; i++){
            double[] row = rhs.getRow(i);
            double elem = this.tri.get(i, subIndex);
            this.substitute(row, elem, subs);
            rhs.setRow(i, row);
        }
    }
    
    /**
     * Substitute into a range of rows after a row solution found, by stream.
     * @param rhs  Right-hand side matrix
     * @param subs  Row solution found
     * @param subIndex  Row solution index
     * @param begin  Begin of row to be substituted
     * @param end   End of row to be substituted
     */
    protected void stream(Matrix rhs, double[] subs, int subIndex, int begin, int end) {
        IntStream.range(begin, end).parallel().forEach((i) -> {
            double[] row = rhs.getRow(i);
            double elem = this.tri.get(i, subIndex);
            this.substitute(row, elem, subs);
            rhs.setRow(i, row);
        });
    }
    
    /**
     * Substitute into a row after row solution found.
     * @param target  Row to be eliminated of row solution found
     * @param coeff  Element on the triangular matrix that is the coefficient
     *               of the row solution found.
     * @param subs   Row solution found
     */
    protected void substitute(double[] target, double coeff, double[] subs) {
        for(int i = 0; i < target.length; i++){
            target[i] -= coeff * subs[i];
        }
    }
    
    /**
     * Obtain a row solution by dividing by a diagonal element.
     * @param rhs  Right-hand side matrix
     * @param rowIndex  Row index of diagonal element, and row solution to be obtained.
     * @return  Row solution
     * @throws  UnsupportedOperationException  
     *          when the diagonal element is too close to zero                             
     */
    protected double[] normalize(Matrix rhs, int rowIndex) {        
        double denom = this.tri.get(rowIndex, rowIndex);
        if(Math.abs(denom) < EPSILON){
            return null;
        }
        double[] row = rhs.getRow(rowIndex);
        for(int i = 0; i < rhs.getColCount(); i++){
            row[i] /= denom;
        }
        return row;
    }

    private Matrix tri;
    private Mode mode;
    
    private static final double EPSILON         = 1e-10;
    private static final int DEFAULT_THRESHOLD  = 8 * 1024;
    
    public enum Mode {
        FORWARD, BACKWARD
    }
}
