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
package jacobi.core.classifier;

import java.util.function.ToIntFunction;

import jacobi.api.Matrix;

/**
 * Common interface for a supervised learner of classifier.
 * 
 * <p>A classifier in this context is a mapping from a set of attributes in form
 * of a real-valued vector as an array to a discrete category encoded as an integer.</p>
 * 
 * @author Y.K. Chan
 *
 * @param <T> Model class of the classifier
 * 
 */
public interface SupervisedClassifierLearner<T extends ToIntFunction<double[]>> {
    
    /**
     * Learn a classifier from a set of attributes and given classified categories.
     * @param obs  Observations
     * @param outcomes  Outcomes of the observations
     * @return  Classifier model
     */
    public T learn(Matrix obs, int[] outcomes);

}
