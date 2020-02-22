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

/**
 * Common interface for learning a classifier.
 * 
 * <p>Learning a classifier is a task in the category of supervised learning. This 
 * interface accepts a data table, which is a numerical matrix with column type
 * defined for nominal columns, associated with an outcome value for each row,
 * and a generic learning parameter.</p>
 * 
 * @author Y.K. Chan
 *
 * @param <T>  Type of outcome
 * @param <C>  Type of classifier model
 * @param <P>  Type of learning parameters
 */
public interface ClassifierLearner<T, C extends Classifier<T>, P> {
	
	/**
	 * Learn a classifier on input instances and given learning parameters.
	 * @param dataTab  Input instances
	 * @param params  Learning parameters.
	 * @return  Classifier
	 */
	public C learn(DataTable<T> dataTab, P params);

}
