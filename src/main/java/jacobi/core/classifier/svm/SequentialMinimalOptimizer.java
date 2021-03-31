/*
 * The MIT License
 *
 * Copyright 2021 Y.K. Chan
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
package jacobi.core.classifier.svm;

import java.util.Set;
import java.util.TreeSet;
import java.util.function.IntUnaryOperator;

import jacobi.api.classifier.ClassifierLearner;
import jacobi.api.classifier.DataTable;
import jacobi.api.classifier.svm.SupportVectorMachine;
import jacobi.core.classifier.Reweightable;
import jacobi.core.classifier.WeightedMatrix;
import jacobi.core.op.Dot;
import jacobi.core.util.IntStack;
import jacobi.core.util.Real;
import jacobi.core.util.Weighted;

/**
 * Implementation of the sequential minimal optimizer algorithm for SVMs.
 * 
 * For technical details, see <a href="http://ykechan.github.io/jacobi/svm">here</a>
 * 
 * @author Y.K. Chan
 *
 */
public class SequentialMinimalOptimizer implements ClassifierLearner<Boolean, SupportVectorMachine, Double> {
	
	/**
	 * Constructor.
	 * @param rand  Random function
	 * @param epsilon  Tolerance value
	 */
	public SequentialMinimalOptimizer(IntUnaryOperator rand, double epsilon) {
		this.epsilon = epsilon;
	}

	@Override
	public SupportVectorMachine learn(DataTable<Boolean> dataTab, Double control) {
		return null;
	}
	
	public Weighted<double[]> learn(WeightedMatrix wMat, double[] lagrange) {
		// ...
		return null;
	}
	
	protected Delta select(WeightedMatrix wMat, Context context, IntStack pivots) {
		if(pivots.isEmpty()){
			int limit = (int) Math.sqrt(wMat.getRowCount());
			int[] nonKKTs = this.scanNonKKT(wMat, context, limit);
			pivots.pushAll(nonKKTs);
		}
		
		IntStack done = IntStack.newInstance();
		try {
			while(!pivots.isEmpty()){
				int pivot = pivots.pop();
				// find a target for this pivot
				
				// if no target found
				done.push(pivot);
			}
			
			return null;
		} finally {
			pivots.pushAll(done.toArray());
		}
	}
	
	/**
	 * Find instances that violates the KKT conditions
	 * @param wMat  Weighted matrix
	 * @param context  Training context
	 * @param limit  Maximum number of instances to find
	 * @return  Indices of the instances that violates the KKT conditions
	 */
	protected int[] scanNonKKT(WeightedMatrix wMat, Context context, int limit) {
		if(limit > 0 && limit >= wMat.getRowCount()){
			return this.scanNonKKT(wMat, context, 0);
		}
		
		Set<Integer> set = new TreeSet<>();
		for(int i = 0; i < limit; i++){
			int pivot = this.rand.applyAsInt(wMat.getRowCount());
			if(!set.contains(pivot) && !this.satisfyKKT(wMat, context, pivot)){
				set.add(pivot);
			}
		}
		
		if(!set.isEmpty()){
			return set.stream().mapToInt(Integer::intValue).toArray();
		}
		
		// full scan
		IntStack stack = IntStack.newInstance();
		for(int i = 0; i < wMat.getRowCount() && stack.size() < limit; i++){
			if(!this.satisfyKKT(wMat, context, i)){
				stack.push(i);
			}
		}
		return stack.toArray();
	}
	
