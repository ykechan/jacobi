package jacobi.core.stats.select;

import org.junit.Assert;
import org.junit.Test;

public class ExtremaSelectTest {
	
	@Test
	public void shouldBeAbleToSelectMedianOf5() {
		Assert.assertEquals(2, 
			new ExtremaSelect().select(new double[] {1.0, 2.0, 3.0, 4.0, 5.0}, 0, 5, 2));
		Assert.assertEquals(0, 
			new ExtremaSelect().select(new double[] {5.0, 7.0, 9.0, 3.0, 1.0}, 0, 5, 2));
		Assert.assertEquals(1, 
			new ExtremaSelect().select(new double[] {11.7, 13.8, 0.0, 99.9, 50.0}, 0, 5, 2));
		Assert.assertEquals(3, 
			new ExtremaSelect().select(new double[] {99.0, 83.0, -13.9, 0.0, -58.8}, 0, 5, 2));
		Assert.assertEquals(4, 
			new ExtremaSelect().select(new double[] {-99, -88, 99, 88, 1}, 0, 5, 2));
	}
	
	@Test
	public void shouldBeAbleToSelectMinimumInSortedSeq() {
		Assert.assertEquals(0, 
			new ExtremaSelect().select(new double[] {1.0, 2.0, 3.0, 4.0, 5.0}, 0, 5, 0));
		Assert.assertEquals(1, 
			new ExtremaSelect().select(new double[] {1.0, 2.0, 3.0, 4.0, 5.0}, 0, 5, 1));
	}
	
	@Test
	public void shouldBeAbleToSelectMinimumInRandomSeq() {
		Assert.assertEquals(2, 
			new ExtremaSelect().select(new double[] {77.0, 89, -111, 3, 567.23, 76.666, Math.PI}, 0, 7, 0));
	}
	
	@Test
	public void shouldBeAbleToSelectMinimumInRandomSeqRange() {
		Assert.assertEquals(3, 
			new ExtremaSelect().select(new double[] {77.0, 89, -111, 3, 567.23, 76.666, Math.PI}, 3, 7, 3));
		Assert.assertEquals(6, 
			new ExtremaSelect().select(new double[] {77.0, 89, -111, 3, 567.23, 76.666, Math.PI}, 3, 7, 4));
	}
	
	@Test
	public void shouldBeAbleToSelectMaximumInSortedSeq() {
		Assert.assertEquals(4, 
			new ExtremaSelect().select(new double[] {1.0, 2.0, 3.0, 4.0, 5.0}, 0, 5, 4));
		Assert.assertEquals(3, 
			new ExtremaSelect().select(new double[] {1.0, 2.0, 3.0, 4.0, 5.0}, 0, 5, 3));
	}
	
	@Test
	public void shouldBeAbleToSelectMaximumInRandomSeq() {
		Assert.assertEquals(4, 
			new ExtremaSelect().select(new double[] {77.0, 89, -111, 3, 567.23, 76.666, Math.PI}, 0, 7, 6));
	}
	
	@Test
	public void shouldBeAbleToSelectMaximumInRandomSeqRange() {
		Assert.assertEquals(4, 
			new ExtremaSelect().select(new double[] {77.0, 89, -111, 3, 567.23, 76.666, Math.PI}, 1, 6, 5));
		Assert.assertEquals(1, 
			new ExtremaSelect().select(new double[] {77.0, 89, -111, 3, 567.23, 76.666, Math.PI}, 1, 6, 4));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldFailIfTargetBeforeRangeBegin() {
		new ExtremaSelect().select(new double[] {}, 1, 6, 0);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldFailIfTargetAfterRangeEnd() {
		new ExtremaSelect().select(new double[] {}, 1, 6, 6);
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void shouldFailIfTargetIsNotExtrema() {
		new ExtremaSelect().select(new double[] {}, 0, 10, 3);
	}

}
