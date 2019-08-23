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
import java.util.TreeSet;
import java.util.stream.IntStream;

import jacobi.api.classifier.Column;
import jacobi.api.classifier.cart.DecisionNode;
import jacobi.api.classifier.cart.Strategy;
import jacobi.core.classifier.cart.data.DataTable;
import jacobi.core.classifier.cart.data.Sequence;
import jacobi.core.classifier.cart.measure.Impurity;
import jacobi.core.classifier.cart.measure.NominalPartition;
import jacobi.core.classifier.cart.measure.Partition;
import jacobi.core.classifier.cart.measure.RankedBinaryPartition;
import jacobi.core.classifier.cart.rule.C45;
import jacobi.core.classifier.cart.rule.Id3;
import jacobi.core.classifier.cart.rule.OneR;
import jacobi.core.classifier.cart.rule.Rule;
import jacobi.core.classifier.cart.rule.ZeroR;
import jacobi.core.util.Throw;

public class DecisionTreeLearner {
	
	public <T> DecisionNode<T> learn(DataTable<T> dataTab, Strategy strategy, Impurity impurity) {
		Throw.when()
			.isNull(() -> dataTab, () -> "No training data.")
			.isNull(() -> strategy, () -> "No training strategy provided.")
			.isNull(() -> impurity, () -> "No impurity measurement function provided.");
		
		Set<Column<?>> featSet = new TreeSet<>();
		boolean nomOnly = true;
		for(Column<?> col : dataTab.getColumns()){
			if(strategy == Strategy.ID3 && col.isNumeric()) {
				continue;
			}
			
			if(col.isNumeric()) {
				nomOnly = false;
			}
						
			featSet.add(col);
		}
		
		Sequence seq = this.defaultSeq(dataTab.size());
		return this.createRule(strategy, impurity, nomOnly).make(dataTab, featSet, seq);
	}
	
	protected Rule createRule(Strategy strategy, Impurity impurity, boolean nominalOnly) {
		Partition partFunc = this.createPartition(strategy, impurity, nominalOnly);
		
		switch(strategy) {
			case ZERO_R :
				return new ZeroR();
				
			case ONE_R :
				return new OneR(new ZeroR(), partFunc);
				
			case ID3 : 
				return Id3.of(impurity);
				
			case C45 :
				return C45.of(impurity, partFunc);
				
			default :
				break;
		}
		
		throw new UnsupportedOperationException("Unknown strategy " + strategy);
	}
	
	protected Partition createPartition(Strategy strategy, Impurity impurity, boolean nominalOnly) {
		if(strategy == Strategy.ZERO_R) {
			return null;
		}
		
		Partition nomPart = new NominalPartition(impurity);
		
		if(nominalOnly) {
			return nomPart;
		}
				
		Partition biPart = new RankedBinaryPartition(impurity);
		return (dataTab, target, seq) -> (target.isNumeric() ? biPart : nomPart)
				.measure(dataTab, target, seq);
	}
	
	protected Sequence defaultSeq(int n) {
		return new Sequence(IntStream.range(0, n).toArray(), 0, n);
	}

}