	/**
	 * Update training context with given delta data
	 * @param wMat  Weighted matrix
	 * @param context  Training context
	 * @param delta  Delta data
	 */
	protected void updateLagrange(WeightedMatrix wMat, Context context, Delta delta) {
		double pOut = wMat.getWeight(delta.pivot);
		double qOut = wMat.getWeight(delta.target);
		
		int pSgn = (int) Math.signum(pOut);
		int qSgn = (int) Math.signum(qOut);
		
		int parity = pSgn * qSgn;
		if(Double.isNaN(delta.alpha)){
			// ...
		}
		
		double pAlpha = context.lagrange[delta.pivot] - parity * delta.alpha;
		double qAlpha = context.lagrange[delta.target] + delta.alpha;
		
		this.assertConstraint(wMat, delta.pivot, pAlpha);
		this.assertConstraint(wMat, delta.target, qAlpha);
		
		context.lagrange[delta.pivot] = pAlpha;
		context.lagrange[delta.target] = qAlpha;
		
		double deltaW = qSgn * delta.alpha / 2;
		double[] u = wMat.getRow(delta.pivot);
		double[] v = wMat.getRow(delta.target);
		
		double[] normal = context.normal;
		for(int i = 0; i < normal.length; i++){
			double dx = v[i] - u[i];
			normal[i] += deltaW * dx;
		}
		
		double pBias = Dot.prod(normal, u) - pSgn;
		double qBias = Dot.prod(normal, v) - qSgn;
		
		context.bias = (pBias + qBias) / 2;
	}
	
	/**
	 * Find the change in value of the latter lagrange multiplier in a pair of instances 
	 * @param wMat  Weighted matrix
	 * @param context  Training context
	 * @param pivot  First instance
	 * @param target  Second instance
	 * @return  Change in lagrange multiplier for the second instance
	 */
	protected double deltaAlpha(WeightedMatrix wMat, Context context, int pivot, int target) {
		if(pivot == target){
			return 0;
		}
		
		double pOut = wMat.getWeight(pivot);
		double qOut = wMat.getWeight(target);
		
		int pSgn = (int) Math.signum(pOut);
		int qSgn = (int) Math.signum(qOut);
		
		int parity = pSgn * qSgn;
		
		if(parity > 0 && context.lagrange[pivot] + context.lagrange[target] < this.epsilon){
			// unable to change if both zero and both are on the same side
			return 0;
		}
		
		double[] u = wMat.getRow(pivot);
		double[] v = wMat.getRow(target);
		
		double[] normal = context.normal;
		
		double normDx = 0.0;
		double wDx = 0.0;
		for(int i = 0; i < normal.length; i++){
			double dx = v[i] - u[i];
			wDx += normal[i] * dx;
			normDx += dx * dx;
		}
	
		if(Real.isNegl(normDx)){
			return Double.NaN;
		}
		
		double dy = qSgn - pSgn;
		
		double qDelta = 2 * qSgn * (dy - wDx) / normDx;
		
		double[] lagrange = context.lagrange;
		
		double pAlpha = Math.max(0, Math.min(lagrange[pivot] - parity * qDelta, Math.abs(pOut)));
		double qAlpha = Math.max(0, Math.min(lagrange[target] + qDelta, Math.abs(pOut)));
		
		double actualDelta = Math.min(
			Math.abs(pAlpha - lagrange[pivot]), 
			Math.abs(qAlpha - lagrange[target])
		);
		
		return Math.signum(qDelta) * actualDelta;
	}
	
	/**
	 * Check if an instance with lagrange multiplier satisfies the KKT conditions
	 * @param wMat  Weighte matrix
	 * @param context  Training context
	 * @param index  Index of the instance
	 * @return  True if it satisfies the KKT conditions, false otherwise
	 */
	protected boolean satisfyKKT(WeightedMatrix wMat, Context context, int index) {
		double lagMult = context.lagrange[index];
		if(Double.isNaN(lagMult)){
			// instance ignored
			return true;
		}
		
		double w = wMat.getWeight(index);
		
		int y = w > 1 ? 1 : -1;
		double bound = Math.abs(w);
		
		double[] v = wMat.getRow(index);
		double yDist = y * (Dot.prod(context.normal, v) - context.bias);
		
		if(lagMult < this.epsilon){
			return yDist > 1 - this.epsilon;
		}
		
		if(lagMult > bound - this.epsilon){
			return yDist < 1;
		}
		
		return true;
	}
	
