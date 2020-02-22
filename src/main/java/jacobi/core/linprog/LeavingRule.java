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
import java.util.function.ToIntBiFunction;

/**
 * Implementation of the rule of choosing the leaving variable.
 * 
 * <p>Given the following tableau</p>
 * <pre>
 * [  A   I ][ x ]   [ b ]
 * [        ][   ] = [   ]
 * [ c^t  0 ][ s ]   [ z ]
 * </pre>
 * 
 * <p>And the trivial solution [x s] = [0 b] is feasible.</p>
 * 
 * <p>
 * Given k where x[k] is an enter variable, i.e.&nbsp;x[k] &lt; d for some value d.
 * To maintain the system of equations, s[i] &lt; s[i] - A[i, k] * d for all row index i.
 * </p>
 * 
 * <p>
 * Since s &gt;= 0, b[i] - A[i, k] * d &gt;= 0, therefore d &lt; b[i] / A[i, k].
 * </p>
 * 
 * <p>
 * This class finds the row index that gives the lower bound of d.
 * </p>
 * 
 * @author Y.K. Chan
 */
public class LeavingRule implements ToIntBiFunction<Tableau, Integer> {

    @Override
    public int applyAsInt(Tableau tab, Integer enter) {
        Matrix matrix = tab.getMatrix();
        int last = matrix.getColCount() - 1;
        int target = -1;
        double min = Double.MAX_VALUE;
        for(int i = 0; i < matrix.getRowCount(); i++){
            double denom = matrix.get(i, enter);
            if(denom < 0.0){
                continue;
            }
            double bound = matrix.get(i, last) / denom;
            if(bound < 0.0){
                throw new UnsupportedOperationException();
            }
            if(bound < min){
                min = bound;
                target = i;
            }
        }
        return target;
    }

}
