package jacobi.core.stats;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.core.impl.ColumnVector;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.JacobiJUnit4ClassRunner;

@JacobiImport("/jacobi/test/data/OrderStatsTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class OrderStatsTest {
	
	@JacobiInject(0)
	public Matrix input;
	
	@JacobiResult(10)
	public Matrix output;
	
	@JacobiInject(100)
	public Matrix sorted;
	
	@JacobiImport("Rand 30x8")
	@JacobiEquals(expected = 10, actual = 10)
	@Test
	public void shouldBeAbleToSelectExtrema() {
		AtomicInteger invokeMin = new AtomicInteger(0);
		AtomicInteger invokeMax = new AtomicInteger(0);
		
		OrderStats ord = new OrderStats((s, i, j, k) -> { throw new UnsupportedOperationException(); }, 
		new RowReduce.Min() {

			@Override
			public double[] compute(Matrix matrix) {
				invokeMin.incrementAndGet();
				return super.compute(matrix);
			}
			
		}, new RowReduce.Max() {

			@Override
			public double[] compute(Matrix matrix) {
				invokeMax.incrementAndGet();
				return super.compute(matrix);
			}
			
		});
		
		this.output = Matrices.wrap(new double[][] {
			ord.compute(input, 0),
			ord.compute(input, input.getRowCount() - 1)
		});
		
		Assert.assertEquals(1, invokeMin.get());
		Assert.assertEquals(1, invokeMax.get());
	}
	
	@JacobiImport("Rand 30x8 And Sorted")
	@Test
	public void shouldBeAbleToSelect1To28In30x8Matrix() {
		OrderStats ord = new OrderStats();
		Assert.assertEquals(30, this.input.getRowCount());
		
		for(int i = 1; i < 29; i++) {
			double[] ans = ord.compute(this.input, i);			
			Assert.assertArrayEquals("Order " + i, this.sorted.getRow(i),  ans, 1e-12);
		}
	}
	
	@JacobiImport("Rand 128x3 And Sorted")
	@Test
	public void shouldBeAbleToSelect40To90In128x3Matrix() {
		OrderStats ord = new OrderStats();
		Assert.assertEquals(128, this.input.getRowCount());
		
		for(int i = 40; i < 90; i++) {
			double[] ans = ord.compute(this.input, i);			
			Assert.assertArrayEquals("Order " + i, this.sorted.getRow(i),  ans, 1e-12);
		}
	}
	
	@Test
	public void shouldBeAbleToSelectRandomIndexInLongRandomSeq() {
		long init = Double.doubleToLongBits(Math.exp(Math.PI));
		double[] seq = new Random(init).doubles().limit(1024).toArray();
		
		double[] sorted = Arrays.copyOf(seq, seq.length);
		Arrays.sort(sorted);
		
		OrderStats ord = new OrderStats();
		
		int k = 100;
		for(int i = 0; i < 128; i++) {
			Assert.assertEquals(sorted[k], ord.compute(new ColumnVector(seq), k)[0], 1e-12);
			
			k = (13 * k + 7) % seq.length;
		}
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldFailWhenOrderIsOutOfRange() {
		new OrderStats().compute(Matrices.zeros(5), 5);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldFailWhenOrderIsNegative() {
		new OrderStats().compute(Matrices.zeros(5), -1);
	}

}
