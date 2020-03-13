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
import jacobi.core.util.MinHeap;
import jacobi.core.util.Weighted;
import java.util.Arrays;

/**
 * Implementation of the largest increment pivoting rule.
 * 
 * <p>While Dantzig's Rule chooses the largest entry in the objective coefficient c, the magnitude of the increment of
 * the target value in bounded by the exit variable, which the largest entry in c may not contribute to the largest
 * increment of the target value.</p>
 * 
 * <p>Largest increment rule uses the actual target value increment as the criteria of selection, which involves both
 * the coefficient magnitude and bound of the leaving variable.</p>
 * 
 * <p>This rule requires O(n^2) time to find the best entering variable, and would be more efficient to be used with a
 * larger entering variable pool.</p>
 * 
 * @author Y.K. Chan
 */
public class LargestIncrementRule implements PivotingRule {

    @Override
    public int[] apply(Tableau tab, Integer max) {
        double[] delta = this.compute(tab.getMatrix(), tab.getCoeff());        
        int[] vars = tab.getVars();
        return Arrays.stream(this.select(delta, max)).map((i) -> vars[i]).toArray();
    }

    /**
     * Find the increment value of each entering variable candidate.
     * @param mat  Constraint matrix part of the tableau
     * @param coeff  Coefficient for objective function
     * @return  Increment value of each entering variable candidate
     */
    protected double[] compute(Matrix mat, double[] coeff) {        
        int last = mat.getColCount() - 1;
        double[] bounds = new double[last]; 
        Arrays.fill(bounds, Double.NaN);
        
        for(int i = 0; i < mat.getRowCount(); i++){
            this.merge(coeff, mat.getRow(i), bounds);
        }
        
        for(int i = 0; i < bounds.length; i++){
            bounds[i] = Double.isNaN(bounds[i]) ? coeff[i] < 0.0 ? 0.0 : -1.0 : bounds[i] * coeff[i];
        }
        return bounds;
    }
    
    /**
     * Update the increment of each entering variable candidate.
     * @param coeff  Objective coefficient
     * @param row  Row of the tableau
     * @param bounds  Running increment of entering variable candidates
     * @return  Delta
     */
    protected double[] merge(double[] coeff, double[] row, double[] bounds) {
        int last = row.length - 1;
        for(int i = 0; i < bounds.length; i++){
            if(coeff[i] < 0.0 || row[i] < 0.0){
                continue;
            }
            double bound = row[last] / row[i];
            bounds[i] = Double.isNaN(bounds[i]) ? bound : Math.min(bounds[i], bound);
        }
        return bounds;
    }
    
    /**
     * Select the top k elements givens its weights. A weight of zero means this element is un-fit to be selected.
     * A negative weight indicates this element contains an error and will be returned isolated.
     * @param weights  Weights of the elements
     * @param k  Number of elements wanted
     * @return  Indices of the elements in descending order
     */
    protected int[] select(double[] weights, int k) {
    	MinHeap heap = new MinHeap(k, 0);
    	 for(int i = 0; i < weights.length; i++){            
             if(weights[i] == 0.0){
                 continue;
             }
             
             if(weights[i] < 0.0){
                 return new int[]{i};
             }
             
             heap.push(i, weights[i]);
         }
    	 
         int[] array = new int[heap.size()];
         for(int i = array.length - 1; i >= 0; i--){
             array[i] = heap.pop().item;
         }
         return array;
    }
    
}
