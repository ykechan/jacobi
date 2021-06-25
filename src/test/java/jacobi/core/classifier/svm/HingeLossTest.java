package jacobi.core.classifier.svm;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.core.impl.ColumnVector;
import jacobi.core.solver.nonlin.VectorFunction;
import jacobi.core.util.Real;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.JacobiJUnit4ClassRunner;

@JacobiImport("/jacobi/test/data/HingeLossTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class HingeLossTest {
	
	@JacobiInject(0)
	public Matrix input;
	
	@JacobiInject(1)
	public Matrix outcomes;
	
	@JacobiInject(2)
	public Matrix svms;
	
	@JacobiInject(3)
	public Matrix props;
	
	@JacobiResult(10)
	public Matrix output;
	
	@Test
	public void shouldBeAbleToComputeHingeLossWithZeroBias() {
		double[] svm = {0.0, Math.E, Math.PI, Real.GOLDEN_RATIO};
		
		Assert.assertEquals(Math.E, HingeLoss.dot(svm, new double[]{1.0, 1.0, 0.0, 0.0}), 1e-12);
		Assert.assertEquals(Math.PI, HingeLoss.dot(svm, new double[]{1.0, 0.0, 1.0, 0.0}), 1e-12);
		Assert.assertEquals(Real.GOLDEN_RATIO, HingeLoss.dot(svm, new double[]{1.0, 0.0, 0.0, 1.0}), 1e-12);
	}
	
	@Test
	@JacobiImport("test dot svm rand")
	@JacobiEquals(expected = 10, actual = 10)
	public void shouldBeAbleToDotSvmWithRandDataInBinaryOutcome() {
		List<double[]> result = new ArrayList<>();
		
		for(int i = 0; i < this.input.getRowCount(); i++){
			double[] v = this.input.getRow(i);
			double[] svm = this.svms.getRow(0);
			double dist = HingeLoss.dot(svm, v);
			int out = (int) this.outcomes.get(i, 0);
			
			VectorFunction func = new HingeLoss(Matrices.wrap(v), k -> out > 0, 0.0);
			double fx = func.at(svm);
			result.add(new double[]{dist, fx});
		}
		
		this.output = Matrices.wrap(result.toArray(new double[0][]));
	}
	
	@Test
	@JacobiImport("test dot 3 svms")
	@JacobiEquals(expected = 10, actual = 10)
	public void shouldBeAbleToDotSvmWithRandDataInTernaryOutcome() {
		double[] result = new double[this.input.getRowCount()];
		
		for(int i = 0; i < this.input.getRowCount(); i++){
			double[] v = this.input.getRow(i);
			double[] svm = this.svms.getRow(0);
			int out = (int) this.outcomes.get(i, 0);
			
			VectorFunction func = new HingeLoss(Matrices.wrap(v), k -> out > 0, 0.0);
			double fx = func.at(svm);
			result[i] = fx;
		}
		
		this.output = new ColumnVector(result);
	}
	
	@Test
	@JacobiImport("test grad 3 svms")
	@JacobiEquals(expected = 10, actual = 10)
	public void shouldBeAbleToGradSvmWithRandDataInTernaryOutcome() {
		List<double[]> result = new ArrayList<>();
		
		for(int i = 0; i < this.input.getRowCount(); i++){
			double[] v = this.input.getRow(i);
			double[] svm = this.svms.getRow(0);
			int out = (int) this.outcomes.get(i, 0);
			
			VectorFunction func = new HingeLoss(Matrices.wrap(v), k -> out > 0, 0.0);
			double[] gx = func.grad(svm).getVector();
			result.add(gx);
		}
		
		this.output = Matrices.wrap(result.toArray(new double[0][]));
	}

	@Test
	@JacobiImport("test hinge grad 3 svms")
	@JacobiEquals(expected = 10, actual = 10)
	public void shouldBeAbleToSumGradSvmWithRandDataInTernaryOutcome() {
		double lambda = this.props.get(0, 0);
		List<double[]> result = new ArrayList<>();
		
		for(int i = 0; i < this.svms.getRowCount(); i++){
			int in = i;
			VectorFunction func = new HingeLoss(this.input, k -> ((int) this.outcomes.get(k, 0)) == in, lambda);
			double[] svm = this.svms.getRow(i);

			double[] gx = func.grad(svm).getVector();
			result.add(gx);
		}
		this.output = Matrices.wrap(result.toArray(new double[0][]));
	}
}
