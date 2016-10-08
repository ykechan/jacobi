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

package jacobi.core.decomp.qr.step;

import jacobi.api.Matrix;
import jacobi.core.decomp.qr.step.GivensQR.Givens;
import java.util.ArrayList;
import java.util.List;

/**
 * Functionality class for performing bulge-chasing in Francis QR double step.
 * 
 * This class accepts the intermediate produce during Francis QR double step,
 * the a bulge is created in an otherwise Hessenberg matrix. This matrix is
 * almost Hessenberg except first 4 rows, e.g.
 * 
 *      x x x x x x
 *      x x x x x x
 * A =  x x x x x x
 *      x x x x x x
 *      0 0 0 x x x
 *      0 0 0 0 x x
 * 
 * Two givens rotation matrix G1 and G2 and be found to reduce the first
 * column of A' = G2*G1*A to Hessenberg, however B = A'*G1^t*G2^t has the bulge pushed,
 * i.e.
 * 
 *      x x x x x x
 *      x x x x x x
 * B =  0 x x x x x
 *      0 x x x x x
 *      0 x 0 x x x
 *      0 0 0 0 x x
 * 
 * Repeating the procedure would reduce the final product to Hessenberg form.
 * 
 * @author Y.K. Chan
 */
public class BulgeChaser implements QRStep {

    /**
     * Constructor.
     */
    public BulgeChaser() {
        this.givensQR = new GivensQR();
    }    
    
    @Override
    public void compute(Matrix matrix, Matrix partner, int beginRow, int endRow, boolean fullUpper) {
        int endCol = fullUpper ? matrix.getColCount() : endRow;
        int begin = beginRow;
        int end = endRow - 3;
        List<GivensPair> rotList = new ArrayList<>();
        for(int i = begin; i < end; i++){
            rotList.add(this.pushBulge(matrix, i, endCol, endRow));
        }
        double[] upper = matrix.getRow(endRow - 2);
        double[] lower = matrix.getRow(endRow - 1);
        Givens last = this.givensQR.computeQR(upper, lower, endRow - 3, endCol);        
        matrix.setRow(endRow - 2, upper).setRow(endRow - 1, lower);
        
        this.batchRotate(matrix, beginRow, endRow, rotList, last, fullUpper);
    }
    
    /**
     * Push the 4x4 bulge one row lower. This method computes only partially
     * for applying on the right s.t. the next bulge and be determined. The rest
     * are left to be batch processed.
     * @param matrix  Input matrix
     * @param col  Column index of the bulge
     * @param endCol  End of columns of interest
     * @param endRow  End of rows of interest
     * @return A pair of Givens rotation applied
     */
    protected GivensPair pushBulge(Matrix matrix, int col, int endCol, int endRow) {
        double[] r0 = matrix.getRow(col + 1);
        double[] r1 = matrix.getRow(col + 2);
        double[] r2 = matrix.getRow(col + 3);        
        GivensPair giv = this.createGivensPair(r0[col], r1[col], r2[col]); 
        r0[col] = giv.getLower().getMag();
        r1[col] = 0.0;
        r2[col] = 0.0;
        this.givensQR.apply(giv.getUpper(), r0, r1, col + 1, endCol);
        this.givensQR.apply(giv.getLower(), r0, r2, col + 1, endCol); 
        
        this.doubleGivens(r1, col + 1, giv);
        this.doubleGivens(r2, col + 1, giv);
        
        matrix.setRow(col + 1, r0).setRow(col + 2, r1).setRow(col + 3, r2);
        
        if(col + 4 < endRow){
          matrix.getAndSet(col + 4, (r) -> this.doubleGivens(r, col + 1, giv));
        }
        return giv;
    }
    
    /**
     * Apply Givens rotation on the right as a batch.
     * @param matrix  Input matrix
     * @param beginRow  Begin index of rows of interest
     * @param endRow  End index of rows of interest
     * @param rotList  List of pairs Givens rotation
     * @param last  Last Givens rotation
     * @param fullUpper
     */
    protected void batchRotate(Matrix matrix, int beginRow, int endRow, List<GivensPair> rotList, Givens last, boolean fullUpper) {
        int begin = fullUpper ? 0 : beginRow;
        for(int i = begin; i < endRow; i++){
            double[] row = matrix.getRow(i);
            
            this.revGivens(row, endRow - 2, last);
            matrix.setRow(i, row);
        }
    }
    
    /**
     * Apply Givens rotation on the right as a batch to row vector.
     * @param row  Input row vector
     * @param beginRow  Begin index of row of interest
     * @param index  Row index of the input row vector
     * @param rotList   List of Givens rotation
     */
    protected void batchRotate(double[] row, int beginRow, int index, List<GivensPair> rotList) {
        int skip = index < beginRow ? 0 : index - beginRow - 1;
        int begin = beginRow + skip;
        for(int i = skip; i < rotList.size(); i++){
            this.doubleGivens(row, begin + i + 1, rotList.get(i));
        }
    }
    
    /**
     * Apply a pair of Givens rotation i.e. [a b c]*G^t*Q^t.
     * @param row  Row of input matrix
     * @param col  Column index of rotation
     * @param giv  A pair of Givens rotation
     */
    protected void doubleGivens(double[] row, int col, GivensPair giv) {
        double a = row[col];
        double b = row[col + 1];
        double c = row[col + 2];
        
        double x = giv.getUpper().transRevRotX(a, b);
        double y = giv.getUpper().transRevRotY(a, b);
        
        row[col] = giv.getLower().transRevRotX(x, c);
        row[col + 1] = y;
        row[col + 2] = giv.getLower().transRevRotY(x, c);
    }
    
    /**
     * Apply Givens rotation i.e. [a b c]*G^t.
     * @param row  Row of input matrix
     * @param col  Column index of rotation
     * @param giv  Givens rotation
     */
    protected void revGivens(double[] row, int col, Givens giv) {
        double a = row[col];
        double b = row[col + 1];
        
        row[col] = giv.transRevRotX(a, b);
        row[col + 1] = giv.transRevRotY(a, b);
    }
    
    /**
     * Create a pair of Givens rotation s.t. Q*G*[a b c]^t = [r 0 0]^t.
     * @param a  Value a in [a b c]^t
     * @param b  Value b in [a b c]^t
     * @param c  Value c in [a b c]^t
     * @return  A pair of Givens rotation
     */
    protected GivensPair createGivensPair(double a, double b, double c) {
        Givens upper = this.givensQR.of(a, b);
        Givens lower = this.givensQR.of(upper.getMag(), c);
        return new GivensPair(upper, lower);
    }
    
    private GivensQR givensQR; 
    
    /**
     * Data class for a pair of Givens rotation.
     * For 3 elements [a b c]^t, the upper Givens rotation refers to G s.t.
     * G*[a b c]^t = [x 0 c], and the lower Givens rotation refers to H s.t.
     * H*[x 0 c]^t = [y 0 0].
     */
    protected static final class GivensPair {
        
        /**
         * Constructor.
         * @param upper  Givens rotation for eliminating upper row frontal element
         * @param lower  Givens rotation for eliminating lower row frontal element
         */
        public GivensPair(Givens upper, Givens lower) {
            this.upper = upper;
            this.lower = lower;
        }

        /**
         * Get upper Givens rotation.
         * @return  Upper Givens rotation
         */
        public Givens getUpper() {
            return upper;
        }

        /**
         * Get lower Givens rotation.
         * @return  Lower Givens rotation
         */
        public Givens getLower() {
            return lower;
        }
        
        private Givens upper, lower;
    }
}
