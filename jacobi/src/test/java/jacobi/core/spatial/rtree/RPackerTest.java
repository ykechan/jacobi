package jacobi.core.spatial.rtree;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class RPackerTest {
	
	@Test
	public void shouldStopWhenVolumeDeltaIsLargerThanAcceptance() {
		List<double[]> points = Arrays.asList(
			new double[] {0.0, 0.0},
			new double[] {1.0, 1.0},
			new double[] {1.1, 1.1},
			new double[] {2.2, 2.2},
			new double[] {10.0, 10.0},
			new double[] {999.0, 999.0}
		);
		
		int length = new RPacker().accept(points, 1, 4, () -> 1.0);
		Assert.assertEquals(3, length);
	}
	
	@Test
	public void shouldAcceptWhenVolumeDeltaIsSmallerThanAcceptance() {
		List<double[]> points = Arrays.asList(
			new double[] {0.0, 0.0},
			new double[] {1.0, 1.0},
			new double[] {1.1, 1.1},
			new double[] {1.101, 1.101},
			new double[] {10.0, 10.0},
			new double[] {999.0, 999.0}
		);
		
		int length = new RPacker().accept(points, 1, 4, () -> 1.0);
		Assert.assertEquals(4, length);
	}

	@Test
	public void shouldAcceptanceProbilityBeDecreasingWithPosition() {
		int min = 100;
		int max = 130;
		
		RPacker packer = new RPacker();
		
		for(int i = min + 1; i < max; i++) {
			Assert.assertTrue(
				  packer.acceptProb(min, max, i)
				> packer.acceptProb(min, max, i + 1)
			);
		}
	}
	
	@Test
	public void shouldAcceptanceProbilityBeZeroPointFiveInTheMiddle() {
		int min = 1;
		int max = 5;
		
		Assert.assertEquals(0.5, new RPacker().acceptProb(min, max, 3), 1e-8);
	}

}
