package jacobi.core.stats.select;

public interface Select {
	
	public int select(double[] items, int begin, int end, int target);
	
	public default void swap(double[] items, int i, int j) {
		if(i == j){
			return;
		}
		
		double temp = items[i];
		items[i] = items[j];
		items[j] = temp;
	}

}
