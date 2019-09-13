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

/**
 * Common interface for measurement function for impurities in distribution. 
 * 
 * Common measure e.g. entropy and gini function is provided.
 * 
 * @author Y.K. Chan
 *
 */
public interface Impurity {
    
    /**
     * Entropy function
     */
    public static final Impurity ENTROPY = dist -> {
        double sum = 0.0;
        double rand = 0.0;
        for(int i = 0; i < dist.length; i++) {
            if(dist[i] <= 0.0) {
                continue;
            }
            
            rand += dist[i] * Math.log(dist[i]);
            sum += dist[i];
        }
        return sum <= 0.0 ? 0.0 : Math.log(sum) - rand / sum;
    };
    
    /**
     * Error function
     */
    public static final Impurity ERROR = dist -> {
        double sum = 0.0;
        double max = 0.0;
        for(int i = 0; i < dist.length; i++) {            
            sum += dist[i];
            if(dist[i] > max) {
                max = dist[i];
            }
        }
        return (sum - max) / sum;
    };
    
    /**
     * Find the measurement of impurity given the distribution of items
     * @param dist  Distribution of items
     * @return  Measurement of impurity
     */
    public double of(double[] dist);
    
}
