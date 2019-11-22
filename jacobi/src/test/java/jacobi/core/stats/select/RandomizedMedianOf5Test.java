package jacobi.core.stats.select;

import org.junit.Assert;
import org.junit.Test;

public class RandomizedMedianOf5Test {
	
	@Test
	public void shouldBeAbleToSelectMedianOf5() {
		Assert.assertEquals(2, 
			new RandomizedMedianOf5(n -> n).select(new double[] {1.0, 2.0, 3.0, 4.0, 5.0}, 
				0, 1, 2, 3, 4)
		);
		
		Assert.assertEquals(2, 
			new RandomizedMedianOf5(n -> n).select(new double[] {1.0, 2.0, 3.0, 4.0, 5.0}, 
				4, 3, 2, 1, 0)
		);
	}
	
	@Test
	public void shouldBeAbleToSelectMedianOf5InAllPermutations() {		
		// ...
	}

}
