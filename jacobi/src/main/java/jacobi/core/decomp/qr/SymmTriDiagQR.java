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
package jacobi.core.decomp.qr;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.core.decomp.qr.step.GivensQR;
import jacobi.core.decomp.qr.step.GivensQR.Givens;
import java.util.Arrays;
import java.util.List;

/**
 * QR algorithm for symmetric Hessenberg matrices.
 * 
 * A symmetric Hessenberg matrix is a tri-diagonal matrix, which has only real eigenvalues. Computation is much simpler
 * since it can be represents by only its diagonal elements and sub-diagonal elements, and double-shift is un-necessary
 * since all eigenvalues and  Rayleigh quotient shifts are real.
 * 
 * This class is only for 3x3 or larger symmetric Hessenberg matrices, or else the computation fall-through to 
 * base implementation.
 * 
 * @author Y.K. Chan
 */
public class SymmTriDiagQR implements QRStrategy {

    /**
     * Constructor.
     * @param base   Base implementation to fall through
     */
    public SymmTriDiagQR(QRStrategy base) {
        this.base = base;
        this.givensQR = new GivensQR();
    }

    @Override
    public Matrix compute(Matrix matrix, Matrix partner, boolean fullUpper) {
        if(matrix.getRowCount() < 3){
            return this.base.compute(matrix, partner, fullUpper);
        }
        double[][] diags = this.toTriDiag(matrix);
        if(diags == null){
            return this.base.compute(matrix, partner, fullUpper);
        }        
        return Matrices.diag(diags[0]);
    }
    
    /**
     * Compute an iteration of QR algorithm with diagonal and sub-diagonal element.
     * @param diag  diagonal elements
     * @param subDiag  sub-diagonal elements
     * @param partner  Partner matrix
     * @param begin  Begin index of elements of interest
     * @param end   End index of elements of interest
     */
    protected void compute(double[] diag, double[] subDiag, Matrix partner, int begin, int end) {
        if(end - begin < 2){
            return;
        }
        if(Math.abs(subDiag[begin]) < EPSILON){
            this.compute(diag, subDiag, partner, begin + 1, end);
            return;
        }
        if(Math.abs(subDiag[end - 2]) < EPSILON){
            this.compute(diag, subDiag, partner, begin, end - 1);
            return;
        }
        int max = (end - begin) * 16;
        while(--max > 0){
            this.step(diag, subDiag, partner, begin, end);
            int deflated = this.findDeflated(subDiag, begin, end);
            if(deflated < end){
                this.compute(diag, subDiag, partner, begin, deflated + 1);
                this.compute(diag, subDiag, partner, deflated + 1, end);
                return;
            }
        }
        throw new UnsupportedOperationException("Unable to deflate.");
    }
    
    /**
     * Compute an iteration of QR algorithm with diagonal and sub-diagonal element.
     * @param diag  diagonal elements
     * @param subDiag  sub-diagonal elements
     * @param partner  Partner matrix
     * @param begin  Begin index of elements of interest
     * @param end   End index of elements of interest
     */
    protected void step(double[] diag, double[] subDiag, Matrix partner, int begin, int end) {
        if(end - begin < 2){
            return;
        }
        double shift = this.preCompute(diag, subDiag, begin, end);
        List<GivensQR.Givens> rot = this.qrDecomp(diag, subDiag, begin, end - 1);
        this.computeRQ(diag, subDiag, begin, end - 1, rot);
        this.postCompute(diag, subDiag, begin, end, shift);
    }
    
    /**
     * Pre-compute operation for shifting diagonal elements.
     * @param diag  Diagonal elements
     * @param subDiag  Sub-diagonal elements
     * @param begin  Begin index of elements of interest
     * @param end   End index of elements of interest
     * @return Shift value
     */
    protected double preCompute(double[] diag, double[] subDiag, int begin, int end) { 
        double shift = end - begin == 2 ? this.eigenvalue(diag, subDiag, begin) : diag[end - 1];
        for(int i = begin; i < end; i++){
            diag[i] -= shift;
        }
        return shift;
    }
    
    /**
     * Post-compute operation for un-shifting diagonal elements.
     * @param diag  Diagonal elements
     * @param subDiag  Sub-diagonal elements
     * @param begin  Begin index of elements of interest
     * @param end   End index of elements of interest
     * @param shift  Shift value
     */
    protected void postCompute(double[] diag, double[] subDiag, int begin, int end, double shift) {
        for(int i = begin; i < end; i++){
            diag[i] += shift;
        }
    }
    
