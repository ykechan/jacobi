package jacobi.core.graph;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

public class AdjMatrixTest {
	
	@Test
	public void shouldBeAbleToEncodeNonZeroList() {
		Assert.assertArrayEquals(new int[] {0, 1, 2, 3, 4, 6, 7, 8}, AdjMatrix.scatters(new double[] {
			1.0, 2.0, 3.0, 4.0, 5.0, 0.0, 6.0, 7.0, 8.0
		}, new int[100]));
		
		Assert.assertArrayEquals(new int[] {0, 1, 2, 3, 7, 8}, AdjMatrix.scatters(new double[] {
			1.0, 2.0, 3.0, 4.0, 0.0, 0.0, 0.0, 7.0, 8.0
		}, new int[100]));
		
		Assert.assertArrayEquals(new int[] {0, 2, 4}, AdjMatrix.scatters(new double[] {
			1.0, 0.0, 2.0, 0.0, 3.0
		}, new int[100]));
	}
	
	@Test
	public void shouldBeAbleToEncodeContinuousList() {
		/*
		Assert.assertArrayEquals(new int[] {-1, 0, 2, 3, 3}, (
			AdjMatrix.continuous(new double[] {
				2.0, 3.0, 0.0, 4.0, 5.0, 6.0
			}, 
			new int[100])
		));
		*/
		System.out.println(Arrays.toString(AdjMatrix.clumps(new double[] {
				2.0, 3.0, 0.0, 4.0, 5.0, 6.0
			}, 
			new int[100])));
		
		System.out.println(Arrays.toString(AdjMatrix.clumps(new double[] {
				0.0, 0.0, 0.0, 2.0, 3.0, 0.0, 4.0, 5.0, 6.0, 0.0, 0.0, 0.0
			}, 
			new int[100])));
	}	

}
