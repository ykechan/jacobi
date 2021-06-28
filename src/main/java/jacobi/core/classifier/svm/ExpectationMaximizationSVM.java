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

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.DoubleConsumer;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;
import java.util.zip.CRC32;

import jacobi.api.Matrix;
import jacobi.core.util.Weighted;

/**
 * Initalize solution of SVM by expectation-maximization algorithm.
 * 
 * @author Y.K. Chan
 *
 */
public class ExpectationMaximizationSVM {
	
	/**
	 * Constructor.
	 * @param vectors  Feature vectors
	 * @param isin  Index to class mapping
	 * @param lambda  Regulation coefficient
	 */
	public ExpectationMaximizationSVM(Matrix vectors, IntPredicate isin, double lambda) {
		this.vectors = vectors;
		this.isin = isin;
		this.lambda = lambda;
	}
	
	/**
	 * Run the expectation-maximization iterations
	 * @param limit  Maximum number of iterations
	 * @param epsilon  Error tolerance
	 * @return  SVM found with minimum error
	 */
	public double[] run(int limit, double epsilon) {
		double[] dists = new double[this.vectors.getRowCount()];
		Weighted<double[]> result = this.step(dists, epsilon);
		double[] svm = result.item;
		if(svm.length < 1){
			// no data
			return svm;
		}
		
		double min = result.weight;
		double[] minSvm = svm;
		
		Set<Long> done = new TreeSet<>();
		
		for(int t = 1; t < limit; t++){
			long hash = this.hash(dists, epsilon);
			if(done.contains(hash)){
				break;
			}
			
			Weighted<double[]> res = this.step(dists, epsilon);
			double[] next = res.item;
			if(next.length < 1){
				break;
			}
			
			double sim = this.cosine(svm, next);
			svm = next;
			
			if(res.weight < min){
				min = res.weight;
				minSvm = res.item;
			}
			
			if(sim > 1 - epsilon){
				break;
			}
			
			done.add(hash);
		}
		return minSvm;
	}
	
	protected Weighted<double[]> step(double[] dists, double epsilon) {
		double[] svm = this.expectation(dists, epsilon);
		if(svm.length < 1){
			return new Weighted<>(svm, 0);
		}
		
		double bias = this.project(svm, dists);
		svm[0] = bias;
		
		double margin = this.marginalize(svm, dists, epsilon);

		double sqNorm = 0.0;
		for(int i = 0; i < svm.length; i++){
			svm[i] *= margin;
			if(i > 0){
				sqNorm += svm[i] * svm[i];
			}
		}
		
		double loss = this.lambda * sqNorm;
		for(int i = 0; i < dists.length; i++){
			dists[i] *= margin;
			
			if(dists[i] < 1){
				loss += 1 - dists[i];
			}
		}
		return new Weighted<>(svm, loss);
	}
	
	/**
	 * Find the expected value of the normal vector given the distances for each feature vector
	 * @param dists  Distances to the boundary
	 * @param in  Class of begin considered positive
	 * @param epsilon  Error tolerance
	 * @return  Expected value of the normal vector
	 */
	protected double[] expectation(double[] dists, double epsilon) {
		int dim = this.vectors.getColCount();
		double[] pos = new double[dim];
		double[] neg = new double[dim];
		
		int numPos = 0;
		int num = 0;
		
		for(int i = 0; i < dists.length; i++){
			double w = dists[i];
			if(w > 1.0 + epsilon){
				continue;
			}
			
			double[] v = this.vectors.getRow(i);
			boolean isPos = this.isin.test(i);
			
			double[] t = isPos ? pos : neg;
			for(int j = 1; j < t.length; j++){
				t[j] += v[j];
			}
			
			numPos += isPos ? 1 : 0;
			num++;
		}
		
		int numNeg = num - numPos;
		
		if(numPos < 1 || numNeg < 1){
			return new double[0];
		}
		
		double[] normal = new double[dim];
		for(int j = 1; j < normal.length; j++){
			normal[j] = pos[j] / numPos - neg[j] / numNeg;
		}
		
		return normal;
	}
	