    /**
     * Compute QR decomposition on the tri-diagonal matrix.
     * @param diag  Diagonal elements
     * @param subDiag  Sub-diagonal elements
     * @param begin  Begin index of elements of interest
     * @param end   End index of elements of interest
     * @return  List of Givens rotation applied
     */
    protected List<Givens> qrDecomp(double[] diag, double[] subDiag, int begin, int end) {
        GivensQR.Givens[] rot = new GivensQR.Givens[end - begin];
        double up = subDiag[begin];
        for(int i = begin; i < end; i++){
            GivensQR.Givens giv = this.givensQR.of(diag[i], subDiag[i]);
            diag[i] = giv.getMag();            
            double upper = giv.rotateX(up, diag[i + 1]);
            double lower = giv.rotateY(up, diag[i + 1]);
            subDiag[i] = upper;
            diag[i + 1] = lower;            
            up = giv.rotateY(0.0, subDiag[i + 1]);
            rot[i - begin] = giv;
        }        
        return Arrays.asList(rot);
    }
    
    /**
     * Compute R*Q from QR decomposition.
     * @param diag  Diagonal elements
     * @param subDiag  Sub-diagonal elements
     * @param begin  Begin index of elements of interest
     * @param end   End index of elements of interest
     * @param rot  List of Givens rotation applied
     */
    protected void computeRQ(double[] diag, double[] subDiag, int begin, int end, List<GivensQR.Givens> rot) {         
        for(int i = begin; i < end; i++){
            GivensQR.Givens giv = rot.get(i - begin);
            diag[i] = giv.transRevRotX(diag[i], subDiag[i]);
            double left = giv.transRevRotX(0.0, diag[i + 1]);
            double right = giv.transRevRotY(0.0, diag[i + 1]);
            subDiag[i] = left;
            diag[i + 1] = right;
        }
    }    
    
    /**
     * Transform input matrix to symmetric tri-diagonal elements, if matrix is symmetric and at least 3x3.
     * @param matrix  Input matrix
     * @return  Diagonal elements and sub-diagonal elements, or null if matrix is not symmetric or too small.
     */
    protected double[][] toTriDiag(Matrix matrix) {
        for(int i = 0; i < matrix.getRowCount(); i++){
            double[] row = matrix.getRow(i);
            boolean nonZero = Arrays.stream(row).skip(i + 2)
                    .filter((elem) -> Math.abs(elem) > 1e-14 )
                    .findAny()
                    .isPresent();
            if(nonZero){
                return null;
            }
        }
        double[][] diags = new double[2][matrix.getRowCount()];
        double upper = 0.0;
        for(int i = 0; i < matrix.getRowCount(); i++){
            double[] row = matrix.getRow(i);
            diags[0][i] = row[i];
            if(i > 0 && Math.abs(row[i - 1] - upper) > EPSILON){
                return null;
            }
            upper = i + 1 < matrix.getColCount() ? row[i + 1] : 0.0; 
            diags[1][i] = upper;
        }
        return diags;
    }
    
    /**
     * Compute the eigenvalue for 2x2 symmetric matrix.
     * @param diag  Diagonal elements
     * @param subDiag  Sub-diagonal elements
     * @param at  Index of elements of interest
     * @return  One real eigenvalue
     */
    protected double eigenvalue(double[] diag, double[] subDiag, int at) {
        //
        //     [a c]
        // A = [   ]
        //     [c b]
        //
        // p(A) = (a - k) * (b - k) - c^2
        //      = k^2 - (a + b)*k + a*b - c^2
        //  det = (a + b)^2 - 4(a*b - c^2)
        //      = (a - b)^2 + 4c^2
        //
        double b = diag[at + 1];
        double c = subDiag[at];
        
        if(Math.abs(c) < 1e-14){
            return b;
        }        
        double a = diag[at];
        System.out.println("tr = " + (a + b));
        System.out.println("det = " + ((a - b) * (a - b) + 4*c*c));
        return (a + b + Math.sqrt((a - b) * (a - b) + 4*c*c));
    }
    
    /**
     * Find the first deflated element in sub-diagonal
     * @param subDiag  Sub-diagonal
     * @param begin  Begin index of elements of interest
     * @param end  End index of elements of interest
     * @return  First deflated element in sub-diagonal
     */
    protected int findDeflated(double[] subDiag, int begin, int end) {
        for(int i = begin; i < end; i++){
            if(Math.abs(subDiag[i]) < EPSILON){
                return i;
            }            
        }
        return end;
    }

    private QRStrategy base;
    private GivensQR givensQR;
    
    private static final double EPSILON = 1e-14;    
}
