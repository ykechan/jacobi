package jacobi.core.classifier.cart;

import java.util.AbstractList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import jacobi.api.Matrix;
import jacobi.core.classifier.cart.data.Column;
import jacobi.core.classifier.cart.data.Instance;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.util.JacobiJUnit4ClassRunner;

@JacobiImport("/jacobi/test/data/NominalPartitionTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class NominalPartitionTest {
	
	@JacobiInject(0)
	public Matrix input;
	
	@Test
	@JacobiImport("test 10x5 radix")
	public void testShouldBeAbleToCount10x5Radix2() {
		double result = new NominalPartition(dist -> {
			Assert.assertTrue(
				(dist[0] == 2.0 && dist[1] == 3.0)
			 || (dist[0] == 3.0 && dist[1] == 2.0) 
			);
			return Math.E;
		}).measure(
			Column.nominal(1, 2, v -> (int) v), 
			Column.signed(0), 
			this.toInstances(this.input, 1, 0, -1));
		
		Assert.assertEquals(10 * Math.E, result, 1e-12);
	}
	
	@Test
	@JacobiImport("test 10x5 radix")
	public void testShouldBeAbleToCount10x5Radix3() {
		AtomicInteger count = new AtomicInteger(0);
		double result = new NominalPartition(dist -> {
			switch(count.getAndIncrement()) {
				case 0 :
					Assert.assertArrayEquals(new double[] {2.0, 2.0}, dist, 1e-12);
					break;
				case 1 :
					Assert.assertArrayEquals(new double[] {2.0, 1.0}, dist, 1e-12);
					break;
				case 2 :
					Assert.assertArrayEquals(new double[] {1.0, 2.0}, dist, 1e-12);
					break;
				default :
					break;
			}
			return Math.E;
		}).measure(
			Column.nominal(2, 3, v -> (int) v), 
			Column.signed(0), 
			this.toInstances(this.input, 2, 0, -1));
		
		Assert.assertEquals(3, count.get());
		Assert.assertEquals(10 * Math.E, result, 1e-12);
	}
	
	
	
	protected List<Instance> toInstances(Matrix matrix, 
			int featCol, int outcomeCol, int weightCol) {
		return new AbstractList<Instance>() {

			@Override
			public Instance get(int index) {
				double[] row = matrix.getRow(index);
				return new Instance(
					(int) Math.floor(row[featCol]),
					(int) Math.floor(row[outcomeCol]),
					weightCol < 0 ? 1.0 : row[weightCol]);
			}

			@Override
			public int size() {
				return matrix.getRowCount();
			}
			
		};
	}

}
