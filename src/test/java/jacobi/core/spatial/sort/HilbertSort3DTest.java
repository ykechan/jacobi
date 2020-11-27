package jacobi.core.spatial.sort;

import java.util.AbstractList;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;

import jacobi.core.spatial.sort.HilbertSort3D.BasisType;

public class HilbertSort3DTest {
	
	@Test
	public void shouldBeAbleToSortA2x2x2CubeByStayCurve() {
		List<double[]> points = this.grid(2);
		HilbertSort3D sorter = new HilbertSort3D(0, 1, 2);
		
		double[] comps = sorter.init(points);
		int[] order = sorter.sort(comps, 
			HilbertSort3D.BASIS[BasisType.STAY.ordinal()], 
			IntStream.range(0, points.size()).toArray()
		);
		
		Assert.assertArrayEquals(new double[]{0.0, 0.0, 0.0}, points.get(order[0]), 1e-12);
		Assert.assertArrayEquals(new double[]{1.0, 0.0, 0.0}, points.get(order[1]), 1e-12);
		Assert.assertArrayEquals(new double[]{1.0, 1.0, 0.0}, points.get(order[2]), 1e-12);
		Assert.assertArrayEquals(new double[]{0.0, 1.0, 0.0}, points.get(order[3]), 1e-12);
		
		Assert.assertArrayEquals(new double[]{0.0, 1.0, 1.0}, points.get(order[4]), 1e-12);
		Assert.assertArrayEquals(new double[]{1.0, 1.0, 1.0}, points.get(order[5]), 1e-12);
		Assert.assertArrayEquals(new double[]{1.0, 0.0, 1.0}, points.get(order[6]), 1e-12);
		Assert.assertArrayEquals(new double[]{0.0, 0.0, 1.0}, points.get(order[7]), 1e-12);
	}
	
	@Test
	public void shouldBeAbleToSortA2x2x2CubeByDiagCurve() {
		List<double[]> points = this.grid(2);
		HilbertSort3D sorter = new HilbertSort3D(0, 1, 2);
		
		double[] comps = sorter.init(points);
		int[] order = sorter.sort(comps, 
			HilbertSort3D.BASIS[BasisType.DIAG.ordinal()], 
			IntStream.range(0, points.size()).toArray()
		);
		
		Assert.assertArrayEquals(new double[]{0.0, 0.0, 0.0}, points.get(order[0]), 1e-12);
		Assert.assertArrayEquals(new double[]{1.0, 0.0, 0.0}, points.get(order[1]), 1e-12);
		Assert.assertArrayEquals(new double[]{1.0, 1.0, 0.0}, points.get(order[2]), 1e-12);
		Assert.assertArrayEquals(new double[]{0.0, 1.0, 0.0}, points.get(order[3]), 1e-12);
		
		Assert.assertArrayEquals(new double[]{0.0, 1.0, 1.0}, points.get(order[4]), 1e-12);
		Assert.assertArrayEquals(new double[]{0.0, 0.0, 1.0}, points.get(order[5]), 1e-12);
		Assert.assertArrayEquals(new double[]{1.0, 0.0, 1.0}, points.get(order[6]), 1e-12);
		Assert.assertArrayEquals(new double[]{1.0, 1.0, 1.0}, points.get(order[7]), 1e-12);
	}
	
	@Test
	public void shouldBeAbleToSortA2x2x2CubeByRightCurve() {
		List<double[]> points = this.grid(2);
		HilbertSort3D sorter = new HilbertSort3D(0, 1, 2);
		
		double[] comps = sorter.init(points);
		int[] order = sorter.sort(comps, 
			HilbertSort3D.BASIS[BasisType.RIGHT.ordinal()], 
			IntStream.range(0, points.size()).toArray()
		);
		
		Assert.assertArrayEquals(new double[]{0.0, 0.0, 0.0}, points.get(order[0]), 1e-12);
		Assert.assertArrayEquals(new double[]{0.0, 1.0, 0.0}, points.get(order[1]), 1e-12);
		Assert.assertArrayEquals(new double[]{0.0, 1.0, 1.0}, points.get(order[2]), 1e-12);
		Assert.assertArrayEquals(new double[]{0.0, 0.0, 1.0}, points.get(order[3]), 1e-12);
		
		Assert.assertArrayEquals(new double[]{1.0, 0.0, 1.0}, points.get(order[4]), 1e-12);
		Assert.assertArrayEquals(new double[]{1.0, 1.0, 1.0}, points.get(order[5]), 1e-12);
		Assert.assertArrayEquals(new double[]{1.0, 1.0, 0.0}, points.get(order[6]), 1e-12);
		Assert.assertArrayEquals(new double[]{1.0, 0.0, 0.0}, points.get(order[7]), 1e-12);
	}
	
