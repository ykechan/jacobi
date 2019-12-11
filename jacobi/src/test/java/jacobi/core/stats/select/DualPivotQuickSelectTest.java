package jacobi.core.stats.select;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

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
		long seed = Double.doubleToLongBits(123 * Math.sqrt(Math.E * Math.PI));
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
				
				if(target - begin < 4 || end - 1 - target < 4) {
					System.out.println("Short-cut:" + depth + " in [" + begin + "," + end + ")");
				}
				
				if(target - begin < 16 || end - 1 - target < 16) {
					System.out.println("Heap:" + depth + " in [" + begin + "," + end + ")");
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
	
	@Test
	public void shouldBeAbleToDetectManyMinElements() {
		double[] seq = new double[65536];
		Arrays.fill(seq, 49.99);
		seq[60000] = 50.0;
		
		double[] temp = Arrays.copyOf(seq, seq.length);
		AtomicInteger max = new AtomicInteger(0);
		
		int target = 32768;
		
		int ans = new DualPivotQuickSelect((a, i, j, t) -> i, (a, i, j, t) -> j - 1) {

			@Override
			public int select(double[] items, int begin, int end, int target, int depth) {
				if(depth > max.get()) {
					max.set(depth);
				}
				
				return super.select(items, begin, end, target, depth);
			}
			
		}.select(temp, 0, temp.length, target);
		
		Assert.assertEquals(target, ans);
		Assert.assertEquals(0, max.get());
	}
	
	@Test
	public void shouldBeAbleToSelectFromLongSeqOfFewDistinctValues() {
		double[] enums = {3.6, 7.8, 9.9, 9.999};
		
		double[] seqs = IntStream.range(0, 2 * 65536)
				.map(i -> Math.abs(i))
				.mapToDouble(i -> enums[i % enums.length])
				.toArray();
		double[] temp = Arrays.copyOf(seqs, seqs.length);
		int target = 32768;
		AtomicInteger max = new AtomicInteger(0);
		int ans = new DualPivotQuickSelect((a, i, j, t) -> i, (a, i, j, t) -> j - 1) {

			@Override
			public int select(double[] items, int begin, int end, int target, int depth) {
				if(depth > max.get()) {
					max.set(depth);
				}
				
				return super.select(items, begin, end, target, depth);
			}
			
		}.select(temp, 0, temp.length, target);
		
		Assert.assertEquals(7.8, temp[target], 1e-12);
		Assert.assertTrue(max.get() >= 0 && max.get() < 4);
		
		temp = Arrays.copyOf(seqs, seqs.length);
		target = 32767;
		max.set(-1);
		ans = new DualPivotQuickSelect((a, i, j, t) -> i, (a, i, j, t) -> j - 1) {

			@Override
			public int select(double[] items, int begin, int end, int target, int depth) {
				if(depth > max.get()) {
					max.set(depth);
				}
				
				return super.select(items, begin, end, target, depth);
			}
			
		}.select(temp, 0, temp.length, target);
		
		Assert.assertEquals(3.6, temp[target], 1e-12);
		Assert.assertTrue(max.get() >= 0 && max.get() < 4);
	}
	
	@Test
	public void shouldDegenerateToQuadraticTimeWhenBadPivotsAreConstantlyUsed() {
		double[] temp = IntStream.range(0, 100).mapToDouble(i -> 1000.0 - Math.PI * i).toArray();
		AtomicInteger max = new AtomicInteger(0);
		int ans = new DualPivotQuickSelect(this::findMin, this::findMax) {
			
			@Override
			public int select(double[] items, int begin, int end, int target, int depth) {
				if(depth > max.get()) {
					max.set(depth);
				}
				
				return super.select(items, begin, end, target, depth);
			}
			
		}.select(temp, 0, temp.length, 50);
		Assert.assertEquals(1000.0 - Math.PI * 50.0, temp[ans], 1e-12);
		Assert.assertEquals(49, max.get());
	}
	
	protected Select random(long seed) {
		Random rand = seed == 0 ? new Random() : new Random(seed);
		return (a, i, j, t) -> i + rand.nextInt(j - i);
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
