package jacobi.core.stats.select;

import java.util.Arrays;

public class QuickSelect implements Select {
	
	public QuickSelect(Select pivotselect) {
		this.pivotselect = pivotselect;
	}

	@Override
	public int select(double[] items, int begin, int end, int target) {
		int pivot =  this.pivotselect.select(items, begin, end, target);		
		double value = items[pivot];
		
		System.out.println(Arrays.toString(items) + ", pivot = " + pivot + " (" + value + ") in "
				+ "[" + begin + "," + end + ").");
		
		this.swap(items, pivot, end - 1);
		int i = begin;
		for(int j = begin; j < end; j++){
			if(items[j] < value){
				this.swap(items, i++, j);
			}
		}
		
		if(i == begin && Arrays.stream(items, begin, end).noneMatch(v -> v > value)){
			// all values are the same
			return target;
		}
		
		this.swap(items, end - 1, i);
		return i == target 
			? i 
			: this.select(items, i < target ? i + 1 : begin, i < target ? end : i, target);
	}

	private Select pivotselect;
}
