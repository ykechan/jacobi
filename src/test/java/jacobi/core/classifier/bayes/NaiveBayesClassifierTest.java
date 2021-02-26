package jacobi.core.classifier.bayes;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;

import jacobi.api.classifier.Column;

public class NaiveBayesClassifierTest {
	
	@Test
	public void shouldBeAbleToSumLikelihoodAndPriors() {
		Column<Integer> outCol = Column.nominal(-1, 3, v -> (int) v);
		BayesianClassifier<Integer> classifier = new NaiveBayesClassifier<>(
			outCol, new double[]{-1.0, -2.0, -3.0}, Arrays.asList(
				this.bias(outCol, new double[]{-3.0, -1.0, -2.0})
			)
		);
		
		double[] probs = classifier.eval(new double[outCol.cardinality()], outCol);
		Assert.assertArrayEquals(new double[]{-4.0, -3.0, -5.0}, probs, 1e-12);
	}
	
	@Test
	public void shouldBeAbleToSumMultipleLikelihoodAndPriors() {
		Column<Integer> outCol = Column.nominal(-1, 3, v -> (int) v);
		BayesianClassifier<Integer> classifier = new NaiveBayesClassifier<>(
			outCol, new double[]{-1.0, -2.0, -3.0}, Arrays.asList(
				this.bias(outCol, new double[]{-3.0, -1.0, -2.0}),
				this.bias(outCol, new double[]{-0.3, -1.2, -0.0})
			)
		);
		
		double[] probs = classifier.eval(new double[outCol.cardinality()], outCol);
		Assert.assertArrayEquals(new double[]{-4.3, -4.2, -5.0}, probs, 1e-12);
	}
	
	@Test
	public void shouldEvalYieldsTheSameResultAsEvalAll() {
		Column<Integer> outCol = Column.nominal(-1, 3, v -> (int) v);
		BayesianClassifier<Integer> classifier = new NaiveBayesClassifier<>(
			outCol, new double[]{-1.0, -2.0, -3.0}, Arrays.asList(
				this.bias(outCol, new double[]{-3.0, -1.0, -2.0})
			)
		);
		
		double[] probs = IntStream.range(0, outCol.cardinality())
			.mapToDouble(v -> classifier.eval(new double[outCol.cardinality()], v))
			.toArray();
		
		Assert.assertArrayEquals(new double[]{-4.0, -3.0, -5.0}, probs, 1e-12);
	}
	
	protected <T> BayesianClassifier<T> bias(Column<T> outCol, double[] lnLikes) {
		return new NaiveBayesClassifier<>(outCol, lnLikes, Collections.emptyList());
	}
	
	protected double[] ln(double[] probs) {
		double lnSum = Math.log(Arrays.stream(probs).sum());
		return Arrays.stream(probs).map(v -> Math.log(v) - lnSum).toArray();
	}

}
