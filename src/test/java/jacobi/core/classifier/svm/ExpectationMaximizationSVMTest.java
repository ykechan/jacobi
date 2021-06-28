package jacobi.core.classifier.svm;

import java.util.function.IntPredicate;
import java.util.stream.IntStream;

import org.junit.Test;
import org.junit.runner.RunWith;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.JacobiJUnit4ClassRunner;

@JacobiImport("/jacobi/test/data/ExpectationMaximizationSVMTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class ExpectationMaximizationSVMTest {
	
	@JacobiInject(0)
	public Matrix input;
	
	@JacobiInject(1)
	public Matrix outcomes;
	
	@JacobiInject(2)
	public Matrix props;
	
	@JacobiInject(3)
	public Matrix position;
	
	@JacobiResult(10)
	public Matrix output;
	
	@JacobiResult(11)
	public Matrix distances;
	
	@Test
	@JacobiImport("test expect AND data")
	@JacobiEquals(expected = 10, actual = 10)
	public void shouldBeAbleToComputeExpectedValueUsingAndData() {
		double[] dists = new double[this.input.getRowCount()];
		IntPredicate isin = this.toIsin(this.outcomes);
		double lambda = this.props.get(0, 0);
		
		ExpectationMaximizationSVM em = new ExpectationMaximizationSVM(this.input, isin, lambda);
		double[] out = em.expectation(dists, 1e-4);
		
		this.output = Matrices.wrap(out);
	}
	
	@Test
	@JacobiImport("test max AND data")
	@JacobiEquals(expected = 10, actual = 10)
	public void shouldBeAbleToComputeMaximizedValueUsingAndData() {
		double[] dists = new double[this.input.getRowCount()];
		IntPredicate isin = this.toIsin(this.outcomes);
		double lambda = this.props.get(0, 0);
		
		ExpectationMaximizationSVM em = new ExpectationMaximizationSVM(this.input, isin, lambda);
		double[] out = em.step(dists, 1e-4).item;
		
		this.output = Matrices.wrap(out);
	}
	
	@Test
	@JacobiImport("test optimize bias (1)")
	@JacobiEquals(expected = 10, actual = 10)
	public void shouldBeAbleToComputeOptimalBias1() {
		double[] dists = new double[this.input.getRowCount()];
		IntPredicate isin = this.toIsin(this.outcomes);
		double[] normal = this.props.getRow(0);
		
		ExpectationMaximizationSVM em = new ExpectationMaximizationSVM(this.input, isin, 0.0);
		double bias = em.project(normal, dists);
		
		this.output = Matrices.scalar(bias);
	}
	
	@Test
	@JacobiImport("test optimize bias (2)")
	@JacobiEquals(expected = 10, actual = 10)
	public void shouldBeAbleToComputeOptimalBias2() {
		double[] dists = new double[this.input.getRowCount()];
		IntPredicate isin = this.toIsin(this.outcomes);
		double[] normal = this.props.getRow(0);
		
		ExpectationMaximizationSVM em = new ExpectationMaximizationSVM(this.input, isin, 0.0);
		double bias = em.project(normal, dists);
		
		this.output = Matrices.scalar(bias);
	}
	
	@Test
	@JacobiImport("test align linear insep (1)")
	@JacobiEquals(expected = 10, actual = 10)
	public void shouldBeAbleToAlignNormalVectorUsingLinearInsepData1() {
		double[] dists = IntStream.range(0, this.input.getRowCount())
			.mapToDouble(i -> this.outcomes.get(i, 1))
			.toArray();
		IntPredicate isin = this.toIsin(this.outcomes);
		double lambda = this.props.get(0, 0);
		
		ExpectationMaximizationSVM em = new ExpectationMaximizationSVM(this.input, isin, lambda){

			@Override
			protected double marginalize(double[] svm, double[] dists, double epsilon) {
				// test align only
				return 1;
			}
			
		};
		double[] next = em.step(dists, 1e-4).item;
		this.output = Matrices.wrap(next);
	}
	
	@Test
	@JacobiImport("test align linear insep (2)")
	@JacobiEquals(expected = 10, actual = 10)
	public void shouldBeAbleToAlignNormalVectorUsingLinearInsepData2() {
		double[] dists = IntStream.range(0, this.input.getRowCount())
			.mapToDouble(i -> this.outcomes.get(i, 1))
			.toArray();
		IntPredicate isin = this.toIsin(this.outcomes);
		double lambda = this.props.get(0, 0);
		
		ExpectationMaximizationSVM em = new ExpectationMaximizationSVM(this.input, isin, lambda){

			@Override
			protected double marginalize(double[] svm, double[] dists, double epsilon) {
				// test align only
				return 1;
			}
			
		};
		double[] next = em.step(dists, 1e-4).item;
		this.output = Matrices.wrap(next);
	}
	
	@Test
	@JacobiImport("test margin linear insep (1)")
	@JacobiEquals(expected = 10, actual = 10)
	public void shouldBeAbleToMarginalizeNormalVectorUsingLinearInsepData2() {
		double[] dists = IntStream.range(0, this.input.getRowCount())
			.mapToDouble(i -> this.outcomes.get(i, 1))
			.toArray();
		IntPredicate isin = this.toIsin(this.outcomes);
		double lambda = this.props.get(0, 0);
		
		double[] svm = this.position.getRow(0);
		
		ExpectationMaximizationSVM em = new ExpectationMaximizationSVM(this.input, isin, lambda);
		double margin = em.marginalize(svm, dists, 1e-4);
		this.output = Matrices.scalar(margin);
	}
	
	protected IntPredicate toIsin(Matrix outcomes) {
		return k -> (int) outcomes.get(k, 0) == 0;
	}

}
