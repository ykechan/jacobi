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

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntUnaryOperator;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.api.classifier.ClassifierLearner;
import jacobi.api.classifier.DataTable;
import jacobi.api.classifier.svm.SupportVectorMachine;
import jacobi.api.classifier.svm.SupportVectorMachineParams;
import jacobi.core.classifier.WeightedMatrix;
import jacobi.core.op.Dot;
import jacobi.core.util.Weighted;

/**
 * Learn a Support Vector Machine on a boolean dataset.
 * 
 * @author Y.K. Chan
 *
 */
public class SupportVectorMachineLearner 
	implements ClassifierLearner<Boolean, SupportVectorMachine, SupportVectorMachineParams> {
	
	public SupportVectorMachineLearner() {
		this(n -> ThreadLocalRandom.current().nextInt(n));
	}

	public SupportVectorMachineLearner(IntUnaryOperator rand) {
		this.rand = rand;
	}

	@Override
	public SupportVectorMachine learn(DataTable<Boolean> dataTab, SupportVectorMachineParams params) {
		double lambda = params.getControl();
		if(dataTab.size() < params.getSmoMax()){
			// use SMO
			// ...
		}
		
		DirectPegasosLearner pegasos = new DirectPegasosLearner(this.rand, params.getMinEpochs());
		if(params.getMaxEpochs() <= params.getMinEpochs()){
			return pegasos.learn(dataTab, lambda);
		}
		
		WeightedMatrix wMat = WeightedMatrix.of(dataTab);
		Weighted<double[]> svm = pegasos.run(wMat, lambda);
		
		int numEpochs = params.getMaxEpochs() - params.getMinEpochs();
		int t = 2 + dataTab.size() * params.getMinEpochs();
		
		int step = 1;
		int k0 = 0;
		for(int k = 0; k < numEpochs; k++){
			svm = pegasos.runEpoch(wMat, t, lambda, svm);
			t += dataTab.size();
			
			if(k - k0 < step){
				continue;
			}
			
			double sim = this.distSim(wMat, svm, params);
			if(sim > params.getSmoSimilarity()){
				// ...
			}
			
			step += k - k0;
			k0 = k;
		}
		return new SupportVectorMachine(svm.item, svm.weight);
	}
	
	/**
	 * Compute the distribution similarity, i.e. the cosine similarity between the confusion matrices
	 * @param wMat  Weighted matrix with boolean outcome
	 * @param svm  Normal vector and bias
	 * @param params  Training parameters
	 * @return  Similarity measure
	 */
	protected double distSim(WeightedMatrix wMat, Weighted<double[]> svm, SupportVectorMachineParams params) {
		Matrix conf = Matrices.zeros(2, 4);
		
		int limit = (int) Math.sqrt(wMat.getRowCount());
		
		int num = 0;
		for(int i = 0; i < wMat.getRowCount(); i++){
			double w = wMat.getWeight(i);
			
			double[] v = wMat.getRow(i);
			double dist = Dot.prod(svm.item, v) - svm.weight;
			// true +ve, false +ve, true -ve, false -ve
			int type = 2 * (w > 0 ? 1 : 0) + (dist > 0 ? 1 : 0);
			conf.getAndSet(0, r -> r[type] += Math.abs(w));
			
			if(dist < params.getSmoMargin() || dist > params.getSmoMargin()){
				continue;
			}
			
			conf.getAndSet(1, r -> r[type] += Math.abs(w));
			num++;
			
			if(num > limit){
				return -1;
			}
		}
		
		double[] u = conf.getRow(0);
		double[] v = conf.getRow(1);
		
		return Dot.prod(u, v) / Math.sqrt(Dot.prod(u, u) * Dot.prod(v, v));
	}

	private IntUnaryOperator rand;
}
