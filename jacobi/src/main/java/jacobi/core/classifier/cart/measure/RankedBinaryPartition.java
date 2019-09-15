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
package jacobi.core.classifier.cart.measure;

import java.util.Arrays;
import java.util.List;

import jacobi.api.Matrix;
import jacobi.api.classifier.Column;
import jacobi.api.classifier.DataTable;
import jacobi.api.classifier.Instance;
import jacobi.core.classifier.cart.Sequence;
import jacobi.core.util.Weighted;

/**
 * Implementation of measure the impurity when binary split the outcomes according to a numeric
 * attribute.
 * 
 * <p>This class find the optimal splitting point in which the impurity is lowest.</p>
 * 
 * <p>This class returns the splitting value of the numeric attribute together with
 * the impurity measurement.</p>
 * 
 * @author Y.K. Chan
 *
 */
public class RankedBinaryPartition implements Partition {
    
    /**
     * Constructor.
     * @param impurity  Impurity function
     */
    public RankedBinaryPartition(Impurity impurity) {
        this.impurity = impurity;
    }

    @Override
    public Weighted<double[]> measure(DataTable<?> table, Column<?> target, Sequence seq) {
        List<Instance> instances = seq.apply(table.getInstances(target));
        
        Column<?> goal = table.getOutcomeColumn();
        
        double left = 0.0;
        double[] leftDist = new double[goal.cardinality()];
        
        Weighted<double[]> total = this.totalDist(goal, instances);
        double right = total.weight;
        double[] rightDist = total.item;
        
        int prev = -1;
        double min = Double.MAX_VALUE;
        int at = -1;
        
        System.out.println("Column #" + target.getIndex());
        Matrix numMat = table.getMatrix();
        
        double prevVal = Double.NaN;
        for(int i = 0; i < instances.size(); i++) { 
            Instance inst = instances.get(i);
            double numVal = numMat.get(inst.feature, target.getIndex());
            if(prev < 0){
                prev = inst.outcome;
            } 
            
            if(prev != inst.outcome && numVal > prevVal) {
            	double imp = left * this.impurity.of(leftDist)
                        + right * this.impurity.of(rightDist);
            	
            	System.out.println(
            		"$" + i + ":" 
            		+ Arrays.toString(leftDist) + "(" + this.impurity.of(leftDist) + ")" 
            		+ ":" 
            		+ Arrays.toString(rightDist) + "(" + this.impurity.of(rightDist) + ")"
            		+ ":" + imp);
             
	             if(imp < min) {
	                 min = imp;
	                 at = i;
	             }
	             
	             prev = inst.outcome;
            }
            
            left += inst.weight;
            leftDist[inst.outcome] += inst.weight;
            
            right -= inst.weight;
            rightDist[inst.outcome] -= inst.weight;     
            
            prevVal = numVal;
        }
        
        return at < 0
        	? new Weighted<>(new double[0], Double.NaN)
        	: new Weighted<>(new double[] {
        		this.split(table.getMatrix(), instances, target, at)
        	  }, min);
    }
    
    /**
     * Find the distribution and total weights of given instances
     * @param goal  Column of outcome
     * @param instances  Instances of interest
     * @return  Weight distribution with sum
     */
    protected Weighted<double[]> totalDist(Column<?> goal, List<Instance> instances) {
        double sum = 0.0;
        double[] dist = new double[goal.cardinality()];
        
        for(Instance inst : instances) {
            dist[inst.outcome] += inst.weight;
            sum += inst.weight;
        }
        
        return new Weighted<>(dist, sum);
    }
    
    /**
     * Find the splitting value given the splitting index of given instances
     * @param matrix  Numeric data matrix
     * @param instances  Instances of interest
     * @param target  Splitting column
     * @param at  Splitting index of instances
     * @return  Numeric value of split
     */
    protected double split(Matrix matrix, List<Instance> instances, Column<?> target, int at) {
        int left = instances.get(at - 1).feature;
        int right = instances.get(at).feature;
        
        double[] leftRow = matrix.getRow(left);
        double[] rightRow = matrix.getRow(right);
        
        return (leftRow[target.getIndex()] + rightRow[target.getIndex()]) / 2;
    }

    private Impurity impurity;
}
