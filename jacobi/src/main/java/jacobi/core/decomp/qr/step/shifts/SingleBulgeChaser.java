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

package jacobi.core.decomp.qr.step.shifts;

import jacobi.api.Matrix;
import jacobi.core.givens.Givens;
import java.util.Arrays;
import java.util.function.Consumer;

/**
 * Functionality class for performing bulge-chasing in Francis QR double step.
 * 
 * This class accept a Hessenberg matrix and a double-shift value. A bulge is created in an 
 * otherwise Hessenberg matrix by a pair of Givens rotation. This matrix is almost Hessenberg 
 * except first 4 rows, e.g.
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
public class SingleBulgeChaser {
    
    /**
     * Create a bulge by the given double-shift, and chase it away along the diagonal. Off-bulge elements are left
     * to post-process by batch.
     * @param args  QR step arguments
     * @param shift  Double-shift value
     * @param listener  Invoked when a bulge is created, and chased down a single step.
     * @return  Batch of givens rotation done.
     */
    public Batch compute(Arguments args, DoubleShift shift, Consumer<Integer> listener) {
        int endCol = args.fullUpper ? args.matrix.getColCount() : args.endRow;
        
        GivensPair implicit = this.createBulge(args, shift, () -> listener.accept(args.beginRow));
        
        GivensPair[] rotList = new GivensPair[args.endRow - args.beginRow - 3];
        for(int i = 0; i < rotList.length; i++){
            int atRow = args.beginRow + 1 + i;
            rotList[i] = this.pushBulge(args, atRow, () -> listener.accept(atRow));
        } 
        
        double[] upper = args.matrix.getRow(args.endRow - 2);
        double[] lower = args.matrix.getRow(args.endRow - 1);
        
        Givens giv = Givens.of(upper[args.endRow - 3], lower[args.endRow - 3]);
        upper[args.endRow - 3] = giv.getMag();
        lower[args.endRow - 3] = 0.0;
        giv.applyLeft(upper, lower, args.endRow - 2, endCol)
                //.applyRight(upper, args.endRow - 2)
                .applyRight(lower, args.endRow - 2);
        args.matrix.setRow(args.endRow - 2, upper).setRow(args.endRow - 1, lower);
        
        return new Batch(implicit, Arrays.asList(rotList), giv, lower[args.endRow - 2]);
    }    
    
    /**
     * Create a bulge using a given shift.
     * @param args  QR step arguments
     * @param shift  Double-shift value
     * @param barrier  Invoked before the bottom row of bulge is disturbed
     * @return  The pair of Givens rotation applied
     */
    protected GivensPair createBulge(Arguments args, DoubleShift shift, Runnable barrier) {
        int endCol = args.fullUpper ? args.matrix.getColCount() : args.endRow;
        int at = args.beginRow;
        double[] upper = args.matrix.getRow(at);
        double[] mid = args.matrix.getRow(at + 1);
        double[] lower = args.matrix.getRow(at + 2);
        
        GivensPair giv = shift.getImplicitG(args.matrix, at);
        giv.applyLeft(upper, mid, lower, at, endCol)
                .applyRight(upper, at)
                .applyRight(mid, at)
                .applyRight(lower, at);
        args.matrix.setRow(at, upper).setRow(at + 1, mid).setRow(at + 2, lower);
        
        barrier.run();
        if(at + 3 < args.endRow){
            args.matrix.getAndSet(at + 3, (r) -> giv.applyRight(r, at));
        }
        return giv;
    }
    
    /**
     * Push the bulge a row down.
     * @param args  QR step arguments
     * @param atRow  Row index of the top row of the bulge
     * @param barrier  Invoked before the bulge is pushed down
     * @return  The pair of Givens rotation applied
     */
    protected GivensPair pushBulge(Arguments args, int atRow, Runnable barrier) {
        int endCol = args.fullUpper ? args.matrix.getColCount() : args.endRow;
        
        double[] upper = args.matrix.getRow(atRow);
        double[] mid = args.matrix.getRow(atRow + 1);
        double[] lower = args.matrix.getRow(atRow + 2);
        int atCol = atRow - 1;
        GivensPair applied = GivensPair.of(upper[atCol], mid[atCol], lower[atCol]);
        upper[atCol] = applied.getAnchor();
        mid[atCol] = 0.0;
        lower[atCol] = 0.0;
                
        applied.applyLeft(upper, mid, lower, atRow, endCol)
                .applyRight(upper, atRow)
                .applyRight(mid, atRow)
                .applyRight(lower, atRow);
        args.matrix.setRow(atRow, upper).setRow(atRow + 1, mid).setRow(atRow + 2, lower);
        
        barrier.run();
        if(atRow + 3 < args.endRow){
            args.matrix.getAndSet(atRow + 3, (r) -> applied.applyRight(r, atRow));
        }
        return applied;
    }

    /**
     * Data object for the arguments of QR step
     */
    public static final class Arguments {
        
        /**
         * Input matrix A.
         */
        public final Matrix matrix;        
        
        /**
         * Begin and end index of rows of interest.
         */
        public final int beginRow, endRow;
        
        /**
         * True if full upper triangular matrix is needed, false otherwise.
         */
        public final boolean fullUpper;

        /**
         * Constructor.
         * @param matrix  Input matrix A.
         * @param beginRow  Begin index of rows of interest.
         * @param endRow  End index of rows of interest.
         * @param fullUpper  True if full upper triangular matrix is needed, false otherwise.
         */
        public Arguments(Matrix matrix, int beginRow, int endRow, boolean fullUpper) {
            this.matrix = matrix;
            this.beginRow = beginRow;
            this.endRow = endRow;
            this.fullUpper = fullUpper;
        }
        
    }
}
