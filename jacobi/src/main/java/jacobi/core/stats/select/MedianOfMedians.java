package jacobi.core.stats.select;

import java.util.function.Supplier;

public class MedianOfMedians implements Select {

	@Override
	public int select(double[] items, int begin, int end, int target) {
		// ...
		return 0;
	}
	
	private Supplier<Select> selectorFactory;
}
