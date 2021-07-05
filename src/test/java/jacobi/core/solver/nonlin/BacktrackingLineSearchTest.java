package jacobi.core.solver.nonlin;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import jacobi.api.Matrix;
import jacobi.core.impl.ColumnVector;
public class BacktrackingLineSearchTest {
	
	@Test
	public void shouldBeAbleToOptimizeSingleVariableAbsFunc() {
		VectorFunction func = this.abs(new double[1]);
		IterativeOptimizerStep step = this.mock(0.5, 1e-8);
		
		double[] start = new double[]{Math.PI};
		double[] x = Arrays.copyOf(start, start.length);
		for(int i = 0; i < 100; i++){
			double[] dx = step.delta(func, x);
			System.out.println("#" + i +": x = " + Arrays.toString(x) 
				+ ", dx = " + Arrays.toString(dx)
				+ ", fx = " + func.at(x)
				+ ", f' = " + Arrays.toString(func.grad(x).getVector()));
			
			x[0] += dx[0];
		}
	}
	
	protected BacktrackingLineSearch mock(double decay, double eps) {
		IterativeOptimizerStep gd = new GradientDescentStep(1.0);
		return new BacktrackingLineSearch(gd, 0.5, decay, eps);
	}

	protected VectorFunction abs(double[] origin) {
		return new VectorFunction(){

			@Override
			public double at(double[] pos) {
				double val = 0.0;
				for(int i = 0; i < origin.length; i++){
					val += Math.abs(pos[i] - origin[i]);
				}
				return val;
			}

			@Override
			public ColumnVector grad(double[] pos) {
				double[] df = new double[origin.length];
				for(int i = 0; i < df.length; i++){
					df[i] = pos[i] < origin[i] ? -1 : pos[i] > origin[i] ? 1 : 0;
				}
				return new ColumnVector(df);
			}

			@Override
			public Matrix hess(double[] pos) {
				throw new UnsupportedOperationException();
			}
			
		};
	}

}
