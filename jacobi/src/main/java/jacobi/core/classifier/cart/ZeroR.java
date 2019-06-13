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

import java.util.Set;

import jacobi.core.classifier.cart.data.Column;
import jacobi.core.classifier.cart.data.DataTable;
import jacobi.core.classifier.cart.data.Sequence;
import jacobi.core.classifier.cart.node.Decision;
import jacobi.core.classifier.cart.node.DecisionNode;

/**
 * Implementation of the 0-Rule algorithm, which is the simplest case of
 * learning a Decision Tree that does not consider any feature.
 * 
 * <p>Given no associated feature, the best guess is the most frequent 
 * appearing outcome with consideration of weights.</p>
 * 
 * @author Y.K. Chan
 *
 */
public class ZeroR implements DecisionTreeLearner {

    @Override
    public DecisionNode learn(DataTable table, 
            double[] weights, 
            Set<Column<?>> features, 
            Sequence seq) {
        
        double[] dist = this.count(table, weights, seq);
        int est = this.argMax(dist);
        return new Decision(est);
    }
    
    protected double[] count(DataTable table, double[] weights, Sequence seq) {
        double[] dist = new double[table.getOutcomeColumn().cardinality()];
        int[] outcomes = table.outcomes();
        for(int i = 0; i < seq.length(); i++){
            int out = outcomes[seq.indexAt(i)];
            dist[out] += weights[seq.indexAt(i)];
        }
        return dist;
    }
    
    protected int argMax(double[] dist) {
        int max = 0;
        for(int i = 1; i < dist.length; i++){
            if(dist[i] > dist[max]) {
                max = i;
            }
        }
        return max;
    }

}
