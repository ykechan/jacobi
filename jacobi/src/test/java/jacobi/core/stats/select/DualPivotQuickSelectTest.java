package jacobi.core.stats.select;

import java.util.Arrays;

import org.junit.Test;

public class DualPivotQuickSelectTest {
	
	@Test
	public void shouldBeSeparateSequenceBetween2Pivots() {
		double[] array = {8.9, 13.2, 5.3, 5.4, 4.4, -100.0, 10.0, 0.1, 3.14};
		new DualPivotQuickSelect(
			(arr, i, j, t) -> {
				
				return t;
			}, 
			(arr, i, j, t) -> 0,
			(arr, i, j, t) -> 1
		).select(array, 0, array.length, array.length / 2);
		
		System.out.println(Arrays.toString(array));
	}

}
