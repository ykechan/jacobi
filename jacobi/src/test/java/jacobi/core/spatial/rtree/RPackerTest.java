package jacobi.core.spatial.rtree;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import jacobi.core.spatial.rtree.RPacker.Mbb;
import jacobi.test.util.JacobiSvg;

public class RPackerTest {
	
	@Test
	public void shouldBeAbleToGroup3PointCloudsWithDifferentCenterOfMass() throws IOException {
		double variance = 60.0;
		
		double[][] centers = new double[][] {
			{0.0, 0.0},
			{100.0, 0.0},
			{50.0, 100.0},
		};
		
		List<double[]> points = new ArrayList<>(300);
		Random rand = new Random(Double.doubleToLongBits(Math.E));
		for(int i = 0; i < 300; i++) {
			double[] c = centers[i / 100];
			points.add(new double[] {
				c[0] + rand.nextDouble() * (variance + (i % 100 > 90 ? 10.0 : 0.0)),
				c[1] + rand.nextDouble() * (variance + (i % 100 > 90 ? 10.0 : 0.0))
			});
		}
		
		RPacker packer = new RPacker();
		
		int[] groups = packer.pack(points, 90, 120, () -> 1.0);
		Assert.assertArrayEquals(new int[] {100, 100, 100},  groups);
		
		JacobiSvg svg = new JacobiSvg();
		
		int start = 0;
		for(int i = 0; i < groups.length; i++) {
			int num = groups[i];
			Mbb mbb = packer.degenerate(points.get(start));
			for(int k = 1; k < num; k++){
				packer.updateMbb(mbb, points.get(start + k));
			}
			start += num;
			
			svg.rect(mbb.min[0], mbb.min[1], 
					mbb.max[0] - mbb.min[0],
					mbb.max[1] - mbb.min[1], Color.GREEN);
		}
				
		for(double[] p : points) {
			svg.dot(p[0], p[1], Color.BLUE);
		}
				
		svg.exportTo(null);
	}
	
	@Test
	public void shouldBeAbleToCutOffWhenAreaJumps() {
		List<double[]> points = Arrays.asList(
			new double[] {1.0, 2.0},
			new double[] {1.1, 1.9},
			new double[] {0.9, 2.1},
			new double[] {7.99, 6.3},
			new double[] {6.0, 7.1},
			new double[] {6.3, 6.8},
			new double[] {1.01, 2.07},
			new double[] {1.09, 2.02}			
		);
		
		Assert.assertEquals(3, new RPacker().packFront(points, 3, () -> 1.0));
	}	
	
	@Test
	public void shouldBeAbleToAcceptInclusionPoint() {
		List<double[]> points = Arrays.asList(
			new double[] {1.0, 2.0},
			new double[] {1.1, 1.9},
			new double[] {0.9, 2.1},
			new double[] {0.99, 2.3},
			new double[] {1.01, 2.07},
			new double[] {1.09, 2.02},
			new double[] {6.0, 7.1},
			new double[] {6.3, 6.8}
		);
		
		Assert.assertEquals(6, new RPacker().packFront(points, 3, () -> 1.0));
	}
	
	@Test
	public void shouldBeAbleToComputeTheRateOfChangeWhenDoubleLengthIn2D() {
		Mbb mbb = new Mbb(
			new double[] {0.0, 0.0},
			new double[] {1.0, 1.0}
		);
		
		double dv = new RPacker().updateMbb(mbb, new double[] {2.0, 2.0});
		Assert.assertEquals(0.5, dv, 1e-12);
	}
	
	@Test
	public void shouldBeAbleToComputeTheRateOfChangeWhenIncreaseVariousLengthIn3D() {
		Mbb mbb = new Mbb(
			new double[] {0.5, 0.1, 0.3},
			new double[] {1.0, 2.0, 3.0}
		);
		
		double dv = new RPacker()
			.updateMbb(mbb, new double[] {0.7, 2.5, 0.1});
		Assert.assertEquals((1.0 + (2.0 - 0.1) / (2.5 - 0.1) + (3.0 - 0.3) / (3.0 - 0.1)) / 3.0
			,dv, 1e-12);
	}
	
	@Test
	public void shouldBeAbleToUpdateTheMaxBounds() {
		Mbb mbb = new Mbb(
			new double[] {0.0, 0.0},
			new double[] {1.0, 1.0}
		);
		
		new RPacker().updateMbb(mbb, new double[] {2.0, 3.0});
		Assert.assertArrayEquals(new double[] {2.0, 3.0}, mbb.max, 1e-12);
	}
	
	@Test
	public void shouldBeAbleToUpdateTheMinBounds() {
		Mbb mbb = new Mbb(
			new double[] {0.0, 0.0},
			new double[] {1.0, 1.0}
		);
		
		new RPacker().updateMbb(mbb, new double[] {-1.0, 3.0});
		Assert.assertArrayEquals(new double[] {-1.0, 0.0}, mbb.min, 1e-12);
	}
	
	@Test
	public void shouldBeAbleToUpdateTheMaxAndMinBounds() {
		Mbb mbb = new Mbb(
			new double[] {0.0, 0.0},
			new double[] {1.0, 1.0}
		);
		
		new RPacker().updateMbb(mbb, new double[] {-1.0, 3.0});
		Assert.assertArrayEquals(new double[] {-1.0, 0.0}, mbb.min, 1e-12);
		Assert.assertArrayEquals(new double[] { 1.0, 3.0}, mbb.max, 1e-12);
	}
	
	@Test
	public void shouldBeAbleToComputeRejectionProbabilities() {
		int range = 10;
		for(int i = 0; i < range; i++) {
			System.out.println(new RPacker().rejectProb(i, range));
		}
	}

}