	@Test
	public void shouldBeAbleToSortA2x2x2CubeByForwardCurve() {
		List<double[]> points = this.grid(2);
		HilbertSort3D sorter = new HilbertSort3D(0, 1, 2);
		
		double[] comps = sorter.init(points);
		int[] order = sorter.sort(comps, 
			HilbertSort3D.BASIS[BasisType.FORWARD.ordinal()], 
			IntStream.range(0, points.size()).toArray()
		);
		
		Assert.assertArrayEquals(new double[]{0.0, 0.0, 0.0}, points.get(order[0]), 1e-12);
		Assert.assertArrayEquals(new double[]{1.0, 0.0, 0.0}, points.get(order[1]), 1e-12);
		Assert.assertArrayEquals(new double[]{1.0, 0.0, 1.0}, points.get(order[2]), 1e-12);
		Assert.assertArrayEquals(new double[]{0.0, 0.0, 1.0}, points.get(order[3]), 1e-12);
		
		Assert.assertArrayEquals(new double[]{0.0, 1.0, 1.0}, points.get(order[4]), 1e-12);
		Assert.assertArrayEquals(new double[]{1.0, 1.0, 1.0}, points.get(order[5]), 1e-12);
		Assert.assertArrayEquals(new double[]{1.0, 1.0, 0.0}, points.get(order[6]), 1e-12);
		Assert.assertArrayEquals(new double[]{0.0, 1.0, 0.0}, points.get(order[7]), 1e-12);
	}
	
	@Test
	public void shouldBeAbleToSortA4x4x4CubeByFirstBasis() {
		List<double[]> points = this.grid(4);
		HilbertSort3D sorter = new HilbertSort3D(0, 1, 2);
		
		double[] comps = sorter.init(points);
		int[] order = sorter.sort(comps, 
			HilbertSort3D.BASIS[0], 
			IntStream.range(0, points.size()).toArray()
		);			
		
		for(int k = 1; k < order.length; k++) {
			double[] u = points.get(order[k - 1]);
			double[] v = points.get(order[k]);
			
			Assert.assertEquals(1.0, 
				Math.abs(u[0] - v[0])
			  + Math.abs(u[1] - v[1])
			  + Math.abs(u[2] - v[2]), 1e-8);
		}
	}
	
	@Test
	public void shouldBeAbleToSortA4x4x4CubeByAnyBasis() {
		List<double[]> points = this.grid(4);
		HilbertSort3D sorter = new HilbertSort3D(0, 1, 2);
		
		for(int basis : HilbertSort3D.BASIS) {
			double[] comps = sorter.init(points);
			int[] order = sorter.sort(comps, 
				basis, 
				IntStream.range(0, points.size()).toArray()
			);			
			
			for(int k = 1; k < order.length; k++) {
				double[] u = points.get(order[k - 1]);
				double[] v = points.get(order[k]);
				
				Assert.assertEquals(1.0, 
					Math.abs(u[0] - v[0])
				  + Math.abs(u[1] - v[1])
				  + Math.abs(u[2] - v[2]), 1e-8);
			}
		}
	}
	
	@Test
	public void shouldBeAbleToSortA16x16x16Cube() {
		List<double[]> points = this.grid(16);
		int[] order = new HilbertSort3D(0, 1, 2).sort(points);
		
		for(int k = 1; k < order.length; k++) {
			double[] u = points.get(order[k - 1]);
			double[] v = points.get(order[k]);
			
			Assert.assertEquals(1.0, 
				Math.abs(u[0] - v[0])
			  + Math.abs(u[1] - v[1])
			  + Math.abs(u[2] - v[2]), 1e-8);
		}
	}
	
	protected List<double[]> grid(int n) {
		return new AbstractList<double[]>() {

			@Override
			public double[] get(int index) {
				return new double[] { 
					index % n, 
					(index / n) % n,
					(index / n / n) % n
				};
			}

			@Override
			public int size() {
				return n * n * n;
			}
			
		};
	}

}
