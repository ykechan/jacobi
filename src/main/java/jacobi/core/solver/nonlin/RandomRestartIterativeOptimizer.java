package jacobi.core.solver.nonlin;

import java.util.List;
import java.util.function.Supplier;

import jacobi.core.impl.ColumnVector;
import jacobi.core.util.Weighted;

public class RandomRestartIterativeOptimizer implements IterativeOptimizer {
	
	protected RandomRestartIterativeOptimizer(List<IterativeOptimizer> optimizers, int minTrial) {
		this.optimizers = optimizers;
		this.minTrial = minTrial;
	}
	
	@Override
	public Weighted<ColumnVector> optimize(VectorFunction func, 
			Supplier<double[]> init, 
			long limit, double epsilon) {
		int trial = 0;
		Weighted<ColumnVector> bestAns = null;

		long trialLimit = limit / this.optimizers.size();
		
		for(IterativeOptimizer optimizer : this.optimizers){
			Weighted<ColumnVector> ans = optimizer
					.optimize(func, init, trialLimit, epsilon);
			
			if(bestAns == null || ans.weight < bestAns.weight) {
				bestAns = ans;
			}
			
			if(trial > this.minTrial && bestAns.weight < epsilon) {
				break;
			}
		}
		
		return bestAns;
	}
	
	private List<IterativeOptimizer> optimizers;
	private int minTrial;
}
