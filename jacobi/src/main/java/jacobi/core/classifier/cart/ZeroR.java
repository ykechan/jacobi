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

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import jacobi.core.classifier.cart.data.Column;
import jacobi.core.classifier.cart.data.DataTable;
import jacobi.core.classifier.cart.data.Instance;
import jacobi.core.classifier.cart.data.Sequence;
import jacobi.core.classifier.cart.node.Decision;
import jacobi.core.classifier.cart.node.DecisionNode;
import jacobi.core.util.Weighted;

/**
 * Implementation of 0-R decision making rule, which disregards any feature columns
 * and select the most frequent outcome.
 * 
 * <p>The frequency of the outcomes are weighted.</p>
 * 
 * @author Y.K. Chan
 *
 */
public class ZeroR implements Rule {
	
	/**
	 * Constructor.
	 * @param impurity  Measure function of the impurity of a distribution
	 */
	public ZeroR(Impurity impurity) {
		this.impurity = impurity;
	}

	@Override
	public <T> Weighted<DecisionNode<T>> make(
			DataTable<T> dataTable, 
			Set<Column<?>> features, 
			Sequence seq) {
		Column<T> outCol = dataTable.getOutcomeColumn();
		
		List<Instance> instances = seq.apply(dataTable.getInstances(outCol));
		double[] dist = this.distribute(instances, new double[outCol.cardinality()]);
		int ans = this.argmax(dist);
		
		return new Weighted<>(
			new Decision<>(outCol.valueOf(ans)), 
			this.impurity.of(dist)
		);
	}
	
	/**
	 * Find the weight distribution of distinct outcomes
	 * @param instances  Instances
	 * @param dist  Distribution array
	 * @return  Distribution array
	 */
	protected double[] distribute(List<Instance> instances, double[] dist) {
		Arrays.fill(dist, 0.0);
		for(Instance inst : instances){
			dist[inst.outcome] += inst.weight;
		}
		return dist;
	}
	
	/**
	 * Argument of the maximum entry in a double array
	 * @param dist  Input array
	 * @return  Argument of the maximum entry
	 */
	protected int argmax(double[] dist) {
		int max = 0;
		for(int i = 1; i < dist.length; i++) {
			if(dist[i] > dist[max]) {
				max = i;
			}
		}
		return max;
	}

	private Impurity impurity;
}
