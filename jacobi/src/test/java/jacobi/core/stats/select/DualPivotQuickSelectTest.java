package jacobi.core.stats.select;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

public class DualPivotQuickSelectTest {
	
	@Test
	public void shouldBeSeparateSequenceBetween2Pivots() {
		double[] array = {8.9, 13.2, 5.3, 5.4, 4.4, -100.0, 10.0, 0.1, 3.14};
		new DualPivotQuickSelect(
			(arr, i, j, t) -> 0,
			(arr, i, j, t) -> 1
		).select(array, 0, array.length, array.length / 2);
		
		System.out.println(Arrays.toString(array));
	}
	
	@Test
	public void shouldBeAbleToSelectIn65536RandomSequence() {
		long seed = Double.doubleToLongBits(Math.sqrt(Math.E * Math.PI));
		double[] seq = new Random(seed).doubles().limit(65536).map(v -> 100 * v - 30.0)
				.toArray();
		double[] temp = Arrays.copyOf(seq, seq.length);
		
		AtomicInteger max = new AtomicInteger(0);
		
		int target = 32768;
		int ans = new DualPivotQuickSelect(this.random(5728356783L), this.random(517832564178573285L)) {

			@Override
			public int select(double[] items, int begin, int end, int target, int depth) {
				if(depth > max.get()) {
					max.set(depth);
				}
				return super.select(items, begin, end, target, depth);
			}
			
		}.select(temp, 0, temp.length, target);
		
		Assert.assertEquals(target, ans);
		
		double value = temp[ans];
		Arrays.sort(seq);
		System.out.println(max.get());
		Assert.assertEquals(seq[target], value, 1e-12);
	}
	
	protected Select random(long seed) {
		Random rand = seed == 0 ? new Random() : new Random(seed);
		return (a, i, j, t) -> i + rand.nextInt(j - i);
	}

}
