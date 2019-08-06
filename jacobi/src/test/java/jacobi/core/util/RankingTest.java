package jacobi.core.util;

import java.util.Arrays;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
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
		Ranking ranking = new Ranking(buffer, n -> 0).init(i -> this.input.get(i, 0));
		
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
		/*
		double[] buffer = new double[2 * this.input.getRowCount()];
		
		int pivot = new Ranking(buffer, n -> 2).init(i -> this.input.get(i, 0))
				.select(0, buffer.length);
		Set<Double> values = new TreeSet<>();
		values.add(this.input.get(0, 0));
		values.add(this.input.get(this.input.getRowCount() / 2, 0));
		values.add(this.input.get(this.input.getRowCount() - 1, 0));
		values.add(this.input.get(2, 0));
		values.add(this.input.get((this.input.getRowCount() / 2) + 2, 0));
		
		Assert.assertEquals(values.toArray(new Double[0])[2], this.input.get(pivot / 2, 0), 1e-12);
		
		pivot = new Ranking(buffer, n -> 3).init(i -> this.input.get(i, 1))
				.select(0, buffer.length);
		values = new TreeSet<>();
		values.add(this.input.get(0, 1));
		values.add(this.input.get(this.input.getRowCount() / 2, 1));
		values.add(this.input.get(this.input.getRowCount() - 1, 1));
		values.add(this.input.get(3, 1));
		values.add(this.input.get((this.input.getRowCount() / 2) + 3, 1));
		Assert.assertEquals(values.toArray(new Double[0])[2], this.input.get(pivot / 2, 1), 1e-12);
		
		pivot = new Ranking(buffer, n -> 1).init(i -> this.input.get(i, 2))
				.select(0, buffer.length);
		values = new TreeSet<>();
		values.add(this.input.get(0, 2));
		values.add(this.input.get(this.input.getRowCount() / 2, 2));
		values.add(this.input.get(this.input.getRowCount() - 1, 2));
		values.add(this.input.get(1, 2));
		values.add(this.input.get((this.input.getRowCount() / 2) + 1, 2));
		Assert.assertEquals(values.toArray(new Double[0])[2], this.input.get(pivot / 2, 2), 1e-12);
		*/
	}
	
	@Test
	@JacobiImport("test random 10x5")
	public void testShouldBeAbleToSortByHeapSort() {
		double[] buf = new double[2 * this.input.getRowCount()];
		
		Ranking ranking = new Ranking(buf, n -> 2).init(i -> this.input.get(i, 0));
		ranking.heapsort(0, this.input.getRowCount());
		
		for(int i = 2; i < buf.length; i += 2) {
			Assert.assertTrue(buf[i] >= buf[i - 2]);
		}
		
		Arrays.fill(buf, 0.0);
		ranking = new Ranking(buf, n -> 2).init(i -> this.input.get(i, 1));
		ranking.heapsort(0, this.input.getRowCount());
		
		for(int i = 2; i < buf.length; i += 2) {
			Assert.assertTrue(buf[i] >= buf[i - 2]);
		}
		
		Arrays.fill(buf, 0.0);
		ranking = new Ranking(buf, n -> 2).init(i -> this.input.get(i, 2));
		ranking.heapsort(0, this.input.getRowCount());
		
		for(int i = 2; i < buf.length; i += 2) {
			Assert.assertTrue(buf[i] >= buf[i - 2]);
		}
		
		Arrays.fill(buf, 0.0);
		ranking = new Ranking(buf, n -> 2).init(i -> this.input.get(i, 3));
		ranking.heapsort(0, this.input.getRowCount());
		
		for(int i = 2; i < buf.length; i += 2) {
			Assert.assertTrue(buf[i] >= buf[i - 2]);
		}
		
		Arrays.fill(buf, 0.0);
		ranking = new Ranking(buf, n -> 2).init(i -> this.input.get(i, 4));
		ranking.heapsort(0, this.input.getRowCount());
		
		for(int i = 2; i < buf.length; i += 2) {
			Assert.assertTrue(buf[i] >= buf[i - 2]);
		}
	}
	
	@Test
	@JacobiImport("test random 10x5")
	public void testShouldBeAbleToPositionPivotsByAny2DistinctPivots() {
		double[] buf = new double[2 * this.input.getRowCount()];
		Ranking ranking = new Ranking(buf, n -> 2);
		
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
		Ranking ranking = new Ranking(buf, n -> 2);
		
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
		Ranking ranking = new Ranking(buf, n -> 2)
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
			Assert.assertTrue(val < buf[2 * i]);
		}
	}
	
	@Test
	@JacobiImport("test rand 13x4 with duplicates")
	public void shouldBeAbleToPartition3ByWithAllEqualValues() {
		double[] buf = new double[2 * this.input.getRowCount()];
		Ranking ranking = new Ranking(buf, n -> 2)
				.init(k -> this.input.get(k, 3));
		
		int[] pos = ranking.pivoting3(0, this.input.getRowCount(), 0, 1);
		Assert.assertEquals(0, pos[0]);
		Assert.assertEquals(this.input.getRowCount() - 1, pos[1]);
	}

}
