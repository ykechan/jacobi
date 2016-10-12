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

package jacobi.core.solver;

import jacobi.api.Matrix;
import jacobi.core.util.Throw;
import java.util.Arrays;
import java.util.stream.Collectors;
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
