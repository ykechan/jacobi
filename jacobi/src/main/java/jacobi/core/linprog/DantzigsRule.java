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

import jacobi.api.Matrix;
import jacobi.core.util.IntArray;
import jacobi.core.util.Real;

/**
 * Implementation of Dantzig's pivoting rule.
 * 
 * Dantzig's rule chooses the largest entry in c^t to be the entering column.
 * 
 * In case of ties, this implementation uses least-subscript rule, which favors the column that
 * represents an upper entry in [x s].
 * 
 * @author Y.K. Chan
 */
public class DantzigsRule implements PivotingRule {

    @Override
    public int[] apply(Tableau tab, Integer max) {
        int enter = this.find(tab.getMatrix(), tab.getVars());
        return enter < 0 ? PivotingRule.NONE : new int[]{enter};
    }    

    /**
     * Find the pivot using Dantzig's rule
     * @param matrix  Tableau entries
     * @param vars  Variable indices
     * @return  The entering column index, or -1 if fail to find any
     */
    protected int find(Matrix matrix, IntArray vars) {
        double min = 0.0;
        int target = -1;
        for(int i = 1; i < matrix.getColCount(); i++){
            double coeff = matrix.get(0, i);
            if(coeff < min
            || (target >= 0 && Real.isNegl(coeff - min) && vars.get(i) < vars.get(target)) ){
                target = i;
                min = coeff;
            }
        }
        return target;
    }    
        
}
