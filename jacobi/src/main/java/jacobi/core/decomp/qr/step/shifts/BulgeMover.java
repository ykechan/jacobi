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

package jacobi.core.decomp.qr.step.shifts;

import jacobi.api.Matrix;
import jacobi.core.givens.GivensPair;
import java.util.Arrays;
import java.util.List;

/**
 * Functionality class for performing bulge-chasing in Francis QR double step.
 * 
 * <p>This class accepts the intermediate produce during Francis QR double step,
 * the a bulge is created in an otherwise Hessenberg matrix. This matrix is
 * almost Hessenberg except first 4 rows, e.g.<p>
 * 
 * <pre>
 *      x x x x x x
 *      x x x x x x
 * A =  x x x x x x
 *      x x x x x x
 *      0 0 0 x x x
 *      0 0 0 0 x x
 * </pre>
 * 
 * <p>
 * Two givens rotation matrix G1 and G2 and be found to reduce the first
 * column of A' = G2*G1*A to Hessenberg, however B = A'*G1^t*G2^t has the bulge pushed,
 * i.e.
 * </p>
 * 
 * <p>
 * <pre>
 *      x x x x x x
 *      x x x x x x
 * B =  0 x x x x x
 *      0 x x x x x
 *      0 x 0 x x x
 *      0 0 0 0 x x
 * </pre>
 * </p>
 * 
 * <p>Repeating the procedure would reduce the final product to Hessenberg form.</p>
 * 
 * <p>Instead of chasing the bulge all the way, this class move the bulge from 
 * one column to another, and returns the Givens rotation applied.</p>
 * 
 * <p>This class is "lazy" in the sense that Givens rotation are applied to the
 * right only along the diagonal region for computing the next Givens rotation
 * to further move the bulge. Computation of other columns are omitted.</p>
 * 
 * @author Y.K. Chan
 */
public class BulgeMover {

    /**
     * Constructor.
     * @param at  Initial position of the bulge
     * @param target  Finial position of the bulge after computation
     * @param endRow  End of rows of interest
     * @param full  True if apply computation to all columns, false otherwise
     */
    public BulgeMover(int at, int target, int endRow, boolean full) {
        this.begin = at;
        this.end = target;
        this.endRow = endRow;
        this.full = full;
    }
    
    /**
     * Move the bulge to final position.
     * @param input  Input matrix
     * @param listener  Invoked before the bulge is pushed down
     * @return  The pair of Givens rotation applied
     */
    public List<GivensPair> compute(Matrix input, Runnable listener) {
        GivensPair[] rotList = new GivensPair[end - begin];
        for(int i = begin; i < end; i++){
            rotList[i - begin] = this.pushBulge(input, i, listener);
        }
        return Arrays.asList(rotList);
    }
    
    /**
     * Push the bulge a row down.
     * @param input  Input matrix
     * @param atCol  Column index of the top row of the bulge
     * @param listener  Invoked before the bulge is pushed down
     * @return  The pair of Givens rotation applied
     */
    protected GivensPair pushBulge(Matrix input, int atCol, Runnable listener) {
        int endCol = full ? input.getColCount() : endRow;
        int atRow = atCol + 1;
        double[] upper = input.getRow(atRow);
        double[] mid = input.getRow(atRow + 1);
        double[] lower = input.getRow(atRow + 2);        
        GivensPair applied = GivensPair.of(upper[atCol], mid[atCol], lower[atCol]);
        upper[atCol] = applied.getAnchor();
        mid[atCol] = 0.0;
        lower[atCol] = 0.0;
                
        applied.applyLeft(upper, mid, lower, atRow, endCol)
                .applyRight(upper, atRow)
                .applyRight(mid, atRow)
                .applyRight(lower, atRow);
        input.setRow(atRow, upper).setRow(atRow + 1, mid).setRow(atRow + 2, lower);
        
        listener.run();
        if(atRow + 3 < this.endRow){
            input.getAndSet(atRow + 3, (r) -> applied.applyRight(r, atRow));
        }
        return applied;
    }

    private int begin, end, endRow;
    private boolean full;
}
