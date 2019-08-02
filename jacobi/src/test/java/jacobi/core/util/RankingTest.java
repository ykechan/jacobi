package jacobi.core.util;

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
	public void testShouldBeSelectMedianOf5() {
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
	}

}
