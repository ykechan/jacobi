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
package jacobi.core.classifier.cart.rule;

import java.util.Set;

import jacobi.api.classifier.Column;
import jacobi.api.classifier.DataTable;
import jacobi.api.classifier.cart.DecisionNode;
import jacobi.core.classifier.cart.Sequence;

/**
 * Common interface for inferring decision rule on a subset of data table.
 * 
 * <p>Implementations should return the resultant decision node together
 * with the impurity measure after applying this rule.</p>
 * 
 * @author Y.K. Chan
 *
 */
public interface Rule {
	
	/**
	 * Infer the decision rule as node from a subset of data 
	 * and measure the impurity after applying this rule.
	 * @param dataTable  Input data table
	 * @param features  Feature columns to consider
	 * @param seq  Sequence of access
	 * @return  Decision Node with impurity measurement
	 */
	public <T> DecisionNode<T> make(
		DataTable<T> dataTable, 
		Set<Column<?>> features, 
		Sequence seq
	);

}
