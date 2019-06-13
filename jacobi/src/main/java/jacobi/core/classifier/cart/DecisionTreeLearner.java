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
import jacobi.core.classifier.cart.node.DecisionNode;

/**
 * Common interface for algorithms of learning a Decision Tree from a data set.
 * 
 * <p>The implementation should only consider the given feature columns and only consider
 * rows given the access sequence.</p>
 * 
 * @author Y.K. Chan
 *
 */
public interface DecisionTreeLearner {
    
    /**
     * Learn the decision tree
     * @param table  Full data table
     * @param weights  Full weights for each instance
     * @param features  Feature available
     * @param seq  Access sequence
     * @return  Root decision node
     */
    public DecisionNode learn(DataTable table, 
        double[] weights, 
        Set<Column<?>> features,
        Sequence seq
    );

}
