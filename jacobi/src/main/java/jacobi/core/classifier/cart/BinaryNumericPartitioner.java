/* 
 * The MIT License
 *
 * Copyright 2019 Y.K. Chan
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
package jacobi.core.classifier.cart;

import java.util.List;

import jacobi.core.classifier.cart.data.Column;
import jacobi.core.util.Weighted;

/**
 * Partition a data set into two parts based on a numeric attribute.
 * 
 * <p>Each break points of contiguous values of outcome value in the sorted
 * order of the numeric attribute can be employed as a split point to partition 
 * the data set into two parts. A sliding distribution count can be found to 
 * update the distribution as the break point moving in the sorted order to
 * find the break point with lowest measure of impurity.</p>
 * 
 * @author Y.K. Chan
 *
 */
public class BinaryNumericPartitioner implements Partitioner<Integer> {
    
    /**
     * Constructor.
     * @param impurity  Function to the measure the impurity
     */
    public BinaryNumericPartitioner(Impurity impurity) {
        this.impurity = impurity;
    }

    @Override
    public Weighted<Integer> partition(Column<?> target, Column<?> goal, List<Instance> instances) {
        double[] left = new double[goal.cardinality()];
        double[] right = this.weightedSum(goal, instances);
        
        double min = Double.MAX_VALUE;
        int argMin = -1;
        
        int prev = instances.get(0).outcome;        
        for(int i = 0; i < instances.size(); i++) {
            Instance inst = instances.get(i);
            if(inst.outcome != prev){
                // measure impurity
                double measure = this.impurity.of(left) + this.impurity.of(right);
                if(measure < min) {
                    min = measure;
                    argMin = i;
                }
            } 
            left[inst.outcome] += inst.weight;
            right[inst.outcome] -= inst.weight;
            prev = inst.outcome;
        }
        return new Weighted<>(argMin, min);
    }
    
    /**
     * Compute the sum of weights for each distinct value of outcome
     * @param goal  Outcome column
     * @param instances  List of instances in the data set
     * @return  Sum of weights for each distinct value of outcome
     */
    protected double[] weightedSum(Column<?> goal, List<Instance> instances) {
        double[] sum = new double[goal.cardinality()];
        for(Instance inst : instances) {
            sum[inst.outcome] += inst.weight;
        }
        return sum;
    }

    private Impurity impurity;
}
