package jacobi.core.stats.select;

import java.util.PriorityQueue;

import org.junit.Test;

public class AdaptiveSelectTest {
	
	@Test
	public void test() {
		PriorityQueue<Double> temp = new PriorityQueue<>();
		temp.offer(3.0);
		temp.offer(9.0);
		temp.offer(6.0);
		System.out.println(temp.peek());
		System.out.println(temp.remove());
	}

}
