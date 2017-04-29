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

package jacobi.core.linprog;

import jacobi.core.util.Real;

/**
 * Implementation of Dantzig's pivoting rule. 
 * 
 * This is proposed by Dantzig in his original simplex algorithm: uses the index i where c[i] is largest positive.
 * 
 * The benefit of this approach is its speed: it requires checking the column vector c only. To maintain this
 * advantage, this class provides only 1 entering variable only, unless no choice fits.
 * 
 * @author Y.K. Chan
 */
public class DantzigsRule implements PivotingRule {

    @Override
    public int[] apply(Tableau tab, Integer limit) {
        double[] coeff = tab.getCoeff();
        int[] vars = tab.getVars();
        double max = 0.0;
        int index = -1;
        for(int i = 0; i < coeff.length; i++){
            if(coeff[i] < 0.0){
                continue;
            }
            if(index < 0 || (Real.isNegl(coeff[i] - max) && vars[i] < vars[index])){
                max = coeff[i];
                index = i;
            }else if(coeff[i] > max){
                max = coeff[i];
                index = i;
            }            
        }
        return index < 0 ? PivotingRule.NONE : new int[]{index};
    }

}