	/**
	 * Project the distance of the feature vectors to the decision boundary
	 * @param svm  Input SVM
	 * @param in  Index of positive outcome
	 * @param dists  Output distances of the feature vectors to the decision boundary
	 * @return  Bias distance of the feature vectors
	 */
	protected double project(double[] svm, double[] dists) {
		double minUpper = Double.POSITIVE_INFINITY;
		double maxLower = Double.NEGATIVE_INFINITY;
		
		for(int i = 0; i < dists.length; i++){
			double[] v = this.vectors.getRow(i);
			int sgn = this.isin.test(i) ? 1 : -1;
			
			double hx = HingeLoss.dot(svm, v);
			double b = hx;
			
			switch(sgn){
				case 1 :
					if(b < minUpper){
						minUpper = b;
					}
					break;
				
				case -1 :
					if(b > maxLower){
						maxLower = b;
					}
					break;
					
				default :
					throw new IllegalStateException();
			}
			
			dists[i] = b;
		}
		
		DoubleConsumer func = b -> {
			for(int i = 0; i < dists.length; i++){
				int sgn = this.isin.test(i) ? 1 : -1;
				double hx = dists[i];
				dists[i] = sgn * (hx - b);
 			}
			
		};
		
		boolean hasUpper = Double.isFinite(minUpper);
		boolean hasLower = Double.isFinite(maxLower);
		
		if(!hasUpper || !hasLower){
			double bias = hasUpper ? minUpper : hasLower ? maxLower : 0.0;
			func.accept(bias);
			return bias;
		}
		
		if(maxLower < minUpper){
			double bias = (maxLower + minUpper) / 2;
			func.accept(bias);
			return bias;
		}
		
		double bias = this.optimizeBias(dists, maxLower, minUpper);
		func.accept(bias);
		return bias;
	}
	
	/**
	 * Find the optimal bias given the distances between feature vectors and decision boundary
	 * @param dists  Distances of the feature vectors to the decision boundary
	 * @param maxLower  Maximum lower bound
	 * @param minUpper  Minimum upper bound 
	 * @param in  Index of positive outcome
	 * @return  Optimal bias distance
	 */
	protected double optimizeBias(double[] dists, double maxLower, double minUpper) {
		double[] lowers = IntStream.range(0, dists.length)
			.filter(this.isin.negate())
			.mapToDouble(i -> dists[i])
			.filter(v -> v > minUpper)
			.sorted().toArray();
		
		double[] uppers = IntStream.range(0, dists.length)
			.filter(this.isin)
			.mapToDouble(i -> dists[i])
			.filter(v -> v < maxLower)
			.sorted().toArray();
		
		// reverse lower bounds to descending order
		for(int i = 0, j = lowers.length - 1; i < j; i++, j--){
			double tmp = lowers[i];
			lowers[i] = lowers[j];
			lowers[j] = tmp;
		}
		
		int len = Math.min(lowers.length, uppers.length);
		for(int i = 0; i < len; i++){
			if(lowers[i] < uppers[i]){
				return (lowers[i] + uppers[i]) / 2;
			}
		}
		
		return (minUpper + maxLower) / 2;
	}
	
	/**
	 * Find the margin that minimize the Hinge-Loss function given a SVM
	 * @param svm  Input svm
	 * @param dists  Distances of the feature vectors to the decision boundary
	 * @param epsilon  Error tolerance
	 * @return  Margin as multiplier
	 */
	protected double marginalize(double[] svm, double[] dists, double epsilon) {
		double sqNorm = 0.0;
		for(int i = 1; i < svm.length; i++){
			double w = svm[i];
			sqNorm += w * w;
		}
		
		double error = 0.0;
		double support = 0.0;
		for(double hx : dists){
			if(hx < 0.0){
				error -= hx;
				continue;
			}
			
			if(hx < 1.0){
				support += 1 - hx;
			}
		}
		
		double margin = 1;
		double minHx = this.lambda * sqNorm + error + support;
		
		double[] margins = Arrays.stream(dists)
				.filter(v -> v > epsilon).distinct().sorted().toArray();
		if(margins.length < 1){
			return 1.0;
		}
		
		double sv = 0.0;
		for(int i = 0; i < margins.length; i++){
			double m = 1 / margins[i];
			double hx = m * m * this.lambda * sqNorm + m * error + (i - m * sv);
			
			if(hx < minHx){
				minHx = hx;
				margin = m;
			}
			
			sv += margins[i];
		}
		return margin;
	}
	
	protected double cosine(double[] u, double[] v) {
		double dot = 0.0;
		double uNorm = 0.0;
		double vNorm = 0.0;
		
		for(int i = 1; i < v.length; i++){
			dot += u[i] * v[i];
			uNorm += u[i] * u[i];
			vNorm += v[i] * v[i];
		}
		return dot / Math.sqrt(uNorm * vNorm);
	}
	
	protected long hash(double[] dists, double epsilon) {
		CRC32 crc = new CRC32();
		ByteBuffer buf = ByteBuffer.allocate(4);
		for(int i = 0; i < dists.length; i++){
			double w = dists[i];
			if(w > 1.0 + epsilon){
				continue;
			}
			
			buf.putInt(0, i);
			crc.update(buf);
		}
		return crc.getValue();
	}
	
	private Matrix vectors;
	private IntPredicate isin;
	private double lambda;
}
