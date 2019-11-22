package jacobi.core.stats.select;

import java.util.Arrays;
import java.util.function.IntUnaryOperator;

public class RandomizedMedianOf5 implements Select {
	
	public RandomizedMedianOf5(IntUnaryOperator rand) {
		this.rand = rand;
	}

	@Override
	public int select(double[] items, int begin, int end, int target) {
		
		return this.select(items, 
			begin, end - 1, (begin + end) / 2, 
			begin + this.rand.applyAsInt(end - begin),
			begin + this.rand.applyAsInt(end - begin)
		);
	}
	
	protected int select(double[] items, int... pivots) {
		if(pivots.length != 5) {
			throw new UnsupportedOperationException();
		}

		int[] min = {-1, -1, -1};
		for(int i = 0; i < pivots.length; i++){
			if(min[0] < 0 || items[min[0]] > items[i]) {
				min[2] = min[1]; min[1] = min[0]; min[0] = i;
				continue;
			}
			
			if(min[1] < 0 || items[min[1]] > items[i]) {
				min[2] = min[1]; min[1] = i;
				continue;
			}
			
			if(min[2] < 0 || items[min[2]] > items[i]) {
				min[2] = i;
			}
		}
		 
		return min[2];
	}

	private IntUnaryOperator rand;
}
