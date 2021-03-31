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
import java.util.function.IntUnaryOperator;

import jacobi.api.classifier.ClassifierLearner;
import jacobi.api.classifier.DataTable;
import jacobi.api.classifier.svm.SupportVectorMachine;
import jacobi.core.classifier.WeightedMatrix;
import jacobi.core.op.Dot;
import jacobi.core.util.Weighted;

/**
 * Implementation of the Primal Estimated sub-GrAdient SOlver algorithm for SVM.
 * 
 * <p>
 * Given a set of data {x<sub>i</sub>, y<sub>i</sub>}, y<sub>i</sub> &isin; {-1, 1}, a SVM 
 * minimizes the Hinge-Loss function:
 * 
 * L(w) = &lambda;||w||<sup>2</sup> + &sum;max(0, 1 - y<sub>i</sub>(&lt;w, x<sub>i</sub>&gt; - b)).
 * </p>
 * 
 * <p>The pegasos algorithm applies the stochastic gradient descent algorithm on the
 * sub-gradient of the Hinge-Loss function. In stochastic gradient descent, the instances are 
 * applied iteratively, and the direction is the sub-gradient of the Hinge-Loss function:<br>
 * 
 * w*[k] = w[k] - &tau;( 2&lambda;w[k] - &delta;<sub>p</sub>y(&lt;w, x&gt;)'[k] )<br>
 *       = (1 - 2&tau;&lambda;)w[k] + tau;&delta;<sub>p</sub>y(&lt;w, x&gt)'[k].,<br>
 *       
 * where &delta<sub>p</sub> is the Kronecker delta for predicate p: y(&lt;w, x&gt; - b) &lt; 1<br>
 * </p>
 * 
 * <p>
 * &tau; is set to 1/t&lambda;, which t is the time into the algorithm. The step thus simplifies to
 * 
 * w*[k] = (1 - 2/t)w[k] + &delta<sub>p</sub>y(&lt;w, x&gt)'[k]/t.,<br>
 * 
 * It is common to merge the get rid of the 2 by some coefficient, but it is kept here for a better
 * estimation of the value of &lambda;. Instead t is started at 2 to use the first instance
 * as starting position.
 * </p>
 * 
 * <p>
 * The bias term is treated in equal footings with w:
 * 
 * b* = (1 - 2/t)b - &delta;y<sub>i</sub>.
 * 
 * This causes a slight deviation from the original problem s.t. the bias term is also regulated. Using 
 * a linear kernal function as an example, which is itself a hyperplane &lt;n, x&gt; = b. The bias value
 * b = &lt;n, p&gt;, in which p is some point that lies on the plane. Thus the magnitude of b is related
 * to the normal vector n and regulating the bias term together is not entirely non-sensical.
 * </p>
 * 
 * <p>Pegasos algorithm iterates a fixed number of time steps, and each randomly picks an instances
 * to update.</p>
 * 
 * @author Y.K. Chan
 *
 */
public class DirectPegasosLearner implements ClassifierLearner<Boolean, SupportVectorMachine, Double> {
	
	/**
	 * Constructor.
	 * @param rand  Random function
	 * @param epochs  Number of epochs to iterative
	 */
	public DirectPegasosLearner(IntUnaryOperator rand, int epochs) {
		this.rand = rand;
		this.epochs = epochs;
	}
	
	@Override
	public SupportVectorMachine learn(DataTable<Boolean> dataTab, Double lambda) {
		WeightedMatrix data = WeightedMatrix.of(dataTab);
		Weighted<double[]> model = this.run(data, lambda);
		return new SupportVectorMachine(model.item, model.weight);
	}
	
	/**
	 * Run the pegasos algorithm
	 * @param insts  Instances of training data
	 * @param lambda  Control parameter
	 * @return  Normal vector and bias
	 */
	public Weighted<double[]> run(WeightedMatrix insts, double lambda) {
		Weighted<double[]> model = new Weighted<>(new double[insts.getColCount()], 0.0);
		
		int t = 2;
		for(int i = 0; i < this.epochs; i++){
			model = this.runEpoch(insts, t, lambda, model);
			t += insts.getRowCount();
		}
		return model;
	}
	
	/**
	 * Run a epoch of pegasos algorithm
	 * @param input  Input pair of features and associated weights as column vector
	 * @param time  Start time
	 * @param lambda  Strength of regulation term
	 * @param starts  Starting normal vector and bias
	 * @return  Normal vector and bias
	 */
	public Weighted<double[]> runEpoch(WeightedMatrix insts, int time, double lambda, Weighted<double[]> starts) {
		double[] normal = Arrays.copyOf(starts.item, insts.getColCount());
		double bias = starts.weight;
	
		for(int i = 0; i < insts.getRowCount(); i++){
			int target = this.rand.applyAsInt(insts.getRowCount());
			
			double[] x = insts.getRow(target);
			double weight = insts.getWeight(target);
			double y = Math.signum(weight);
			
			double z = y * (Dot.prod(normal, x) - bias);
		
			// update
			int t = time + i;
			double[] dx = x;
			
			double mu = 1 - 2.0 / t;
			
			for(int k = 0; k < normal.length; k++){
				double w = normal[k];
				normal[k] = mu * w + (z < 1 ? y * dx[k] / t : 0);
			}
			
			bias = mu * bias - (z < 1 ? y / t : 0);
		}
		return new Weighted<>(normal, bias);
	}

	private IntUnaryOperator rand;
	private int epochs;
}
