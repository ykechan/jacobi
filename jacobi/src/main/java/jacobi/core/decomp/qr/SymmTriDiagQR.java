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
package jacobi.core.decomp.qr;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.core.givens.Givens;
import jacobi.core.givens.GivensMode;
import jacobi.core.givens.GivensRQ;
import jacobi.core.util.Divider;
import jacobi.core.util.Real;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * QR algorithm for symmetric Hessenberg matrices.
 * 
 * <p>A symmetric Hessenberg matrix is a tri-diagonal matrix, which has only real eigenvalues. Computation is much 
 * simpler since it can be represents by only its diagonal elements and sub-diagonal elements, and double-shift is 
 * un-necessary since all eigenvalues and  Rayleigh quotient shifts are real.</p>
 * 
 * <p>Internally this class keeps the symmetric tri-diagonal elements in B-notation, i.e.&nbsp;
 * {a1, b1, a2, b2, ...} where {a1, a2, ...} = diag(H) and {b1, b2, ...} = supDiag(H)</p>
 * 
 * <p>This class is only for 3x3 or larger symmetric Hessenberg matrices, or else the computation falls-through to 
 * base implementation.</p>
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
    }

    @Override
    public Matrix compute(Matrix matrix, Matrix partner, boolean fullUpper) {
        if(matrix.getRowCount() < 3){
            return this.base.compute(matrix, partner, fullUpper);
        }        
        double[] diag = this.toTriDiag(matrix) 
                .map((diags) -> Divider
                        .repeats((begin, end) -> this.step(diags, partner, begin, end))
                        .visit(0, diags.length / 2)
                        .echo(diags) )
                .map((diags) -> IntStream.range(0, diags.length / 2).mapToDouble((i) -> diags[2*i]).toArray())
                .orElse(null);
        return diag == null ? this.base.compute(matrix, partner, fullUpper) : Matrices.diag(diag);
    }
    
    /**
     * Compute an iteration of QR algorithm on the diagonal and sub-diagonal elements
     * @param diags  Diagonal and sub-diagonal elements in Z-notation
     * @param partner  Partner matrix
     * @param begin  Begin index of elements of interest
     * @param end   End index of elements of interest
     * @return  Index of deflated element or -1 if none
     */
    protected int step(double[] diags, Matrix partner, int begin, int end) {
        if(end - begin < 2){
            return begin + 1;
        }
        if(Real.isNegl(diags[2*begin + 1])){
            return begin + 1;
        }
        double shift = diags[2*(end - 1)];
        List<Givens> rot = this.qrDecomp(diags, begin, end, shift);        
        int split = this.computeRQ(diags, begin, end, rot, shift);
        if(partner != null){
            new GivensRQ(rot).compute(partner, begin, end, GivensMode.FULL);
        }
        return split;
    }    
    
    /**
     * Compute QR decomposition on the tri-diagonal matrix
     * @param diags  Diagonal and sub-diagonal elements.
     * @param begin  Begin index of elements of interest
     * @param end   End index of elements of interest
     * @param shift  Shift value
     * @return  List of Givens rotation applied
     */
    protected List<Givens> qrDecomp(double[] diags, int begin, int end, double shift) {
        Givens[] rot = new Givens[end - begin];
        double up = diags[begin + 1];
        int last = end - 1;
        int finish = 2 * last;
        diags[2 * begin] -= shift;
        for(int i = 2 * begin; i < finish; i += 2){
            Givens giv = Givens.of(diags[i], diags[i + 1]);
            diags[i] = giv.getMag(); 
            double upper = giv.rotateX(up, diags[i + 2] - shift);
            double lower = giv.rotateY(up, diags[i + 2] - shift);
            diags[i + 1] = upper;
            diags[i + 2] = lower;
            up = giv.rotateY(0.0, diags[i + 3]);
            rot[i / 2 - begin] = giv;
        }
        return Arrays.asList(rot);
    }
    
    /**
     * Compute R*Q from QR decomposition.
     * @param diags  Diagonal and sub-diagonal elements.
     * @param begin  Begin index of elements of interest
     * @param end   End index of elements of interest
     * @param rot  Givens rotation applied
     * @param shift  Shift value
     * @return  Index of deflated entry, or -1 if none
     */
    protected int computeRQ(double[] diags, int begin, int end, List<Givens> rot, double shift) {
        int last = end - 1;
        int finish = 2 * last;
        int deflated = -1;
        for(int i = 2 * begin; i < finish; i+=2){
            Givens giv = rot.get(i/2 - begin);
            diags[i] = giv.rotateX(diags[i], diags[i + 1]) + shift;
            double left = giv.rotateX(0.0, diags[i + 2]);
            double right = giv.rotateY(0.0, diags[i + 2]);
            if(Real.isNegl(left)){
                deflated = i / 2;
            }
            diags[i + 1] = left;
            diags[i + 2] = right;
        }
        diags[2 * last] += shift;
        return deflated < 0 ? deflated : deflated + 1;
    }
    
    /**
     * Transform input matrix to symmetric tri-diagonal elements, if matrix is symmetric
     * @param matrix  Input matrix
     * @return   Diagonal and sub-diagonal elements, or empty if matrix is not symmetric
     */
    protected Optional<double[]> toTriDiag(Matrix matrix) {
        for(int i = 0; i < matrix.getRowCount(); i++){
            double[] row = matrix.getRow(i);
            boolean nonZero = Arrays.stream(row).skip(i + 2)                    
                    .filter((elem) -> !Real.isNegl(elem) )
                    .findAny()
                    .isPresent();
            if(nonZero){                
                return Optional.empty();
            }
        }
        double[] diags = new double[2 * matrix.getRowCount()];
        double upper = 0.0;
        for(int i = 0; i < matrix.getRowCount(); i++){
            double[] row = matrix.getRow(i);
            diags[2*i] = row[i];
            if(i > 0 && !Real.isNegl(row[i - 1] - upper)){
                return Optional.empty();
            }
            upper = i + 1 < matrix.getColCount() ? row[i + 1] : 0.0; 
            diags[2*i + 1] = upper;
        }
        return Optional.of(diags);
    }

    private QRStrategy base;
}
