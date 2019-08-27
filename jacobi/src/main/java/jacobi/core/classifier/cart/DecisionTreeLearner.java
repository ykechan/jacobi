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

import java.util.TreeSet;
import java.util.stream.IntStream;

import jacobi.api.classifier.ClassifierLearner;
import jacobi.api.classifier.DataTable;
import jacobi.api.classifier.cart.DecisionNode;
import jacobi.api.classifier.cart.DecisionTreeParams;
import jacobi.core.classifier.cart.measure.NominalPartition;
import jacobi.core.classifier.cart.measure.Partition;
import jacobi.core.classifier.cart.measure.RankedBinaryPartition;
import jacobi.core.classifier.cart.rule.C45;
import jacobi.core.classifier.cart.rule.OneR;
import jacobi.core.classifier.cart.rule.Rule;
import jacobi.core.classifier.cart.rule.ZeroR;

/**
 * Learn decision tree classifier on a data set.
 * 
 * @author Y.K. Chan
 * @param <T>  Type of outcome
 */
public class DecisionTreeLearner<T> implements ClassifierLearner<T, DecisionNode<T>, DecisionTreeParams> {

	@Override
	public DecisionNode<T> learn(DataTable<T> dataTab, DecisionTreeParams params) {
		return this.createRule(dataTab, params).make(
			dataTab, 
			new TreeSet<>(dataTab.getColumns()), 
			this.allIndices(dataTab.size())
		);
	}
	
	/**
	 * Create rule maker by input data set and parameters
	 * @param dataTab  Input data set
	 * @param params  Decision tree parameters
	 * @return  Rule maker
	 */
	protected Rule createRule(DataTable<?> dataTab, DecisionTreeParams params) {	
		Partition partFunc = this.createPartition(params, false);
		
		if(params.maxHeight >= dataTab.getColumns().size()){
			// no limit
			return C45.of(partFunc);
		}
		
		Rule rule = ZERO_R;
		for(int i = 0; i < params.maxHeight; i++) {
			rule = new OneR(rule, partFunc);
		}
		return rule;
	}
	
	/**
	 * Create partition function by given parameters
	 * @param params  Decision tree parameters
	 * @param nominalOnly  For nominal only
	 * @return  Partition function
	 */
	protected Partition createPartition(DecisionTreeParams params, boolean nominalOnly) {
		Partition nom = new NominalPartition(params.impurityMeasure);
		if(nominalOnly) {
			return nom;
		}
		
		Partition num = new RankedBinaryPartition(params.impurityMeasure);
		return (tab, col, seq) -> (col.isNumeric() ? num : nom).measure(tab, col, seq);
	}
	
	/**
	 * Get the default sequence for all instances
	 * @param len  Length of data set
	 * @return  Default sequence
	 */
	protected Sequence allIndices(int len) {
		return new Sequence(IntStream.range(0, len).toArray(), 0, len);
	}
	
	/**
	 * Common instance for 0-R implementation
	 */
	protected static final Rule ZERO_R = new ZeroR();
}
