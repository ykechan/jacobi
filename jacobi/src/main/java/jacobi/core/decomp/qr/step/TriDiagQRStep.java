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
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Y.K. Chan
 */
public class TriDiagQRStep {

    public TriDiagQRStep() {
        this.givensQR = new GivensQR();
    }
    
    public void compute(double[] diag, double[] subDiag, Matrix partner, int begin, int end) {
        if(end - begin < 2){
            return;
        }
        if(end - begin == 2){
            double[] eig = this.eigenvalues(diag, subDiag, begin);
            diag[begin] = eig[0];
            diag[begin + 1] = eig[1];
            subDiag[begin] = 0.0;
            // ...
            return;
        }
        double shift = this.preCompute(diag, begin, end);
        List<Givens> rot = this.qrDecomp(diag, subDiag, begin, end - 1);
        this.computeRQ(diag, subDiag, begin, end - 1, rot);
        this.postCompute(diag, begin, end, shift);
    }
    
    protected double preCompute(double[] diag, int begin, int end) {
        double shift = diag[end - 1]; 
        for(int i = begin; i < end; i++){
            diag[i] -= shift;
        }
        return shift;
    }
    
    protected void postCompute(double[] diag, int begin, int end, double shift) {
        for(int i = begin; i < end; i++){
            diag[i] += shift;
        }
    }
    
    protected List<Givens> qrDecomp(double[] diag, double[] subDiag, int begin, int end) {
        Givens[] rot = new Givens[end - begin];
        double up = subDiag[begin];
        for(int i = begin; i < end; i++){
            Givens giv = this.givensQR.of(diag[i], subDiag[i]);
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
    
    protected void computeRQ(double[] diag, double[] subDiag, int begin, int end, List<Givens> rot) {         
        for(int i = begin; i < end; i++){
            Givens giv = rot.get(i - begin);
            diag[i] = giv.transRevRotX(diag[i], subDiag[i]);
            double left = giv.transRevRotX(0.0, diag[i + 1]);
            double right = giv.transRevRotY(0.0, diag[i + 1]);
            subDiag[i] = left;
            diag[i + 1] = right;
        }
    }
    
    protected double[] eigenvalues(double[] diag, double[] subDiag, int at) {
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
        double a = diag[at];
        double b = diag[at + 1];
        double c = subDiag[at];
        
        if(Math.abs(c) < 1e-14){
            return new double[]{a, b};
        }
        
        double det = Math.sqrt((a - b) * (a - b) + 4*c*c);
        
        return new double[]{ a + b + det, a + b - det };
    }

    private GivensQR givensQR;
}
