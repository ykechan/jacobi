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

import java.util.Arrays;
import java.util.function.IntPredicate;

import jacobi.api.Matrix;
import jacobi.core.impl.ColumnVector;
import jacobi.core.solver.nonlin.SumLinearArgFunc;
import jacobi.core.util.Weighted;

/**
 * The Hinge-Loss function on a set of data as a vector function.
 * 
 * @author Y.K. Chan
 *
 */
public class HingeLoss extends SumLinearArgFunc<Weighted<double[]>> {
	
	/**
	 * Find the projection distance between an expanded SVM and a vector for it to belong
	 * in a certain class given by the class index
	 * @param svm  Expanded SVM
	 * @param vector  Instance vector
	 * @param in  Class index
	 * @return  Projection distance
	 */
	public static double dot(double[] svm, double[] vector) {
		if(svm.length % vector.length != 0){
			throw new IllegalArgumentException("Dimension mismatch");
		}
		
		int dim = vector.length;
		double dist = -svm[0];
		for(int i = 1; i < dim; i++){
			dist += svm[i] * vector[i];
		}
		return dist;
	}
	
	/**
	 * Constructor
	 * @param consts  Feature vectors
	 * @param isin  Index to in set mapping
	 * @param lambda  Regulation coefficient
	 */
	public HingeLoss(Matrix consts, IntPredicate isin, double lambda) {
		super(consts);
		this.isin = isin;
		this.lambda = lambda;
	}
	
	/**
	 * Get feature vectors
	 * @return  Feature vectors
	 */
	public Matrix getVectors() {
		return this.consts;
	}
	
	/**
	 * Get the class of a feature vector given its index
	 * @param index  Index of feature vector
	 * @return  True if positive, false otherwise
	 */
	public boolean isIn(int index) {
		return this.isin.test(index);
	}
	
	/**
	 * Get regulation coefficient
	 * @return  Regulation coefficient
	 */
	public double getLambda() {
		return this.lambda;
	}

	@Override
	public double at(double[] pos) {
		double hx = super.at(pos);
		
		double sqNorm = 0.0;
		for(int i = 1; i < pos.length; i++){
			sqNorm += pos[i] * pos[i];
		}
		return this.lambda * sqNorm + hx;
	}
	
	@Override
	public ColumnVector grad(double[] pos) {
		Params<Weighted<double[]>> params = this.prepare(pos);
		double bias = params.inter.weight;
		
		ColumnVector gx = super.grad(pos);
		double[] dx = gx.getVector();
		
		int dBias = 0;
		double[] dists = params.inter.item;
		for(int i = 0; i < dists.length; i++){
			int sgn = this.isin.test(i) ? 1 : -1;
			double w = sgn * (dists[i] - bias);
			if(w < 1){
				dBias += sgn;
			}
		}
		
		for(int i = 1; i < pos.length; i++){
			dx[i] += 2 * this.lambda * pos[i];
		}
		dx[0] = dBias;
		return gx;
	}
	
	@Override
	protected double valueAt(Weighted<double[]> inter, int index, double x) {
		double bias = inter.weight;
		double dist = x - bias;
		int sgn = this.isin.test(index) ? 1 : -1;
		double hx = sgn * dist;
		
		return hx < 1 ? 1 - hx : 0;
	}
	
	@Override
	protected double slopeAt(Weighted<double[]> inter, int index, double x) {
		double bias = inter.weight;
		double dist = x - bias;
		int sgn = this.isin.test(index) ? 1 : -1;
		double hx = sgn * dist;
		
		return hx < 1 ? -sgn : 0;
	}
	
	@Override
	protected double convexityAt(Weighted<double[]> inter, int index, double x) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	protected Params<Weighted<double[]>> prepare(double[] pos) {
		double bias = pos[0];
		
		double[] x = Arrays.copyOf(pos, pos.length);
		x[0] = 0;
		
		Params<Weighted<double[]>> params = super.prepare(x);
		params.inter = new Weighted<>(params.inter.item, bias);
		
		return params;
	}
	
	@Override
	protected Weighted<double[]> prepare(double[] pos, double[] args) {
		return new Weighted<>(args, 0.0);
	}
	
	private IntPredicate isin;
	private double lambda;
}
