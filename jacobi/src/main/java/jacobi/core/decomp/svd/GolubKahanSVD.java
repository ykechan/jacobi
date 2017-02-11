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

package jacobi.core.decomp.svd;

import jacobi.api.Matrix;
import jacobi.core.givens.Givens;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of Golub-Kahan SVD step.
 * 
 * Given a bi-diagonal matrix B, for SVD i.e. B = U*E*V where U and V are orthogonal and E diagonal,
 * consider B^t*B = (V^t*E*U^t)*(U*E*V) = V^t*E^2*V which is essentially the eigenvalue decomposition
 * of B^t*B. Similar argument can be made with B*B^t and U. Therefore QR algorithm on B^t*B can be used.
 * 
 * Since B^t*B is symmetric, an analogy of QR algorithm for symmetric tri-diagonal matrix can be employed.
 * 
 * Moreover, it is not necessary to compute B^t*B. Like in the Francis QR double-shift step, by Implicit-Q
 * theorem, bi-diagonal form is unique up to signs. A first Givens rotation G to QR decompose B^t*B is computed,
 * and B*G^t is computed. A bulge is created which breaks the bi-diagonal form, and then subsequently by applying
 * Givens rotation left and right the bulge is pushed and eliminated.
 * 
 * @author Y.K. Chan
 */
public class GolubKahanSVD implements SvdStep {

    @Override
    public int compute(double[] diag, double[] supDiag, int begin, int end, Matrix uMat, Matrix vMat) { 
        if(end - begin < 2){
            return begin;
        }
        double shift = this.wilkinson(diag, supDiag, begin, end);
        Step step = this.createBulge(diag, supDiag, begin, shift); 
        List<Step> left = new ArrayList<>();
        List<Step> right = new ArrayList<>();
        int last = end - 2;
        int deflated = -1;
        for(int i = begin; i < last; i++){
            Step leftStep = this.pushRight(diag, supDiag, i, step.bulge);
            Step rightStep = this.pushDown(diag, supDiag, i, leftStep.bulge);
            left.add(leftStep);
            right.add(rightStep);
            step = rightStep;
            if(rightStep.givens.getMag() < SvdStep.EPSILON){
                deflated = i;
            }
        }
        left.add(this.pushRight(diag, supDiag, last, step.bulge));
        if(uMat != null){
            // ...
        }
        if(vMat != null){
            // ...
        }
        return Math.abs(supDiag[last + 1]) < SvdStep.EPSILON ? end - 1 : deflated; 
    }
    
    protected Step createBulge(double[] diag, double[] supDiag, int at, double shift) {
        Givens giv = Givens.of(diag[at] * diag[at] - shift, diag[at] * supDiag[at]);
        double x = giv.rotateX(diag[at], supDiag[at]);
        double y = giv.rotateY(diag[at], supDiag[at]);
        diag[at] = x; supDiag[at] = y;
        double bulge = giv.rotateX(0.0, diag[at + 1]);
        diag[at + 1] = giv.rotateY(0.0, diag[at + 1]);
        return new Step(giv, bulge);
    }
    
    protected Step pushRight(double[] diag, double[] supDiag, int at, double bulge) {
        Givens giv = Givens.of(diag[at], bulge);
        diag[at] = giv.getMag();
        double x = giv.rotateX(supDiag[at], diag[at + 1]);
        double y = giv.rotateY(supDiag[at], diag[at + 1]);
        supDiag[at] = x; diag[at + 1] = y;
        double next = giv.rotateX(0.0, supDiag[at + 1]);
        supDiag[at + 1] = giv.rotateY(0.0, supDiag[at + 1]);
        return new Step(giv, next);
    }
    
    protected Step pushDown(double[] diag, double[] supDiag, int at, double bulge) {
        Givens giv = Givens.of(supDiag[at], bulge);
        supDiag[at] = giv.getMag();
        double x = giv.rotateX(diag[at + 1], supDiag[at + 1]);
        double y = giv.rotateY(diag[at + 1], supDiag[at + 1]);
        diag[at + 1] = x; supDiag[at + 1] = y;
        double next = giv.rotateX(0.0, diag[at + 2]); 
        diag[at + 2] = giv.rotateY(0.0, diag[at + 2]);
        return new Step(giv, next);
    }
    
    protected double wilkinson(double[] diag, double[] supDiag, int begin, int end) {
        double c = diag[end - 1] * diag[end - 1] + supDiag[end - 2] * supDiag[end - 2];
        double b = diag[end - 2] * supDiag[end - 2];
        double a = diag[end - 2] * diag[end - 2] + (end - 3 < begin ? 0.0 : supDiag[end - 3] * supDiag[end - 3]);
        
        double tr = a + c;
        double delta = (a - c)*(a - c) + 4*b*b;
        return (tr + (tr > 2*c ? -1 : 1) * Math.sqrt(delta) )/2.0;
    }

    protected static final class Step {
        
        public final Givens givens;
        
        public final double bulge;

        public Step(Givens givens, double bulge) {
            this.givens = givens;
            this.bulge = bulge;
        }
        
    }
}
