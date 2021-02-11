package jacobi.core.clustering;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

public class ParzenWindowTest {
	
	@Test
	public void shouldFlatKernelReturnNormalizedWeights() {
		double[] weights = ParzenWindow.FLAT.apply(new double[]{1.0, 2.0, 3.0}, Arrays.asList(
			new double[]{1.0, 2.0, 3.0},
			new double[]{1.0, 2.0, 3.0},
			new double[]{1.0, 2.0, 3.0},
			new double[]{1.0, 2.0, 3.0}
		));
		
		Assert.assertArrayEquals(new double[]{0.25, 0.25, 0.25, 0.25}, weights, 1e-12);
	}
	
	@Test
	public void shouldGaussKernelReturnNormalizedWeights() {
		double[] weights = ParzenWindow.gauss(1.0).apply(new double[]{0.0}, Arrays.asList(
			new double[]{1.0},
			new double[]{2.0},
			new double[]{3.0}
		));
		
		double total = Math.exp(-1.0) + Math.exp(-4.0) + Math.exp(-9.0);
		Assert.assertArrayEquals(new double[]{
			Math.exp(-1.0) / total,
			Math.exp(-4.0) / total,
			Math.exp(-9.0) / total
		}, weights, 1e-8);
	}

}
