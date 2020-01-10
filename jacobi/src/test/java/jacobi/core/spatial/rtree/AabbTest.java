package jacobi.core.spatial.rtree;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;

public class AabbTest {
	
	@Test
	public void shouldBeAbleToJoinDiagonalPoint() {
		Aabb aabb = Aabb.wrap(new double[] {-7.3, -13.4})
			.join(Arrays.asList( Aabb.wrap(new double[] {5.6, 8.9}) ));
		
		this.assertEquals(new double[] {-7.3, -13.4},new double[] {5.6, 8.9} , aabb);
		
		aabb = Aabb.wrap(new double[] {-7.3, 8.9})
				.join(Arrays.asList( Aabb.wrap(new double[] {5.6, -13.4}) ));
		
		this.assertEquals(new double[] {-7.3, -13.4},new double[] {5.6, 8.9} , aabb);
		
		aabb = Aabb.wrap(new double[] {5.6, 8.9})
				.join(Arrays.asList( Aabb.wrap(new double[] {-7.3, -13.4}) ));
		
		this.assertEquals(new double[] {-7.3, -13.4},new double[] {5.6, 8.9} , aabb);
		
		aabb = Aabb.wrap(new double[] {5.6, -13.4})
				.join(Arrays.asList( Aabb.wrap(new double[] {-7.3, 8.9}) ));
		
		this.assertEquals(new double[] {-7.3, -13.4},new double[] {5.6, 8.9} , aabb);				
	}
	
	@Test
	public void shouldBeAbleToJoinMultiplePoints() {
		Aabb aabb = Aabb.wrap(new double[] {1.0, 1.0, 1.0, 1.0}).join(Arrays.asList(
			Aabb.wrap(IntStream.range(0, 4).mapToDouble(i -> 4.0).toArray()),
			Aabb.wrap(IntStream.range(0, 4).mapToDouble(i -> 2.0).toArray()),
			Aabb.wrap(IntStream.range(0, 4).mapToDouble(i -> 3.0).toArray()),
			Aabb.wrap(IntStream.range(0, 4).mapToDouble(i -> 6.0).toArray()),
			Aabb.wrap(IntStream.range(0, 4).mapToDouble(i -> 9.0).toArray())
		));
		
		this.assertEquals(new double[] {1.0, 1.0, 1.0, 1.0}, new double[] {9.0, 9.0, 9.0, 9.0}, aabb);
	}
	
	@Test
	public void shouldBeAbleToJoinLongListOfAabbs() {
		double[] items = {-67.8, 35.12, -0.334, 2.13, 99.9, -3, 0.0, -99.9};
		int dim = 11;
		IntFunction<double[]> fn = i -> IntStream
			.range(i, i + dim).mapToDouble(k -> items[k % items.length]).toArray();
		
		Aabb aabb = Aabb.wrap(fn.apply(0)).join(new AbstractList<Aabb>() {

			@Override
			public Aabb get(int index) {
				return Aabb.wrap(fn.apply(1 + index));
			}

			@Override
			public int size() {
				return 1024;
			}
			
		});
		
		this.assertEquals(
			IntStream.range(0, dim).mapToDouble(i -> -99.9).toArray(), 
			IntStream.range(0, dim).mapToDouble(i ->  99.9).toArray(), 
			aabb);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldFailJoiningAabbWitDiffDim() {
		Aabb.wrap(new double[] { 180 }).join(Arrays.asList(
			Aabb.wrap(new double[] {20.3, 77.6})
		));
	}
	
	protected void assertEquals(double[] min, double[] max, Aabb aabb) {
		Assert.assertEquals(aabb.dim(), min.length);
		for(int i = 0; i < min.length; i++){
			Assert.assertEquals(min[i], aabb.min(i), 1e-12);
			Assert.assertEquals(min[i], aabb.max(i), 1e-12);
		}
	}

}
