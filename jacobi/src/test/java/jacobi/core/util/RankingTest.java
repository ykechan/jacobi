package jacobi.core.util;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntToDoubleFunction;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import jacobi.api.Matrix;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.util.JacobiJUnit4ClassRunner;

@JacobiImport("/jacobi/test/data/RankingTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class RankingTest {
	
	@JacobiInject(0)
	public Matrix input;
	
	@Test
	@JacobiImport("test random 10x5")
	public void testShouldBeAbleToInitRanking() {
		double[] buffer = new double[2 * this.input.getRowCount()];
		Ranking ranking = new Ranking(buffer, n -> 0, 0).init(i -> this.input.get(i, 0));
		
		Assert.assertArrayEquals(IntStream
			.range(0, this.input.getRowCount())
			.toArray(), ranking.toArray());
		
		for(int i = 0; i < buffer.length; i += 2) {
			Assert.assertEquals(this.input.get(i / 2, 0), buffer[i], 1e-12);
		}
	}
	
	@Test
	@JacobiImport("test random 10x5")
	public void testShouldBeAbleToSelectMedianOf5() {		
		double[] buffer = new double[2 * this.input.getRowCount()];
		AtomicInteger count = new AtomicInteger(0);
		int[] pivots = new Ranking(buffer, n -> 2, 0)
				.init(i -> this.input.get(i, 0))
				.select(n -> count.incrementAndGet(), 0, buffer.length / 2);
		Set<Double> values = new TreeSet<>();
		values.add(this.input.get(0, 0));
		values.add(this.input.get(this.input.getRowCount() / 2, 0));
		values.add(this.input.get(this.input.getRowCount() - 1, 0));
		values.add(this.input.get(1, 0));
		values.add(this.input.get(2, 0));
		
		Assert.assertEquals(values.toArray(new Double[0])[1], this.input.get(pivots[0], 0), 1e-12);
		Assert.assertEquals(values.toArray(new Double[0])[3], this.input.get(pivots[1], 0), 1e-12);
		
		count.set(0);
		pivots = new Ranking(buffer, n -> 3, 0).init(i -> this.input.get(i, 1))
				.select(n -> count.incrementAndGet() == 1 ? 3 : 8, 0, buffer.length / 2);
		values = new TreeSet<>();
		values.add(this.input.get(0, 1));
		values.add(this.input.get(this.input.getRowCount() / 2, 1));
		values.add(this.input.get(this.input.getRowCount() - 1, 1));
		values.add(this.input.get(3, 1));
		values.add(this.input.get(8, 1));

		Assert.assertEquals(values.toArray(new Double[0])[1], this.input.get(pivots[0], 1), 1e-12);
		Assert.assertEquals(values.toArray(new Double[0])[3], this.input.get(pivots[1], 1), 1e-12);
		
		count.set(0);
		pivots = new Ranking(buffer, n -> 3, 0).init(i -> this.input.get(i, 2))
				.select(n -> count.incrementAndGet() == 1 ? 1 : 6, 0, buffer.length / 2);
		values = new TreeSet<>();
		values.add(this.input.get(0, 2));
		values.add(this.input.get(this.input.getRowCount() / 2, 2));
		values.add(this.input.get(this.input.getRowCount() - 1, 2));
		values.add(this.input.get(1, 2));
		values.add(this.input.get(6, 2));
		
		Assert.assertEquals(values.toArray(new Double[0])[1], this.input.get(pivots[0], 2), 1e-12);
		Assert.assertEquals(values.toArray(new Double[0])[3], this.input.get(pivots[1], 2), 1e-12);
	}
	
	@Test
	@JacobiImport("test random 10x5")
	public void testShouldBeAbleToSortByHeapSort() {
		double[] buf = new double[2 * this.input.getRowCount()];
		
		Ranking ranking = new Ranking(buf, n -> 2, 0).init(i -> this.input.get(i, 0));
		ranking.heapsort(0, this.input.getRowCount());
		
		for(int i = 2; i < buf.length; i += 2) {
			Assert.assertTrue(buf[i] >= buf[i - 2]);
		}
		
		Arrays.fill(buf, 0.0);
		ranking = new Ranking(buf, n -> 2, 0).init(i -> this.input.get(i, 1));
		ranking.heapsort(0, this.input.getRowCount());
		
		for(int i = 2; i < buf.length; i += 2) {
			Assert.assertTrue(buf[i] >= buf[i - 2]);
		}
		
		Arrays.fill(buf, 0.0);
		ranking = new Ranking(buf, n -> 2, 0).init(i -> this.input.get(i, 2));
		ranking.heapsort(0, this.input.getRowCount());
		
		for(int i = 2; i < buf.length; i += 2) {
			Assert.assertTrue(buf[i] >= buf[i - 2]);
		}
		
		Arrays.fill(buf, 0.0);
		ranking = new Ranking(buf, n -> 2, 0).init(i -> this.input.get(i, 3));
		ranking.heapsort(0, this.input.getRowCount());
		
		for(int i = 2; i < buf.length; i += 2) {
			Assert.assertTrue(buf[i] >= buf[i - 2]);
		}
		
		Arrays.fill(buf, 0.0);
		ranking = new Ranking(buf, n -> 2, 0).init(i -> this.input.get(i, 4));
		ranking.heapsort(0, this.input.getRowCount());
		
		for(int i = 2; i < buf.length; i += 2) {
			Assert.assertTrue(buf[i] >= buf[i - 2]);
		}
	}
	
	@Test
	@JacobiImport("test random 10x5")
	public void testShouldBeAbleToSortByInsertSort() {
		double[] buf = new double[2 * this.input.getRowCount()];
		
		Ranking ranking = new Ranking(buf, n -> 2, 0).init(i -> this.input.get(i, 0));
		ranking.insertsort(0, this.input.getRowCount());
		
		for(int i = 2; i < buf.length; i += 2) {
			Assert.assertTrue(buf[i] >= buf[i - 2]);
		}
		
		Arrays.fill(buf, 0.0);
		ranking = new Ranking(buf, n -> 2, 0).init(i -> this.input.get(i, 1));
		ranking.heapsort(0, this.input.getRowCount());
		
		for(int i = 2; i < buf.length; i += 2) {
			Assert.assertTrue(buf[i] >= buf[i - 2]);
		}
		
		Arrays.fill(buf, 0.0);
		ranking = new Ranking(buf, n -> 2, 0).init(i -> this.input.get(i, 2));
		ranking.heapsort(0, this.input.getRowCount());
		
		for(int i = 2; i < buf.length; i += 2) {
			Assert.assertTrue(buf[i] >= buf[i - 2]);
		}
		
		Arrays.fill(buf, 0.0);
		ranking = new Ranking(buf, n -> 2, 0).init(i -> this.input.get(i, 3));
		ranking.heapsort(0, this.input.getRowCount());
		
		for(int i = 2; i < buf.length; i += 2) {
			Assert.assertTrue(buf[i] >= buf[i - 2]);
		}
		
		Arrays.fill(buf, 0.0);
		ranking = new Ranking(buf, n -> 2, 0).init(i -> this.input.get(i, 4));
		ranking.heapsort(0, this.input.getRowCount());
		
		for(int i = 2; i < buf.length; i += 2) {
			Assert.assertTrue(buf[i] >= buf[i - 2]);
		}
	}
	
	@Test
	@JacobiImport("test random 10x5")
	public void testShouldBeAbleToPositionPivotsByAny2DistinctPivots() {
		double[] buf = new double[2 * this.input.getRowCount()];
		Ranking ranking = new Ranking(buf, n -> 2, 0);
		
		for(int i = 0; i < this.input.getRowCount(); i++) {
			for(int j = i + 1; j < this.input.getRowCount(); j++) {
				ranking.init(k -> this.input.get(k, 0));
				
				double left = buf[2 * i];
				double right = buf[2 * j];
				
				int[] pos = ranking.pivoting3(0, this.input.getRowCount(), i, j);
				
				Assert.assertEquals("Lower not in place " + Arrays.toString(pos)
					+ ", pivots " + left + "," + right + ", array "
					+ Arrays.toString(buf), 
					Math.min(left, right), buf[2 * pos[0]], 1e-12);
				Assert.assertEquals("Upper not in place " + Arrays.toString(pos)
					+ ", pivots " + left + "," + right + ", array "
					+ Arrays.toString(buf),
					Math.max(left, right), buf[2 * pos[1]], 1e-12);
				
				
			}
		}
	}
	
	@Test
	@JacobiImport("test random 10x5")
	public void testShouldBeAbleToPartitionValuesByAny2DistinctPivots() {
		double[] buf = new double[2 * this.input.getRowCount()];
		Ranking ranking = new Ranking(buf, n -> 2, 0);
		
		for(int i = 0; i < this.input.getRowCount(); i++) {
			for(int j = i + 1; j < this.input.getRowCount(); j++) {
				ranking.init(k -> this.input.get(k, 1));
				
				double left = buf[2 * i];
				double right = buf[2 * j];
				
				int[] pos = ranking.pivoting3(0, this.input.getRowCount(), i, j);
				
				double lower = Math.min(left, right);
				double upper = Math.max(left, right);
				
				for(int k = 0; k < buf.length; k += 2) {
					int idx = k / 2;
					if(idx < pos[0]) {
						Assert.assertTrue(buf[k] < lower);
						continue;
					}
					
					if(idx > pos[1]) {
						Assert.assertTrue(buf[k] > upper);
						continue;
					}
					
					Assert.assertTrue(
						"#" + k + " is out of range [" + lower + "," + upper + "] in "
						+ Arrays.toString(buf),
						buf[k] >= lower && buf[k] <= upper);
				}
			}
		}
	}
	
	@Test
	@JacobiImport("test rand 13x4 with duplicates")
	public void shouldBeAbleToPartition3BySamePivotValue() {
		double[] buf = new double[2 * this.input.getRowCount()];
		Ranking ranking = new Ranking(buf, n -> 2, 0)
				.init(k -> this.input.get(k, 0));
		
		double val = buf[2 * 1];
		
		Assert.assertTrue(buf[2 * 1] + " <> " + buf[2 * 5], 
			buf[2 * 1] == buf[2 * 5]);
		
		int[] pos = ranking.pivoting3(0, this.input.getRowCount(), 1, 5);
		
		for(int i = 0; i < pos[0]; i++) {
			Assert.assertTrue(val > buf[2 * i]);
		}
		for(int i = pos[0]; i <= pos[1]; i++) {
			Assert.assertTrue(val == buf[2 * i]);
		}
		for(int i = pos[1] + 1; i < this.input.getRowCount(); i++) {
			Assert.assertTrue(val <= buf[2 * i]);
		}
	}
	
	@Test
	@JacobiImport("test rand 13x4 with duplicates")
	public void shouldBeAbleToPartition3ByWithAllEqualValues() {
		double[] buf = new double[2 * this.input.getRowCount()];
		Ranking ranking = new Ranking(buf, n -> 2, 0)
				.init(k -> this.input.get(k, 3));
		
		int[] pos = ranking.pivoting3(0, this.input.getRowCount(), 0, 1);
		Assert.assertEquals(0, pos[0]);
		Assert.assertEquals(1, pos[1]);
	}
	
	@Test
	@JacobiImport("test random 10x5")
	public void testShouldBeAbleToSortByIntroSortRandCase0() {
		double[] buf = new double[2 * this.input.getRowCount()];
		
		Ranking ranking = new Ranking(buf, n -> n - 1, 0).init(i -> this.input.get(i, 0));		
		ranking.introsort(0, this.input.getRowCount(), Integer.MAX_VALUE);
		
		for(int i = 2; i < buf.length; i += 2) {
			Assert.assertTrue(buf[i] >= buf[i - 2]);
		}		
	}
	
	@Test
	@JacobiImport("test random 10x5")
	public void testShouldBeAbleToSortByIntroSortRandCase1() {
		double[] buf = new double[2 * this.input.getRowCount()];
		
		Ranking ranking = new Ranking(buf, n -> n - 1, 0).init(i -> this.input.get(i, 1));		
		ranking.introsort(0, this.input.getRowCount(), Integer.MAX_VALUE);
		
		for(int i = 2; i < buf.length; i += 2) {
			Assert.assertTrue(buf[i] >= buf[i - 2]);
		}		
	}
	
	@Test
	@JacobiImport("test random 10x5")
	public void testShouldBeAbleToSortByIntroSortRandCase2() {
		double[] buf = new double[2 * this.input.getRowCount()];
		
		Ranking ranking = new Ranking(buf, n -> n - 1, 0).init(i -> this.input.get(i, 2));		
		ranking.introsort(0, this.input.getRowCount(), Integer.MAX_VALUE);
		
		for(int i = 2; i < buf.length; i += 2) {
			Assert.assertTrue(buf[i] >= buf[i - 2]);
		}		
	}
	
	@Test
	@JacobiImport("test random 10x5")
	public void testShouldBeAbleToSortByIntroSortRandCase3() {
		double[] buf = new double[2 * this.input.getRowCount()];
		
		Ranking ranking = new Ranking(buf, n -> n - 1, 0).init(i -> this.input.get(i, 3));		
		ranking.introsort(0, this.input.getRowCount(), Integer.MAX_VALUE);
		
		for(int i = 2; i < buf.length; i += 2) {
			Assert.assertTrue(buf[i] >= buf[i - 2]);
		}		
	}
	
	@Test
	@JacobiImport("test random 10x5")
	public void testShouldBeAbleToSortByIntroSortRandCase4() {
		double[] buf = new double[2 * this.input.getRowCount()];
		
		Ranking ranking = new Ranking(buf, n -> n - 1, 0).init(i -> this.input.get(i, 4));		
		ranking.introsort(0, this.input.getRowCount(), Integer.MAX_VALUE);
		
		for(int i = 2; i < buf.length; i += 2) {
			Assert.assertTrue(buf[i] >= buf[i - 2]);
		}
	}
	
	@Test
	public void testShouldBeAbleToSortCyclicRadix5() {
		IntToDoubleFunction fn = k -> 
			k % 5 == 0 ? 88.3
		  : k % 5 == 1 ? 42.7
		  : k % 5 == 2 ? 6.168
		  : k % 5 == 3 ? -0.71
		  : 60.7;
			
		int[] seq = Ranking.of(20).init(fn).sort();
		System.out.println(Arrays.toString(seq));
	}
	
	@Test
	@JacobiImport("test random 500x3")
	public void testShouldBeAbleToSort500RandomCase0() {
		int[] seq = Ranking.of(this.input.getRowCount())
				.init(i -> this.input.get(i, 0))
				.sort();
		
		Assert.assertEquals(this.input.getRowCount(), seq.length);
		for(int i = 1; i < seq.length; i++) {
			Assert.assertFalse(this.input.get(seq[i - 1], 0) > this.input.get(seq[i], 0));
		}
	}
	
	@Test
	@JacobiImport("test random 500x3")
	public void testShouldBeAbleToSort500RandomCyclicCase2() {
		int[] seq = Ranking.of(this.input.getRowCount())
				.init(i -> this.input.get(i, 2))
				.sort();
		
		Assert.assertEquals(this.input.getRowCount(), seq.length);
		for(int i = 0; i < 5; i++) {
			int base = 100 * i;
			for(int j = 0; j < 100; j++) {
				System.out.println(this.input.get(seq[base + j], 2));
				/*
				Assert.assertEquals(
					this.input.get(seq[base], 2), 
					this.input.get(seq[base + j], 2), 1e-12);
					*/
			}
		}
	}

}
