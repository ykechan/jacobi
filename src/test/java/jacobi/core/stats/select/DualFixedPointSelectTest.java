package jacobi.core.stats.select;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntBinaryOperator;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;

public class DualFixedPointSelectTest {
	
	@Test
	public void shouldBeAbleToUseExtremaSelectWhenCloseToTarget() {
		AtomicInteger counter = new AtomicInteger(0);
		double[] seq = {8.9, 13.2, 5.3, 5.4, 4.4, -100.0, 10.0, 0.1, 3.14};
		Select selector = new DualFixedPointSelect(
				this::unsupported,
				this::unsupported,
				(a, i, j, t) -> { 
					Arrays.sort(a, i, j); 
					counter.incrementAndGet(); 
					return t; 
				},
				this::unsupported
		);
		
		double[] temp = Arrays.copyOf(seq, seq.length);
		Assert.assertEquals(1, selector.select(temp, 0, temp.length, 1));
		Assert.assertEquals(1, counter.get());
		
		temp = Arrays.copyOf(seq, seq.length);
		Assert.assertEquals(2, selector.select(temp, 0, temp.length, 2));
		Assert.assertEquals(2, counter.get());
		
		temp = Arrays.copyOf(seq, seq.length);
		Assert.assertEquals(0, selector.select(temp, 0, temp.length, 0));
		Assert.assertEquals(3, counter.get());
		
		temp = Arrays.copyOf(seq, seq.length);
		Assert.assertEquals(temp.length - 1, selector.select(temp, 0, temp.length, temp.length - 1));
		Assert.assertEquals(4, counter.get());
		
		temp = Arrays.copyOf(seq, seq.length);
		Assert.assertEquals(temp.length - 2, selector.select(temp, 0, temp.length, temp.length - 2));
		Assert.assertEquals(5, counter.get());
		
		temp = Arrays.copyOf(seq, seq.length);
		Assert.assertEquals(temp.length - 3, selector.select(temp, 0, temp.length, temp.length - 3));
		Assert.assertEquals(6, counter.get());
	}
	
	@Test
	public void shouldBeAbleToUseFixNextPointIfExtremaSelectIsNotUsed() {
		double[] seq = {8.9, 13.2, 5.3, 5.4, 4.4, -100.0, 10.0, 0.1, 3.14};
		// -100, 0.1, 3.14, 4.4, 5.3, 5.4, 8.9, 10.0, 13.2
		int target = new DualFixedPointSelect(
				(a, i, j, t) -> 2,
				(a, i, j, t) -> j - 1,
				this::unsupported,
				this::unsupported
		).select(seq, 0, seq.length, 4);
		
		Assert.assertEquals(4, target);
		Assert.assertEquals(5.3, seq[target], 1e-12);
		Assert.assertEquals(5.4, seq[target + 1], 1e-12);
	}
	
	@Test
	public void shouldBeAbleToFixNextPointIfExtremaSelectIsUsedOnLastMinima() {
		double[] seq = {8.9, 13.2, 5.3, 5.4, 4.4, -100.0, 10.0, 0.1, 3.14};
		// -100, 0.1, 3.14, 4.4, 5.3, 5.4, 8.9, 10.0, 13.2
		int target = new DualFixedPointSelect(
				this::unsupported,
				this::unsupported,
				this::minima,
				this::unsupported
		).select(seq, 0, seq.length, 2);
		
		Assert.assertEquals(2, target);
		Assert.assertEquals(3.14, seq[target], 1e-12);
		Assert.assertEquals(4.4, seq[target + 1], 1e-12);
	}
	
	@Test
	public void shouldBeAbleToFallBackWhenDepthIsExhaused() {
		AtomicInteger counter = new AtomicInteger(0);
		
		double[] seq = IntStream.range(0, 32)
			.mapToDouble(i -> i % 2 == 0 ? 32.0 - i : -i)
			.toArray();
		Select selector = new DualFixedPointSelect(
			this::minimum,
			this::maximum,
			this::unsupported,
			(a, i, j, t) -> {
				counter.incrementAndGet();
				return t;
			}
		) {

			@Override
			public int select(double[] items, int begin, int end, int target, int depth) {
				//System.out.println("depth = " + depth + ", [" + begin + "," + end + ").");
				return super.select(items, begin, end, target, depth);
			}
			
		};
		
		selector.select(seq, 0, seq.length, 16);
		Assert.assertEquals(1, counter.get());
	}
	
	protected int minimum(double[] items, int begin, int end, int target) {
		return IntStream.range(begin, end)
			.reduce((a, b) -> items[a] < items[b] ? a : b)
			.orElseThrow(() -> new IllegalArgumentException());
	}
	
	protected int maximum(double[] items, int begin, int end, int target) {
		return IntStream.range(begin, end)
			.reduce((a, b) -> items[a] > items[b] ? a : b)
			.orElseThrow(() -> new IllegalArgumentException());
	}
	
	protected int unsupported(double[] items, int begin, int end, int target) {
		throw new UnsupportedOperationException();
	}
	
	protected int minima(double[] items, int begin, int end, int target) {
		if(target - begin > 2) {
			throw new UnsupportedOperationException();
		}
		
		IntBinaryOperator swap = (a, b) -> { 
			double tmp = items[a]; items[a] = items[b]; items[b] = tmp; 
			return a; 
		};
		
		swap.applyAsInt(this.minimum(items, begin, end, target), begin);
		swap.applyAsInt(this.minimum(items, begin + 1, end, target), begin + 1);
		swap.applyAsInt(this.minimum(items, begin + 2, end, target), begin + 2);
		return target;
	}

}
