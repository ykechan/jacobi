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

package jacobi.core.decomp.qr;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.api.annotations.Delegate;
import jacobi.api.annotations.NonPerturbative;
import jacobi.api.ext.Op;
import jacobi.api.ext.Prop;
import jacobi.core.impl.ColumnVector;
import jacobi.core.impl.CopyOnWriteMatrix;
import jacobi.core.impl.ImmutableMatrix;
import jacobi.core.util.Throw;
import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * 
 * This class represents a Householder Reflection matrix.
 * 
 * Householder reflection matrix of a vector v is H = I - 2*v*v^t. It is 
 * commonly used in QR decomposition, or related decompositions.
 * 
 * Instead of a standard full-multiplication which takes O(n^3) time, computing
 * H*A only takes O(n^2) time by taking advantage of the properties of H.
 * 
 * H * A = (I - 2*v*v^t) * A = A - 2*v*(v^t * A)
 * 
 * v^t is a row vector, therefore v^t * A is also a row vector. All operations
 * takes O(n^2) time and as a whole also takes O(n^2) time.
 * 
 * Computing row vector w = v^t * A is equivalent to multiply v^t with
 * each column in A. However column access to most matrix implementations take
 * a lot of memory miss, since elements usually are stored in a row-by-row manner.
 * Instead, compute k * u first, where k is an element in v^t, and u is a row
 * in A, for each k and u, and summing all the results will yield the row
 * vector w. In this case, the only memory misses would be access of k, which is far
 * less than O(n^2) miss previously. 
 * 
 * @author Y.K. Chan
 */
public class HouseholderReflector extends ImmutableMatrix {

    /**
     * Constructor given column vector v.  Vector is shallow copied since this
     * object is supposed to be temporary.
     * @param vector  vector v
     * @param from   start of element of interest. Elements before this are
     *               assumed to be zero.
     */
    public HouseholderReflector(double[] vector, int from) { // NOPMD - controlled usage
        this.from = from;        
        this.vector = vector;
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
        if(index < this.from){
            double[] row = new double[this.getColCount()];
            Arrays.fill(row, 0.0);
            row[index] = 1.0;
            return row;
        }
        double[] row = new double[this.getColCount()];
        Arrays.fill(row, 0, this.from, 0.0);
        double k = this.vector[index];
        for(int i = from; i < row.length; i++){
            row[i] = - 2.0 * k * this.vector[i];
        }
        row[index] += 1.0;
        return row;
    }

    @Override
    public Matrix copy() {
        return CopyOnWriteMatrix.of(
            new HouseholderReflector(
                Arrays.copyOf(vector, vector.length), 
                from
            )
        );
    }
    
    /**
     * Normalize a vector to reflector vector
     * @return  First element after reflection
     */
    public double normalize() {
        double temp = 0.0;
        for(int i = from + 1; i < vector.length; i++){
            temp += vector[i] * vector[i];
        }
        if(temp < EPSILON){
            return 0.0;
        }
        double norm = Math.sqrt(temp + vector[from] * vector[from]);
        if(norm < EPSILON){
            return 0.0;
        }
        if(vector[from] < 0.0){
            vector[from] -= norm;
        }else{
            vector[from] += norm;
            norm *= -1.0;
        }
        double newNorm = Math.sqrt(temp + vector[from] * vector[from]);
        
        for(int i = from; i < vector.length; i++){
            vector[i] /= newNorm;
        }
        return norm;
    }
    
    /**
     * Transpose of a Householder Reflector, which is a copy of itself since
     * this matrix is symmetric.
     * @return  Transpose of this matrix
     */
    @NonPerturbative
    @Delegate(facade = Prop.class, method = "transpose")
    public Matrix transpose() {
        return this.copy();
    }
    
    /**
     * Inverse of a Householder Reflector, which is a copy of itself since
     * this matrix is orthogonal.
     * @return  Transpose of this matrix
     */
    @NonPerturbative
    @Delegate(facade = Prop.class, method = "inv")
    public Matrix inv() {
        return this.copy();
    }
    
