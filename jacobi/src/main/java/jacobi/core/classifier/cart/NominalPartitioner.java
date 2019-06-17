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
import jacobi.core.util.Throw;
import jacobi.core.util.Weighted;

/**
 * Partition a data set based on a nominal feature.
 * 
 * <p>Partitioning on a nominal feature is trivial: split for each of the discrete values.
 * Therefore no extra information about the partition is provided.</p>
 * 
 * @author Y.K. Chan
 *
 */
public class NominalPartitioner implements Partitioner<Void> {
    
    /**
     * Constructor.
     * @param impurity  Function to measure impurity
     */
    public NominalPartitioner(Impurity impurity) {
        this.impurity = impurity;
    }

    @Override
    public Weighted<Void> partition(Column<?> target, Column<?> goal, List<Instance> instances) {
        Throw.when()
            .isTrue(
                () -> target.isNumeric(), 
                () -> "Feature column #" + target.getIndex() + " is not nominal.")
            .isTrue(
                () -> goal.isNumeric(), 
                () -> "Outcome column is not nominal.");
        
        Matrix dist = this.outcomeDist(instances, 
            Matrices.zeros(target.cardinality(), goal.cardinality()));
        
        return new Weighted<>(null, this.weightedImpurity(dist));
    }
    
    /**
     * Create the distribution matrix in the feature by outcome manner
     * @param instances  Instances of the data set
     * @param dist  Input distribution matrix
     * @return  Distribution matrix, instance of dist
     */
    protected Matrix outcomeDist(List<Instance> instances, Matrix dist) {
        for(Instance inst : instances) {
            double[] row = dist.getRow(inst.feature);
            row[inst.outcome] += inst.weight;
            dist.setRow(inst.feature, row);
        }
        return dist;
    }
    
    /**
     * Compute the total impurity measure given a distribution matrix
     * @param dist  Distribution matrix
     * @return  Total impurity measure
     */
    protected double weightedImpurity(Matrix dist) {
        double measure = 0.0;
        for(int i = 0; i < dist.getRowCount(); i++){
            measure += this.impurity.of(dist.getRow(i));
        }
        return measure;
    }

    private Impurity impurity;
}
