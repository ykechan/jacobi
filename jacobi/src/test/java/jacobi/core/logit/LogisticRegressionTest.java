package jacobi.core.logit;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import jacobi.api.Matrix;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.util.JacobiJUnit4ClassRunner;

/**
 * 
 * @author Y.K. Chan
 *
 */
@JacobiImport("/jacobi/test/data/LogisticRegressionTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class LogisticRegressionTest {
	
	@JacobiInject(0)
	public Matrix input;
	
	@JacobiInject(1)
	public Matrix outcome;
	
	@Test
	@JacobiImport("test example 10x3")
	public void shouldBeAbleToDetectBiasColumn() {
		AtomicInteger count = new AtomicInteger(0);
		new LogisticRegression(null, (mat, w) -> new LnLikeLogistic(mat, w)) {

			@Override
			protected int detectBias(double[] stdDev) {
				int bias = super.detectBias(stdDev);
				Assert.assertEquals(0, bias);
				count.incrementAndGet();
				return bias;
			}
			
			
		}.init(this.input, () -> 0.0);
		
		Assert.assertEquals(1, count.get());
	}
	
	@Test
	@JacobiImport("test example 10x3")
	public void shouldBeAbleToInitAroundZero() {
		double[] pos = new LogisticRegression(null, (mat, w) -> new LnLikeLogistic(mat, w))
			.init(this.input, () -> 1.0)
			.get();
		System.out.println(Arrays.toString(pos));
	}
	
	@Test
	@JacobiImport("test example 10x3")
	public void shouldBeAbleToFitLogisticCurveOnPerfectSeparableData() {
		double[] ans = new LogisticRegression(
				LogisticRegression.DEFAULT_OPTIMIZER, 
				LnLikeLogistic::new)
			.compute(this.input, IntStream.range(0, this.outcome.getRowCount())
					.mapToDouble(i -> this.outcome.get(i, 0))
					.toArray());
		System.out.println(Arrays.toString(ans));
	}

}
