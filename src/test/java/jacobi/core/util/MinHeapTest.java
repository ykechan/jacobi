package jacobi.core.util;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;

public class MinHeapTest {
	
	@Test
	public void shouldBeAbleToFindKMaxItemsFromRandomWeights() {
		double[] array = new double[] {78, 3.33, Math.PI, 10.0, 123.99, Math.E, 45.0, 3.0, 70.98};
		
		Enque<Weighted<Integer>> enque = new MinHeap(4, 0);
		for(int i = 0; i < array.length; i++){
			enque.push(new Weighted<>(i, array[i]));
		}
		
		Assert.assertEquals(6, enque.pop().item.intValue());
		Assert.assertEquals(8, enque.pop().item.intValue());
		Assert.assertEquals(0, enque.pop().item.intValue());
		Assert.assertEquals(4, enque.pop().item.intValue());
		
		Assert.assertTrue(enque.isEmpty());
	}
	
	@Test
	public void shouldBeAbleToExtractMin() {
		Enque<Weighted<Integer>> enque = new MinHeap(4, 2)
			.push(new Weighted<>(0, 7.0))
			.push(new Weighted<>(1, 4.0))
			.push(new Weighted<>(2, 5.0))
			.push(new Weighted<>(3, 9.0))
			.push(new Weighted<>(4, 2.0))
			.push(new Weighted<>(5, 1.0))
			.push(new Weighted<>(6, 1.1));
		
		Weighted<Integer> entry = enque.pop();
		Assert.assertEquals(5, entry.item.intValue());
		Assert.assertEquals(1.0, entry.weight, 1e-12);
	}
	
	@Test
	public void shouldBeAbleToSortEntryInAscOrder() {
		Enque<Weighted<Integer>> enque = new MinHeap(4, 2)
				.push(new Weighted<>(0, 7.0))
				.push(new Weighted<>(1, 4.0))
				.push(new Weighted<>(2, 5.0))
				.push(new Weighted<>(3, 9.0))
				.push(new Weighted<>(4, 2.0))
				.push(new Weighted<>(5, 1.0))
				.push(new Weighted<>(6, 1.1));
		
		Weighted<Integer> entry = enque.pop();
		Assert.assertEquals(5, entry.item.intValue());
		Assert.assertEquals(1.0, entry.weight, 1e-12);
		
		entry = enque.pop();
		Assert.assertEquals(6, entry.item.intValue());
		Assert.assertEquals(1.1, entry.weight, 1e-12);
		
		entry = enque.pop();
		Assert.assertEquals(4, entry.item.intValue());
		Assert.assertEquals(2.0, entry.weight, 1e-12);
		
		entry = enque.pop();
		Assert.assertEquals(1, entry.item.intValue());
		Assert.assertEquals(4.0, entry.weight, 1e-12);
		
		entry = enque.pop();
		Assert.assertEquals(2, entry.item.intValue());
		Assert.assertEquals(5.0, entry.weight, 1e-12);
		
		entry = enque.pop();
		Assert.assertEquals(0, entry.item.intValue());
		Assert.assertEquals(7.0, entry.weight, 1e-12);
		
		entry = enque.pop();
		Assert.assertEquals(3, entry.item.intValue());
		Assert.assertEquals(9.0, entry.weight, 1e-12);
	}
	
	@Test
	public void test() {
		double a = Double.POSITIVE_INFINITY;
		double b = Double.POSITIVE_INFINITY;
		
		System.out.println(a);
		System.out.println(a + b);
		System.out.println(2 * b);
		System.out.println(-2 * b);
		System.out.println(Math.min(3.0, b));
	}

}
