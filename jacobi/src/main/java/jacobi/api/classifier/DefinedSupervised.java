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
package jacobi.api.classifier;

import jacobi.api.annotations.Facade;
import jacobi.api.annotations.Implementation;
import jacobi.api.classifier.cart.DecisionNode;
import jacobi.api.classifier.cart.DecisionTreeParams;
import jacobi.core.classifier.cart.DecisionTreeLearner;

/**
 * Proxy interface for supervised classifier learners with feature type defined.
 * 
 * @author Y.K. Chan
 * @param <T>  Type of outcome
 */
@Facade(value = DataTable.class)
public interface DefinedSupervised<T> {
	
	@Implementation(DecisionTreeLearner.class)
	public DecisionNode<T> learnTree(DecisionTreeParams param);

}