	/**
	 * Find the normal vector and bias given the solution of lagrange multipliers
	 * @param wMat  Weighted matrix
	 * @param lagrange  Lagrange multipliers
	 * @return  Normal vector and bias
	 */
	protected Weighted<double[]> toSVM(WeightedMatrix wMat, double[] lagrange) {
		double[] normal = new double[wMat.getColCount()];
		
		for(int i = 0; i < wMat.getRowCount(); i++){
			double[] v = wMat.getRow(i);
			double w = Math.signum(wMat.getWeight(i)) * lagrange[i];
			
			for(int j = 0; j < normal.length; j++){
				normal[j] += w * v[j];
			}
		}
		
		for(int j = 0; j < normal.length; j++){
			normal[j] /= 2;
		}
		
		double bias = 0.0;
		int num = 0;
		
		for(int i = 0; i < wMat.getRowCount(); i++){
			double[] v = wMat.getRow(i);
			int y = wMat.getWeight(i) > 0 ? 1 : -1;
			
			double dist = Dot.prod(normal, v);
			if(y * dist > 1){
				continue;
			}
			
			bias += dist - y;
			num++;
		}
		return new Weighted<>(normal, bias / num);
	}
	
	/**
	 * Assert bound constraint on lagrange multiplier of an instance 
	 * @param wMat  Weighted matrix
	 * @param index  Index of the instance
	 * @param lagMult  Value of the lagrange multiplier
	 */
	protected void assertConstraint(WeightedMatrix wMat, int index, double lagMult) {
		double bound = Math.abs(wMat.getWeight(index));
		
		if(lagMult < 0.0 || lagMult > bound){
			throw new IllegalStateException("Lagrange multiplier #" + index
				+ "=" + lagMult + " is out of bound [0, " + bound + "].");
		}
	}
	
	/**
	 * Formulate the dual optimization problem as a weighted matrix with signed weights as outcomes and bounds
	 * @param dataTab  Input data table 
	 * @param control  Control parameters
	 * @return  Weighted matrix with signed weights as outcomes and bounds
	 */
	protected WeightedMatrix formulate(DataTable<Boolean> dataTab, Double control) {
		if(Real.isNegl(control - 1)){
			return WeightedMatrix.of(dataTab);
		}
		
		double[] bounds = dataTab.getInstances(dataTab.getOutcomeColumn()).stream()
			.mapToDouble(i -> control * i.weight).toArray();
		return this.formulate(Reweightable.of(dataTab).reweight(bounds), 1.0);
	}
	
	private IntUnaryOperator rand;
	private double epsilon;
	
	/**
	 * Data class for training context that is mutable throughout the training process
	 * 
	 * @author Y.K. Chan
	 *
	 */
	protected static class Context {
		
		/**
		 * Normal vector
		 */
		public final double[] normal;
		
		/**
		 * Bias value
		 */
		public double bias;
		
		/**
		 * Lagrangian multipliers
		 */
		public final double[] lagrange;

		/**
		 * Constructor.
		 * @param normal  Normal vector
		 * @param bias  Bias value
		 * @param lagrange  Lagrangian multipliers
		 */
		public Context(double[] normal, double bias, double[] lagrange) {
			this.normal = normal;
			this.bias = bias;
			this.lagrange = lagrange;
		}
		
	}
	
	/**
	 * Data class for change in a pair of lagrange multipliers
	 * 
	 * @author Y.K. Chan
	 *
	 */
	protected static class Delta {
		
		/**
		 * Indices for the 1st and 2nd lagrange multiplier
		 */
		public final int pivot, target;
		
		/**
		 * Change in value for the 2nd lagrange multiplier
		 */
		public final double alpha;

		/**
		 * Constructor.
		 * @param pivot  First lagrange multiplier
		 * @param target  Second lagrange multiplier
		 * @param alpha  Change for the second lagrange multiplier
		 */
		public Delta(int pivot, int target, double alpha) {
			this.pivot = pivot;
			this.target = target;
			this.alpha = alpha;
		}
		
	}
}
