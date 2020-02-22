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
import java.util.function.DoubleSupplier;
import java.util.function.IntToDoubleFunction;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.api.classifier.Column;
import jacobi.api.classifier.DataTable;
import jacobi.api.classifier.Instance;
import jacobi.core.classifier.cart.Sequence;
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
	
	public RankedTernarySimAnnPartition(Impurity impurity) {
		this.impurity = impurity;
	}

	@Override
	public Weighted<double[]> measure(DataTable<?> table, Column<?> target, Sequence seq) {
		List<Instance> clusters = this.clusterize(seq.apply(table.getInstances(target)));
		if(clusters.isEmpty()){
			throw new UnsupportedOperationException();
		}
		
		if(clusters.size() == 1) {
			return new Weighted<>(new double[0], Double.NaN);
		}
		
		Matrix mat = table.getMatrix();
		int col = target.getIndex();
		IntToDoubleFunction split = k -> (
			mat.get(seq.indexAt(k - 1), col) + mat.get(seq.indexAt(k), col)
		) / 2.0;
		
		if(clusters.size() == 2) {
			int rank = clusters.get(0).feature;			
			// perfect binary partition
			return new Weighted<>(new double[] {split.applyAsDouble(rank)}, 0.0);
		}
		
		if(clusters.size() == 3) {
			int left = clusters.get(0).feature;
			int right = left + clusters.get(1).feature;
			
			// perfect ternary partition
			return new Weighted<>(new double[] {
				split.applyAsDouble(left),
				split.applyAsDouble(right)
			}, 0.0);
		}
		
		// ...
		return null;
	}
	
	protected Weighted<int[]> search(List<Instance> clusters, Column<?> outCol, DoubleSupplier rand) {
		int left = (int) Math.floor(clusters.size() * rand.getAsDouble());
		int right = left + 1;
		
		Matrix dist = this.distOf(clusters, outCol, left);
		Weighted<double[]> curr = this.measure(dist);
		
		int minLeft = left;
		int minRight = right;
		Weighted<double[]> min = curr;		
		
		double temp = 1.0;
		for(int k = 0; k < clusters.size(); k++){
			int move = rand.getAsDouble() < 0.5 ? -1 : 1;
			if((move < 0 && left == 0)
			|| (move > 0 && right == clusters.size() - 1)) {
				continue;
			}
			
			Matrix neighDist = Matrices.wrap(new double[][] {
				move < 0 ? Arrays.copyOf(dist.getRow(0), dist.getColCount()) : dist.getRow(0),
				Arrays.copyOf(dist.getRow(1), dist.getColCount()),
				move > 0 ? Arrays.copyOf(dist.getRow(2), dist.getColCount()) : dist.getRow(2)
			});
			
			Instance swap = clusters.get((move < 0 ? left : right) + move);
			double[] centerRow = neighDist.getRow(1);
			neighDist.getAndSet(move < 0 ? 0 : 2, r -> {
				centerRow[swap.outcome] -= swap.weight;
				r[swap.outcome] += swap.weight;
			});
			neighDist.setRow(1, centerRow);
			
			Weighted<double[]> energy = this.measure(neighDist);
			if(energy.weight < min.weight) {
				min = energy;
				minLeft = left + (move < 0 ? move : 0);
				minRight = right + (move > 0 ? move : 0);
			}
			
			double delta = energy.weight - curr.weight;
			double prob = Math.exp(-delta / temp);
			
			if(rand.getAsDouble() < prob) {
				// move to neighbour
				left += (move < 0 ? move : 0);
				right += (move > 0 ? move : 0);
				
				dist = neighDist;
			}
			
			temp = this.coolFun.apply(temp, k, clusters.size());						
		}
		return new Weighted<>(new int[] {minLeft, minRight}, min.weight);
	}
	
	protected TernarySplit measureNeighbour(List<Instance> clusters, TernarySplit curr, int move) {
		int left = curr.left + (move < 0 ? move : 0);
		int right = curr.right + (move > 0 ? move : 0);
		
		if(left < 0 || right >= clusters.size()) {
			return null;
		}
		
		Matrix dist = curr.dist;
		Matrix neighDist = Matrices.copy(dist);
		
		Instance swap = clusters.get((move < 0 ? left : right) + move);
		double[] centerRow = neighDist.getRow(1);
		neighDist.getAndSet(move < 0 ? 0 : 2, r -> {
			centerRow[swap.outcome] -= swap.weight;
			r[swap.outcome] += swap.weight;
		});
		neighDist.setRow(1, centerRow);
		
		Weighted<double[]> energy = this.measure(neighDist);
		return null;
	}
	
	/**
	 * Measure the impurity of the distribution of weights and weights of the partitions
	 * @param mat  Distribution matrix as partition-by-outcome manner
	 * @return  Impurity measurement and weights of partitions
	 */
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
	
	/**
	 * Measure the impurity of the distribution of weights
	 * @param mat  Distribution matrix as partition-by-outcome manner
	 * @param dist  Total weights of each partition
	 * @return  Impurity measurement
	 */
	protected double measure(Matrix mat, double[] dist) {
		double value = 0.0;
		
		for(int i = 0; i < dist.length; i++) {
			double[] row = mat.getRow(i);
			value += dist[i] * this.impurity.of(row);
		}
		
		return value;
	}
	
	/**
	 * Find the distribution of weights of 3 partitions, with a single cluster at the center.
	 * @param clusters  List of clusters
	 * @param outCol  Column of outcome
	 * @param center  Index of the center cluster
	 * @return  A 3-row matrix with the distribution of outcomes of left, center and right clusters
	 */
	protected Matrix distOf(List<Instance> clusters, Column<?> outCol, int center) {
		Matrix mat = Matrices.zeros(3, outCol.cardinality());
		
		for(int i = 0; i < clusters.size(); i++) {
			Instance inst = clusters.get(i);
			double[] row = mat.getRow(i < center ? 0 : i > center ? 2 : 1);
			row[inst.outcome] += inst.weight;
			mat.setRow(i, row);
		}
		return mat;
	}
	
	/**
	 * Clusterize the continuous instances with the same outcome. The returned instance would have
	 * the instance count as feature and the sum of weights as weight.
	 * @param instances  Instances
	 * @return  Cluster of instances
	 */
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
	private CoolingFunction coolFun;
	
	public interface CoolingFunction {
		
		public double apply(double temp, int k, int limit);
		
	}
	
	protected static class TernarySplit {
		
		public final double energy;	
		
		public final Matrix dist;
		
		public final int left, right;

		public TernarySplit(double energy, Matrix dist, int left, int right) {
			this.energy = energy;
			this.dist = dist;
			this.left = left;
			this.right = right;
		}
		
	}
}
