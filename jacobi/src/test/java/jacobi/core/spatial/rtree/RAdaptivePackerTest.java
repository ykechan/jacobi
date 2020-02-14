package jacobi.core.spatial.rtree;

import java.awt.Color;
import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import jacobi.core.spatial.rtree.RAdaptivePacker.Mbb;
import jacobi.test.util.JacobiSvg;

public class RAdaptivePackerTest {
	
	@Test
	public void shouldBeAbleToGroup3PointCloudsWithDifferentCenterOfMass() throws IOException {
		double variance = 60.0;
		
		double[][] centers = new double[][] {
			{0.0, 0.0},
			{100.0, 0.0},
			{50.0, 100.0},
		};
		
		List<RObject<?>> points = new ArrayList<>(300);
		Random rand = new Random(Double.doubleToLongBits(Math.E));
		for(int i = 0; i < 300; i++) {
			double[] c = centers[i / 100];
			points.add(this.point(new double[] {
				c[0] + rand.nextDouble() * (variance + (i % 100 > 90 ? 10.0 : 0.0)),
				c[1] + rand.nextDouble() * (variance + (i % 100 > 90 ? 10.0 : 0.0))
			}));
		}
		
		RAdaptivePacker packer = new RAdaptivePacker(() -> 1.0);
		
		
		
		int[] groups = packer.pack(points, 90, 120);
		Assert.assertArrayEquals(new int[] {100, 100, 100},  groups);
		
		JacobiSvg svg = new JacobiSvg();
		
		int start = 0;
		for(int i = 0; i < groups.length; i++) {
			int num = groups[i];
			Mbb mbb = packer.toMbb(points.get(start).minBoundBox());
			for(int k = 1; k < num; k++){
				packer.updateMbb(mbb, points.get(start + k).minBoundBox());
			}
			start += num;
			
			svg.rect(mbb.min[0], mbb.min[1], 
					mbb.max[0] - mbb.min[0],
					mbb.max[1] - mbb.min[1], Color.GREEN);
		}
				
		for(RObject<?> p : points) {
			svg.dot(p.minBoundBox().min(0), 
					p.minBoundBox().min(1), 
					Color.BLUE);
		}
				
		svg.exportTo(null);
	}

	@Test
	public void shouldBeAbleToRejectJumpInArea() {
		List<RObject<double[]>> rObjs = this.points(new double[][] {
			{0.0, 1.0},
			{1.0, 2.0},
			{1.0, 3.0},
			{10.0, 30.0}
		});
		Assert.assertEquals(3, new RAdaptivePacker(() -> 1.0)
			.spanFront(rObjs, 3));
	}
	
	@Test
	public void shouldDeltaBe05WhenBothWidthAndHeightDoubles() {
		Mbb mbb = new Mbb(new double[] {1.0, 2.0}, new double[] {2.0, 3.0});
		double delta = new RAdaptivePacker(() -> 1.0)
				.updateMbb(mbb, Aabb.wrap(new double[] {3.0, 4.0}));
		
		Assert.assertEquals(0.5, delta, 1e-12);
	}
	
	@Test
	public void shouldDeltaBe075WhenOnlyWidthDoubles() {
		Mbb mbb = new Mbb(new double[] {1.0, 2.0}, new double[] {2.0, 3.0});
		double delta = new RAdaptivePacker(() -> 1.0)
				.updateMbb(mbb, Aabb.wrap(new double[] {3.0, 3.0}));
		
		Assert.assertEquals((1.5 / 2), delta, 1e-12);
	}
	
	protected List<RObject<double[]>> points(double[]... pts) {
		return new AbstractList<RObject<double[]>>() {

			@Override
			public RObject<double[]> get(int index) {
				return point(pts[index]);
			}

			@Override
			public int size() {
				return pts.length;
			}
			
		};
	}
	
	protected RObject<double[]> point(double[] p) {
		return new RObject<double[]>() {

			@Override
			public Optional<double[]> get() {
				return Optional.of(p);
			}

			@Override
			public Aabb minBoundBox() {
				return Aabb.wrap(p);
			}

			@Override
			public List<RObject<double[]>> nodes() {
				return Collections.emptyList();
			}
			
		};
	}

}
