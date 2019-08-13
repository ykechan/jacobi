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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.api.classifier.cart.Column;
import jacobi.core.classifier.cart.data.DataTable;
import jacobi.core.classifier.cart.data.Instance;
import jacobi.core.classifier.cart.data.Sequence;
import jacobi.core.util.Weighted;

/**
 * Implementation of measure the impurity and finding ternary (3-way) split the outcomes according 
 * to a numeric attribute.
 * 
 * <p>Exhausting all ternary split of a data set requires quadratic time complexity. Instead this
 * class implements a stochastic algorithm using Simulated Annealing to attempt to find the optimal
 * splitting strategy.</p>
 * 
 * <p>This class starts with a random guess of the position of the center region, and extend 
 * to the left or to the right when the impurity is lower or the transition probability is
 * larger than a random value. This procedure is repeated a few times to start at different
 * position. The best split found is returned.</p>
 * 
 * <p>This class is experimental.</p>
 * 
 * @author Y.K. Chan
 *
 */
public class RankedTernarySimAnnPartition implements Partition {

	@Override
	public Weighted<double[]> measure(DataTable<?> table, Column<?> target, Sequence seq) {
		return null;
	}
	
	protected Weighted<int[]> search(Column<?> outcomeCol, List<Instance> clusters, int start) {
		Matrix distMat = this.distOf(outcomeCol, clusters, start);
		
		int left = start;
		int right = start + 1;
		
		while(left > 0 || right < clusters.size()) {
			
		}
		
		return null;
	}
	
	protected Weighted<double[]> measure(Matrix mat) {
		double value = 0.0;
		double[] dist = new double[mat.getRowCount()];
		
		for(int i = 0; i < dist.length; i++) {
			double[] row = mat.getRow(i);
			dist[i] = Arrays.stream(row).sum();
			value += dist[i] * this.impurity.of(row);
		}
		
		return new Weighted<>(dist, value);
	}
	
	protected double measure(Matrix mat, double[] dist) {
		double value = 0.0;
		
		for(int i = 0; i < dist.length; i++) {
			double[] row = mat.getRow(i);
			value += dist[i] * this.impurity.of(row);
		}
		
		return value;
	}
	
	protected Matrix distOf(Column<?> outcomeCol, List<Instance> clusters, int center) {
		Matrix mat = Matrices.zeros(3, outcomeCol.cardinality());
		
		for(int i = 0; i < clusters.size(); i++) {
			Instance inst = clusters.get(i);
			double[] row = mat.getRow(i < center ? 0 : i > center ? 2 : 1);
			row[inst.outcome] += inst.weight;
			mat.setRow(i, row);
		}
		return mat;
	}
	
	protected List<Instance> clusterize(List<Instance> instances) {
		int prev = -1;
		int count = 0;
		double weight = 0.0;
		
		List<Instance> clusters = new ArrayList<>();
		for(Instance inst : instances) {
			if(inst.outcome == prev) {
				count++;
				weight += inst.weight;
				continue;
			}
			
			if(count > 0){
				clusters.add(new Instance(count, prev, weight));
			}
			prev = inst.outcome;
			count = 1;
			weight = inst.weight;
		}
		
		if(count > 0) {
			clusters.add(new Instance(count, prev, weight));
		}
		return clusters;
	}

	private Impurity impurity;
}
