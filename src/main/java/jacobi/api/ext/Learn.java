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
package jacobi.api.ext;

import java.util.List;

import jacobi.api.annotations.Facade;
import jacobi.api.annotations.Implementation;
import jacobi.api.classifier.Column;
import jacobi.api.classifier.DefinedSupervised;
import jacobi.api.unsupervised.Unsupervised;
import jacobi.core.classifier.DefinedSupervisedFactory;

/**
 * Extension for models of Machine learning.
 * 
 * @author Y.K. Chan
 *
 */
@Facade
public interface Learn {
	
	/**
	 * Define the column types of the underlying numerical matrix
	 * and associate each row with an outcome for training classifiers.
	 * @param features  List of type of feature columns
	 * @param outcomes  List of outcome
	 * @param <T>  Type of outcome
	 * @return  Extension to learners of classifier models
	 */
	@Implementation(DefinedSupervisedFactory.class)
	public <T> DefinedSupervised<T> classify(List<Column<?>> features, List<T> outcomes);
	
	/**
	 * Define the column types of the underlying numerical matrix
	 * @param features  List of type of feature columns
	 * @param outDef  Column of the outcome
	 * @param <T>  Type of outcome
	 * @return  Extension to learners of classifier models
	 */
	@Implementation(DefinedSupervisedFactory.class)
	public <T> DefinedSupervised<T> classify(List<Column<?>> features, Column<T> outDef);
	
	/**
	 * Get the extension for unsupervised learning, e.g. clustering
	 * @return  Extension to unsupervised learning
	 */
	@Implementation(Unsupervised.Factory.class)
	public Unsupervised unsupervised();

}
