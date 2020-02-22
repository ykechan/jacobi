package jacobi.core.classifier.ensemble;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import jacobi.api.classifier.ClassifierLearner;
import jacobi.core.classifier.cart.node.Decision;

public class BaggingLearnerTest {
	
	@Test
	public void shouldBeAbleToComputeCosineBetweenTwoItemVectors() {
		Map<Boolean, Integer> left = new HashMap<>();
		left.put(Boolean.FALSE, 1);
		left.put(Boolean.TRUE, 2);
		
		Map<Boolean, Integer> right = new HashMap<>();
		right.put(Boolean.FALSE, 3);
		right.put(Boolean.TRUE, 4);
		
		double cos = new BaggingLearner<>(this.mock(Boolean.FALSE), () -> 0.0).cosine(left, right);
		// equiv to cos([1, 2]^T, [4, 6]^T) = (1*4 + 2*6) / sqrt[(1^2  + 2^2) * (4^2 + 6^2)]  		
		Assert.assertEquals(
			(1.0 * 4.0 + 2.0 * 6.0) 
			/ Math.sqrt((1 * 1 + 2 * 2) * (4 * 4 + 6 * 6)), cos, 1e-12);
	}
	
	@Test
	public void shouldBeAbleToComputeCosineBetweenTwoItemVectorsThatAreMutuallyExclusive() {
		Map<Boolean, Integer> left = new HashMap<>();
		left.put(Boolean.FALSE, 2);
		
		
		Map<Boolean, Integer> right = new HashMap<>();
		right.put(Boolean.TRUE, 1);
		
		double cos = new BaggingLearner<>(this.mock(Boolean.FALSE), () -> 0.0).cosine(left, right);
		// equiv to cos([0, 2]^T, [1, 2]^T) = (0*1 + 2*2) / sqrt[(0  + 2^2) * (1^2 + 2^2)]
		Assert.assertEquals(
			(0 * 1 + 2 * 2) / Math.sqrt((0 * 0 + 2 * 2) * (1 * 1 + 2 * 2)), 
			cos, 1e-12);
	}
	
	protected <T> ClassifierLearner<T, Decision<T>, Void> mock(T ans) {
		return (data, param) -> new Decision<>(ans);
	}

}
