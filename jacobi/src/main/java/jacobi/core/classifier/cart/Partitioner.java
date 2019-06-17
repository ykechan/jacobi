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
 * Common interface for accessing and evaluate the strategy and impurity measure if partitioning
 * a data set by a certain feature attribute.
 * 
 * <p>Implementations can provide additional information on the best strategy found
 * to partitioning a data set, typically for numeric attributes.
 * </p>
 * 
 * @author Y.K. Chan
 * @param <T>  Type of additional information on the partition
 */
public interface Partitioner<T> {
    
    /**
     * Analyze the partition strategy and impurity measure given a list of instances.
     * @param target  Target attribute to base the partition on
     * @param goal  Goal attribute i.e. the column type of outcome values
     * @param instances  List of instances
     * @return  The impurity measure and additional information of the partition
     */
    public Weighted<T> partition(Column<?> target, Column<?> goal, List<Instance> instances);
    
    /**
     * Data class for an instance of data.
     * 
     * This class is immutable.
     * 
     * @author Y.K. Chan
     */
    public static class Instance {
       
        /**
         * Feature value and outcome value of this instance.
         */
        public final int feature, outcome;
        
        /**
         * Weight of this instance
         */
        public final double weight;

        /**
         * Constructor.
         * @param feature  Feature value
         * @param outcome  Outcome value
         * @param weight  Weight of this instance
         */
        public Instance(int feature, int outcome, double weight) {
            this.feature = feature;
            this.outcome = outcome;
            this.weight = weight;
        }
        
    }

}
