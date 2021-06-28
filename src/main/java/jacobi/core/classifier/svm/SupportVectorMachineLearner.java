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
import java.util.Collections;
import java.util.List;
import java.util.function.IntPredicate;

import jacobi.api.Matrix;
import jacobi.api.classifier.ClassifierLearner;
import jacobi.api.classifier.Column;
import jacobi.api.classifier.DataTable;
import jacobi.api.classifier.Instance;
import jacobi.api.classifier.svm.SupportVectorMachine;
import jacobi.api.classifier.svm.SupportVectorMachineParams;
import jacobi.api.classifier.svm.SupportVectorMachines;
import jacobi.api.ext.Data;
import jacobi.core.solver.nonlin.GradientDescentStep;
import jacobi.core.solver.nonlin.IterativeOptimizer;
import jacobi.core.solver.nonlin.IterativeOptimizerStep;
import jacobi.core.solver.nonlin.StationaryIterativeOptimizer;
import jacobi.core.solver.nonlin.VectorFunction;

public class SupportVectorMachineLearner<T> 
	implements ClassifierLearner<T, SupportVectorMachines<T>, SupportVectorMachineParams> {

	@Override
	public SupportVectorMachines<T> learn(DataTable<T> dataTab, SupportVectorMachineParams params) {
		int[] cols = dataTab.getColumns().stream().filter(Column::isNumeric).mapToInt(Column::getIndex).toArray();
		Matrix matrix = this.initFeatures(dataTab, cols);
		
		Column<T> outCol = dataTab.getOutcomeColumn();
		if(outCol.isBoolean()){
			List<Instance> insts = dataTab.getInstances(outCol);
			boolean[] outs = new boolean[insts.size()];
			for(int i = 0; i < outs.length; i++){
				outs[i] = insts.get(i).outcome > 0;
			}
			
			double[] svm = this.learn(matrix, k -> outs[k], params);
			return new SupportVectorMachines<>(outCol, Collections.singletonList(this.toSVM(svm, cols)));
		}
		
		int num = outCol.cardinality();
		int[] outs = dataTab.getInstances(outCol).stream().mapToInt(i -> i.outcome).toArray();
		
		SupportVectorMachine[] svms = new SupportVectorMachine[num];
		
		for(int i = 0; i < num; i++){
			int in = i;
			IntPredicate isin = k -> outs[k] == in;
			
			double[] svm = this.learn(matrix, isin, params);
			svms[i] = this.toSVM(svm, cols);
		}
		
		return new SupportVectorMachines<>(outCol, Arrays.asList(svms));
	}
	
	protected double[] learn(Matrix matrix, IntPredicate isin, SupportVectorMachineParams params) {
		double lambda = params.getRegulationCoeff();
		double epsilon = params.getErrorTolerance();
		
		int emLimit = Math.max(4, (int) Math.log(matrix.getRowCount()));
		
		ExpectationMaximizationSVM em = new ExpectationMaximizationSVM(matrix, isin, lambda);
		double[] start = em.run(emLimit, epsilon);
		
		double eta = params.getLearningRate();
		if(eta == 0.0){
			return start;
		}
		
		long epoch = 1L;
		long limit = params.getMaxEpoch() * epoch;
		
		IterativeOptimizer opt = this.initOptimizer(params, epoch);
		VectorFunction func = this.initProblem(matrix, isin, params);
		
		double[] ans = opt.optimize(func, () -> start, limit, epsilon).item.getVector();
		return ans;
	}
	
	protected IterativeOptimizer initOptimizer(SupportVectorMachineParams params, long size) {
		double eta = params.getLearningRate();
		double beta = params.getDecayRate();
		
		IterativeOptimizerStep step = new GradientDescentStep(eta);
		return new StationaryIterativeOptimizer(step, beta);
	}
	
	protected VectorFunction initProblem(Matrix vectors, IntPredicate isin, SupportVectorMachineParams params) {
		double lambda = params.getRegulationCoeff();
		VectorFunction func = new HingeLoss(vectors, isin, lambda);
		return func;
	}
	
	protected Matrix initFeatures(DataTable<?> dataTab, int[] cols) {
		return dataTab.getMatrix().ext(Data.class).select(cols).prepend(r -> 1.0).get();
	}
	
	protected SupportVectorMachine toSVM(double[] svmArray, int[] cols) {
		double bias = svmArray[0];
		double[] normal = Arrays.copyOfRange(svmArray, 1, svmArray.length);
		
		return new SupportVectorMachine(cols, normal, bias);
	}

}
