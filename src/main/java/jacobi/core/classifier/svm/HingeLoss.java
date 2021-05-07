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

import java.util.function.IntUnaryOperator;

import jacobi.api.Matrix;
import jacobi.core.impl.ColumnVector;
import jacobi.core.solver.nonlin.VectorFunction;

/**
 * The Hinge-Loss function on a set of data as a vector function.
 * 
 * @author Y.K. Chan
 *
 */
public class HingeLoss implements VectorFunction {
	
	/**
	 * Find the projection distance between an expanded SVM and a vector for it to belong
	 * in a certain class given by the class index
	 * @param svm  Expanded SVM
	 * @param vector  Instance vector
	 * @param in  Class index
	 * @return  Projection distance
	 */
	public static double dot(double[] svm, double[] vector, int in) {
		if(svm.length % vector.length != 0){
			throw new IllegalArgumentException("Dimension mismatch");
		}
		
		int offset = in * vector.length;
		if(offset < 0 || offset >= svm.length){
			throw new IllegalArgumentException("Invalid class index " + in);
		}
		
		int dim = vector.length;
		double dist = -svm[offset];
		for(int i = 1; i < dim; i++){
			dist += svm[offset + i] * vector[i];
		}
		return dist;
	}

	/**
	 * Constructor
	 * @param vectors  Instance vectors
	 * @param isin  Vector index to class index mapping
	 * @param lambda  Coefficient of the norm term
	 */
	public HingeLoss(Matrix vectors, IntUnaryOperator isin, double lambda) {
		this.vectors = vectors;
		this.isin = isin;
		this.lambda = lambda;
	}

	@Override
	public double at(double[] svm) {
		int dim = this.vectors.getColCount();
		if(svm.length % dim != 0){
			throw new IllegalArgumentException("Dimension mismatch");
		}
		
		int num = svm.length / dim;
		
		double fx = 0.0;
		for(int i = 0; i < this.vectors.getRowCount(); i++){
			double[] v = this.vectors.getRow(i);
			int out = this.isin.applyAsInt(i);
			
			for(int j = 0; j < num; j++){
				int sgn = out == j ? 1 : -1;
				double hx = sgn * HingeLoss.dot(svm, v, j);
				
				fx += hx < 1 ? 1 - hx : 0.0;
			}
		}
		
		if(this.lambda == 0){
			return fx;
		}
		
		double norm = 0.0;
		for(int j = 0; j < num; j++){
			int offset = j * dim;
			for(int k = 1; k < dim; k++){
				double w = svm[offset + k];
				norm += w * w;
			}
		}
		return fx + this.lambda * norm;
	}

	@Override
	public ColumnVector grad(double[] svm) {
		int dim = this.vectors.getColCount();
		if(svm.length % dim != 0){
			throw new IllegalArgumentException("Dimension mismatch");
		}
		
		int num = svm.length / dim;
		double[] gx = new double[svm.length];
		for(int i = 0; i < this.vectors.getRowCount(); i++){
			double[] v = this.vectors.getRow(i);
			int out = this.isin.applyAsInt(i);
			
			for(int j = 0; j < num; j++){
				int sgn = out == j ? 1 : -1;
				int in = sgn * (j + 1);
				
				gx = this.grad(svm, v, in, gx);
			}
		}
		
		if(this.lambda == 0){
			return new ColumnVector(gx);
		}
		
		for(int i = 0; i < num; i++){
			int offset = i * dim;
			for(int j = 1; j < dim; j++){
				gx[offset + j] += 2 * this.lambda * svm[offset + j];
			}
		}
		return new ColumnVector(gx);
	}

	@Override
	public Matrix hess(double[] svm) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Find the sub-gradient of the Hinge-Loss function of a particular class index and matching sign
	 * @param svm  Expanded SVM
	 * @param vector  Instance vector
	 * @param in  Class index and matching sign, i.e. sgn(in) denotes if it matches, and |in| - 1 is the class index
	 * @param gx  Sub-gradient vector
	 * @return  Sub-gradient vector
	 */
	protected double[] grad(double[] svm, double[] vector, int in, double[] gx) {
		int sgn = in > 0 ? 1 : -1;
		int out = Math.abs(in) - 1;
		
		double hx = sgn * HingeLoss.dot(svm, vector, out);
		if(hx < 1){
			int offset = out * vector.length;
			gx[offset] += sgn;
			
			for(int i = 1; i < vector.length; i++){
				gx[offset + i] -= sgn * vector[i];
			}
		}
		return gx;
	}

	private Matrix vectors;
	private IntUnaryOperator isin;
	private double lambda;
}
