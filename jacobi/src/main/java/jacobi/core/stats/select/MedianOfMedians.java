package jacobi.core.stats.select;

import java.util.function.Supplier;

public class MedianOfMedians implements Select {
	
	public MedianOfMedians(Select selector) {
		this.selector = selector;
	}

	@Override
	public int select(double[] items, int begin, int end, int target) {
		if(end - begin < 6){
			return this.select(items, begin, end, target);
		}
		return begin;
	}
	
	protected void medianToFront(double[] items, int begin, int end) {
		for(int i = begin; i < end; i += 5) {
			int length = Math.min(end - i, 5);
			if(length < 3) {
				// meaningful median doesn't exists for 1 or 2 items
				continue;
			}
			int median = this.selector
				.select(items, i, i + length, i + length / 2);
			this.swap(items, i, median);
		}
	}
	
	protected int groupMedians(double[] items, int begin, int end) {
		int k = begin + 1;
		
		for(int i = begin + 5; i < end; i++) {
			this.swap(items, i, k++);
		}
		
		return k;
	}
	
	private Select selector;
}