    /**
     * Multiplication with another matrix A, yielding H * A. This method
     * does not change the value of H and/or A.
     * @param matrix  Matrix A
     * @return   H * A
     */
    @NonPerturbative
    @Delegate(facade = Op.class, method = "mul")
    public Matrix mul(Matrix matrix) {
        Matrix clone = Matrices.copy(matrix);
        this.applyLeft(clone);
        return clone;
    }
    
    /**
     * Transform matrix A to H * A. This method changes the value of A.
     * @param matrix   Matrix A
     */
    public void applyLeft(Matrix matrix) { 
        if(matrix instanceof ColumnVector){
            this.applyLeft((ColumnVector) matrix);
            return;
        }
        this.applyLeft(matrix, 0);
    }
    
    /**
     * Transform matrix A to H * A, interested only in columns beyond a certain 
     * index. Useful when entries are known to be zero. This method changes
     * the value of A
     * @param matrix  Matrix A
     * @param startCol  Start index of columns of interest
     */
    public void applyLeft(Matrix matrix, int startCol) {
        double[] partial = this.partialApply(matrix, startCol);
        for(int i = from; i < matrix.getRowCount(); i++){
            double[] row = matrix.getRow(i);
            double k = this.vector[i];
            for(int j = startCol; j < matrix.getColCount(); j++){
                row[j] -= 2.0 * k * partial[j];
            }
            matrix.setRow(i, row);
        }
    }
    
    /**
     * Transform matrix A to A * H. This method changes the value of A.
     * @param matrix  Matrix A
     */
    public void applyRight(Matrix matrix) {
        int len = this.vector.length - this.from;
        if(matrix.getRowCount() * len >= DEFAULT_THRESHOLD){
            IntStream.range(0, matrix.getRowCount())
                .parallel()
                .forEach((i) -> this.applyRight(matrix, i));
        }else{
            for(int i = 0; i < matrix.getRowCount(); i++){
                this.applyRight(matrix, i);
            }
        }
    }
    
    /**
     * Transform vector v to H * v. This method changes the value of v.
     * @param col  vector v
     */
    protected void applyLeft(ColumnVector col) {
        Throw.when().isTrue(
            () -> this.vector.length != col.getRowCount(), 
            () -> "Dimension mismatch.");
        double sum = 0.0;
        for(int i = from; i < this.vector.length; i++){
            sum += this.vector[i] * col.get(i, 0);
        }
        for(int i = from; i < this.vector.length; i++){
            col.set(i, 0, col.get(i, 0) - 2.0 * sum * this.vector[i]);
        }
    }
    
    protected void applyRight(Matrix matrix, int i) {
        double[] row = matrix.getRow(i);
        double sum = 0.0;
        for(int k = from; k < this.vector.length; k++){
            sum += this.vector[k] * row[k];
        }
        for(int k = from; k < this.vector.length; k++){
            row[k] -= 2.0 * sum * this.vector[k];
        }
        matrix.setRow(i, row);
    }
    
    protected double[] partialApply(Matrix matrix, int startCol) {
        int len = matrix.getColCount() - startCol;
        if((matrix.getRowCount() - from) * len < DEFAULT_THRESHOLD){
            double[] sum = new double[matrix.getColCount()];
            for(int i = from; i < this.vector.length; i++){
                double k = this.vector[i];
                double[] row = matrix.getRow(i);
                for(int j = startCol; j < row.length; j++){
                    sum[j] += k * row[j];
                }
            }
            return sum;
        }
        return IntStream.range(from, matrix.getRowCount())
                .mapToObj((i) -> {
                    double k = this.vector[i];
                    double[] sum = new double[matrix.getColCount()];
                    double[] row = matrix.getRow(i);
                    for(int j = startCol; j < row.length; j++){
                        sum[j] += k * row[j];
                    }
                    return sum;
                })
                .reduce((x, y) -> {
                    for(int j = startCol; j < x.length; j++){
                        x[j] += y[j];
                    }
                    return x;
                })
                .orElseThrow(() -> new IllegalStateException());
    }

    private int from;
    private double[] vector;
    
    private static final int DEFAULT_THRESHOLD = 8 * 1024;
    private static final double EPSILON = 1e-10;
}
