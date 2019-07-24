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

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.core.classifier.cart.data.Column;
import jacobi.core.classifier.cart.data.DataTable;
import jacobi.core.classifier.cart.data.Instance;
import jacobi.core.classifier.cart.data.Sequence;
import jacobi.core.util.Weighted;

/**
 * Implementation to measure the impurity when partitioning by a nominal attribute.
 * 
 * <p>Partitioning by a nominal attribute is trivial: partition by the discrete 
 * value of that attribute. Thus no additional information about the partition
 * is provided.</p>
 * 
 * @author Y.K. Chan
 *
 */
public class NominalPartition implements Partition<Void> {

    /**
     * Constructor.
     * @param impurity  Impurity function
     */
    public NominalPartition(Impurity impurity) {
        this.impurity = impurity;
    }

    @Override
    public Weighted<Void> measure(DataTable<?> table, Column<?> target, Sequence seq) {
        List<Instance> instances = seq.apply(table.getInstances(target));
        return new Weighted<>(null, this.measure(target, table.getOutcomeColumn(), instances));
    }
    
    /**
     * Measure the impurity given the feature outcome pairs with weight
     * @param target  Feature column
     * @param goal  Outcome column
     * @param instances  Feature outcome pairs with weight
     * @return  Impurity measurement
     */
    protected double measure(Column<?> target, Column<?> goal, List<Instance> instances) {
        double[] weights = new double[target.cardinality()];
        Matrix dist = Matrices.zeros(target.cardinality(), goal.cardinality());
        
        for(Instance inst : instances){
            weights[inst.feature] += inst.weight;
            double[] row = dist.getRow(inst.feature);
            row[inst.outcome] += inst.weight;
            dist.setRow(inst.feature, row);
        }
        
        return this.measure(dist, weights);
    }
    
    /**
     * Measure the impurity given the distribution matrix and weight of partitions
     * @param dist  Distribution matrix in outcome-by-feature manner
     * @param weights  Weights of partitions
     * @return  Impurity measurement
     */
    protected double measure(Matrix dist, double[] weights) {
        double value = 0.0;
        for(int i = 0; i < weights.length; i++) {
            value += weights[i] * this.impurity.of(dist.getRow(i));
        }
        return value;
    }
    
    private Impurity impurity;
}
