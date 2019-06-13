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

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.core.classifier.cart.data.Column;
import jacobi.core.classifier.cart.data.DataTable;
import jacobi.core.classifier.cart.data.Sequence;
import jacobi.core.util.Weighted;

/**
 * Partitioning a data set by a nominal attribute.
 * 
 * <p>Partitioning a data set by a discrete nominal attribute is trivial: split the
 * data by their nominal value. Thus no additional information is provided.</p>
 * 
 * @author Y.K. Chan
 *
 */
public class NominalPartitioner implements Partitioner<Void> {
    
    /**
     * Constructor.
     * @param impurity  Impurity measurement function
     */
    public NominalPartitioner(Impurity impurity) {
        this.impurity = impurity;
    }

    @Override
    public Weighted<Void> partition(DataTable table, 
            double[] weights, 
            Column<?> col, 
            Sequence seq) {
        if(col.isNumeric()){
            throw new UnsupportedOperationException("Numeric attribute not supported.");
        }
        
        Matrix dist = Matrices.zeros(col.cardinality(), table.getOutcomeColumn().cardinality());
        int[] values = table.nominals(col.getIndex());
        int[] outcomes = table.outcomes();
        
        for(int i = 0; i < seq.length(); i++) {
            int index = seq.indexAt(i);
            double[] row = dist.getRow(values[index]);
            row[outcomes[index]] += weights[index];
            dist.setRow(index, row);
        }
        return new Weighted<>(null, this.weightedImpurity(dist));
    }
    
    /**
     * Find the impurity given the weights of different outcomes 
     * of different nominal values.
     * @param dist  Matrix of weight values
     * @return  Impurity measurement
     */
    protected double weightedImpurity(Matrix dist) {
        double rand = 0.0;
        for(int i = 0; i < dist.getRowCount(); i++) {
            rand += this.impurity.of(dist.getRow(i));
        }
        return rand;
    }

    private Impurity impurity;
}
