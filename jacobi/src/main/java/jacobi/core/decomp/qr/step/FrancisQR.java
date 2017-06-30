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
package jacobi.core.decomp.qr.step;

import jacobi.api.Matrix;
import jacobi.core.decomp.qr.step.shifts.BulgeMover;
import jacobi.core.decomp.qr.step.shifts.DoubleShift;
import jacobi.core.givens.Givens;
import jacobi.core.givens.GivensBatchRQ;
import jacobi.core.givens.GivensMode;
import jacobi.core.givens.GivensPair;
import jacobi.core.util.Real;
import java.util.List;

/**
 * Implementation of Francis double-shifted QR algorithm.
 * 
 * <p>Wilkinson shifts ensures convergence albeit having slightly lower convergence
 * rate. The biggest problem is that eigenvalues may not be real, which involve
 * entire layer of complexity.</p>
 * 
 * <p>However if a complex value z is an eigenvalue of A, z', its complex conjugate
 * must also be an eigenvalue of A since eigenvalues are roots of characteristic
 * polynomials.</p>
 * 
 * <p>
 * Consider
 * <pre>
 * H0 - zI = Q0 * R0
 *       H1 = R0 * Q0 + zI
 * H1 - z'I = Q1 * R1
 *       H2 = R1 * Q1 + z'I
 * 
 * R0 * Q0 + zI - z'I = Q1 * R1
 * Q0*R0*Q0*R0 + (z - z')*Q0*R0 = Q0*Q1*R1*R0
 * (H0 - zI)^2 + (z - z')*(H0 - zI) = Q0*Q1*R1*R0
 * H0^2 - 2*z*H0 + z^2 + (z - z')*H0 - z(z - z')I = Q0*Q1*R1*R0
 * H0^2 - (z + z')*H0 + z*z'I = Q0*Q1*R1*R0
 * H0^2 - 2*Re(z)*H0 + |z|*I = Q0*Q1*R1*R0
 * </pre>
 * </p>
 * 
 * <p>
 * Therefore computing H2 = Q0^t*Q1^t*H*Q1*Q0 is equivalent to compute
 * QR decomposition on (H0 - zI)*(H1 - z'I). By implicit-Q theorem, Hessenberg
 * form is unique save for signs. Avoid computing the whole matrix, only Q
 * for reducing the first column of (H0 - zI)*(H1 - z'I) is computed and applied
 * to H, and reducing Q*H*Q (Q = Q^t for Householder) to Hessenberg form
 * will yields H2.
 * </p>
 * 
 * <p>Francis QR step fails when matrix is 2x2 or below.</p>
 * 
 * @author Y.K. Chan
 */
public class FrancisQR implements QRStep {

    /**
     * Constructor.
     * @param base  Base implementation for fall through.
     */
    public FrancisQR(QRStep base) {
        this.base = base;
    }

    @Override
    public int compute(Matrix matrix, Matrix partner, int beginRow, int endRow, boolean fullUpper) {
        if(endRow - beginRow < 4){
            return this.base.compute(matrix, partner, beginRow, endRow, fullUpper);
        }
        GivensPair giv = this.createBulge(matrix, beginRow, endRow, fullUpper);
        List<GivensPair> rotList = new BulgeMover(beginRow, endRow - 3, endRow, fullUpper).compute(matrix, () -> {});
        Givens last = this.chaseOff(matrix, endRow, fullUpper);
        GivensBatchRQ batRq = new GivensBatchRQ(giv, rotList, last);
        double off = batRq.compute(matrix, beginRow, endRow, fullUpper ? GivensMode.UPPER : GivensMode.DEFLATE);
        if(partner != null){
            batRq.compute(partner, beginRow, endRow, GivensMode.FULL);
        }
        return Real.isNegl(off) 
                ? endRow - 1 
                : Real.isNegl(last.getMag())
                    ? endRow - 2
                    : this.getDeflated(rotList, beginRow, endRow); 
    }
    
    /**
     * Create initial bulge by computing Q*H*Q.
     * @param matrix  Input matrix H
     * @param begin  Begin index of rows of interest
     * @param end  End index of rows of interest
     * @param full   True if full upper triangular matrix needed, false otherwise
     * @return  The Givens rotation pair applied
     */
    protected GivensPair createBulge(Matrix matrix, int begin, int end, boolean full) {
        int endCol = full ? matrix.getColCount() : end;
        GivensPair giv = DoubleShift.of(matrix, end - 2).getImplicitG(matrix, begin);
        double[] upper = matrix.getRow(begin);
        double[] mid = matrix.getRow(begin + 1);
        double[] lower = matrix.getRow(begin + 2);
        double[] bottom = matrix.getRow(begin + 3);
        
        giv.applyLeft(upper, mid, lower, begin, endCol)
                .applyRight(upper, begin)
                .applyRight(mid, begin)
                .applyRight(lower, begin)
                .applyRight(bottom, begin);
        matrix.setRow(begin, upper).setRow(begin + 1, mid).setRow(begin + 2, lower).setRow(begin + 3, bottom);
        
        return giv;
    }
    
    /**
     * Chase off the bulge to return to Hessenberg form.
     * @param matrix  Input matrix A
     * @param end  End of rows of interest
     * @param full  True if full upper triangular matrix needed, false otherwise
     * @return  The Givens rotation applied
     */
    protected Givens chaseOff(Matrix matrix, int end, boolean full) {
        int endCol = full ? matrix.getColCount() : end;
        int begin = end - 3;
        double[] upper = matrix.getRow(end - 2);
        double[] lower = matrix.getRow(end - 1);
        Givens giv = Givens.of(upper[begin], lower[begin]);
        try {
            upper[begin] = giv.getMag();
            lower[begin] = 0.0;
            return giv.applyLeft(upper, lower, begin + 1, endCol);
        } finally {
            matrix.setRow(end - 2, upper).setRow(end - 1, lower);
        }
    }
    
    /**
     * Find the row index of deflated off-diagonal value.
     * @param rotList  List of Givens rotation applied
     * @param beginRow  Begin index of rows of interest
     * @param endRow  End index of rows of interest
     * @return  Row index of deflated off-diagonal value.
     */
    protected int getDeflated(List<GivensPair> rotList, int beginRow, int endRow) {
        for(int i = 0; i < rotList.size(); i++){ 
            if(Real.isNegl(rotList.get(i).getAnchor())){
                return beginRow + i + 1;
            }
        }
        return -1;
    }
    
    private QRStep base;
}
