package jacobi.core.stats.select;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;

public class AdaptiveSelectTest {
	
	@Test
	public void shouldBeAbleToUseExtremaSelectForExtrema() {
		AtomicInteger count = new AtomicInteger(900);
		Select selector = new AdaptiveSelect(1000, 1000, 
			(arr, i, j, t) -> count.incrementAndGet(), 
			n -> 0
		);
		
		Assert.assertEquals(901, selector.select(new double[100], 0, 100, 0));
		Assert.assertEquals(902, selector.select(new double[100], 0, 100, 1));
		Assert.assertEquals(903, selector.select(new double[100], 0, 100, 2));
		
		Assert.assertEquals(904, selector.select(new double[100], 0, 100, 99));
		Assert.assertEquals(905, selector.select(new double[100], 0, 100, 98));
		Assert.assertEquals(906, selector.select(new double[100], 0, 100, 97));
		
		Assert.assertEquals(906, count.get());
	}
	
	@Test
	public void shouldBeAbleToSearchByHeap() {
		AtomicInteger count = new AtomicInteger(0);
		Select selector = new AdaptiveSelect(1000, -10.0, (arr, a, b, t) -> a, n -> 0) {

			@Override
			protected int selectByHeap(double[] items, int begin, int end, int target) {
				count.incrementAndGet();
				return super.selectByHeap(items, begin, end, target);
			}
			
		};
		
		double[] seq = new Random(Double.doubleToLongBits(Math.E)).doubles()
				.map(v -> 100 * v - 50.0)
				.limit(100)
				.toArray();
		double[] temp = Arrays.copyOf(seq, seq.length);
		int ans = selector.select(temp, 0, temp.length, 4);
		Assert.assertArrayEquals(seq, temp, 1e-12);
		Arrays.sort(temp);
		Assert.assertEquals(temp[4], seq[ans], 1e-12);
		Assert.assertEquals(1, count.get());
		
		seq = new Random(Double.doubleToLongBits(Math.PI)).doubles()
				.map(v -> 100 * v - 50.0)
				.limit(100)
				.toArray();
		temp = Arrays.copyOf(seq, seq.length);
		
		ans = selector.select(temp, 0, temp.length, 6);
		Assert.assertArrayEquals(seq, temp, 1e-12);
		Arrays.sort(temp);
		Assert.assertEquals(temp[6], seq[ans], 1e-12);
		Assert.assertEquals(2, count.get());
	}
	
	@Test
	public void shouldBeAbleToSelectTheSameElementAsSorted() {
		double[] seq = new Random(Double.doubleToLongBits(999 * Math.E))
				.doubles().map(v -> 1000.0 * v).limit(2 * 65536).toArray();
		
		double[] oracle = Arrays.copyOf(seq, seq.length);
		Arrays.sort(oracle);
		
		Select selector = AdaptiveSelect.of(32, 2.0);
		
		new Random(Double.doubleToLongBits(Math.PI))
			.ints().limit(128).map(i -> Math.abs(i)).map(i -> i % oracle.length)
			.forEach(i -> {
				double[] temp = Arrays.copyOf(seq, seq.length);
				int ans = selector.select(temp, 0, temp.length, i);
				
				Assert.assertEquals(oracle[i], temp[ans], 1e-12);
			});
	}
	
	protected int findMin(double[] a, int i, int j, int t) {
		return IntStream.range(i, j)
			.reduce( (u, v) -> a[u] < a[v] ? u : v )
			.orElseThrow(() -> new IllegalArgumentException());
	}
	
	protected int findMax(double[] a, int i, int j, int t) {
		return IntStream.range(i, j)
			.reduce( (u, v) -> a[u] > a[v] ? u : v )
			.orElseThrow(() -> new IllegalArgumentException());
	}

}
