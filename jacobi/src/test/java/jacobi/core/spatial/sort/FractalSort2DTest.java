package jacobi.core.spatial.sort;

import java.awt.Color;
import java.io.IOException;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;

import jacobi.core.spatial.sort.FractalSort2D.Quadrant;
import jacobi.test.util.JacobiSvg;

public class FractalSort2DTest {

	@Test
	public void shouldBeAbleToInitVectorsIntoComponentArray() {
		Assert.assertArrayEquals(new double[] {1.0, 2.0, 3.0, 4.0}, 
			new FractalSort2D(0, 1, i -> new int[0]).init(Arrays.asList(
				new double[] {1.0, 2.0},
				new double[] {3.0, 4.0}
			)), 
			1e-12
		);
		
		Assert.assertArrayEquals(new double[] {2.0, 1.0, 4.0, 3.0}, 
			new FractalSort2D(1, 0, i -> new int[0]).init(Arrays.asList(
				new double[] {1.0, 2.0},
				new double[] {3.0, 4.0}
			)), 
			1e-12
		);
		
		Assert.assertArrayEquals(new double[] {7.0, 2.0, 8.0, 4.0, 9.0, 6.0}, 
			new FractalSort2D(0, 2, i -> new int[0]).init(Arrays.asList(
				new double[] {7.0, 1.0, 2.0},
				new double[] {8.0, 3.0, 4.0},
				new double[] {9.0, 3.0, 6.0}
			)), 
			1e-12
		);
	}
	
	@Test
	public void shouldBeAbleToComputeTheMeanOfComponents() {
		Assert.assertArrayEquals(
			new double[] {2.0, 3.0}, 
			new FractalSort2D(0, 1, i -> new int[0])
				.mean(new double[] {1.0, 2.0, 3.0, 4.0}, 
					0, 2, new int[] {0, 1}),
			1e-12
		);
		
		Assert.assertArrayEquals(
			new double[] {(7.0 + 13.0 + 19.0) / 3, (11.0 + 17.0 + 23.0)/ 3}, 
			new FractalSort2D(0, 1, i -> new int[0])
				.mean(new double[] {3.0, 5.0, 7.0, 11.0, 13.0, 17.0, 19.0, 23.0}, 
					1, 4, new int[] {0, 1, 2, 3, 4}),
			1e-12
		);
	}
	
	@Test
	public void shouldBeAbleToComputeTheMeanOfComponentsAccordingToIndexRange() {
		Assert.assertArrayEquals(
			new double[] {(3.0 + 7.0 + 13.0) / 3, (5.0 + 11.0 + 17.0)/ 3}, 
			new FractalSort2D(0, 1, i -> new int[0])
				.mean(new double[] {3.0, 5.0, 7.0, 11.0, 13.0, 17.0, 19.0, 23.0}, 
					1, 4, new int[] {4, 0, 1, 2, 3}),
			1e-12
		);
	}
	
	@Test
	public void shouldBeAbleToGroup4DistinctCorner() {
		double[] comps = {-1.0, -1.0, -1.0, 1.0, 1.0, -1.0, 1.0, 1.0};
		
		int[] indices = {0, 1, 2, 3};
		
		Assert.assertArrayEquals(new int[] {1, 1, 1, 1}, 
			new FractalSort2D(0, 1, i -> new int[0]).groupQuad(comps, 
				new Quadrant(0, 4, Fractal2D.Z_ORDER, 0), 
				indices, 
				new int[comps.length / 2])
		);
		
		Assert.assertArrayEquals(new int[] {0, 2, 1, 3}, indices);			
	}
	
	@Test
	public void shouldBeAbleToGroup16GridPoints() {
		FractalSort2D fsort = new FractalSort2D(0, 1, i -> new int[0]);
		
		List<double[]> vectors = this.grid(4);
		
		int[] indices = IntStream.range(0, vectors.size()).toArray();
		
		double[] comps = fsort.init(vectors);
		int[] counts = fsort.groupQuad(comps, 
			new Quadrant(0, indices.length, Fractal2D.Z_ORDER, 0), 
			indices, 
			new int[indices.length]
		);
		
		Assert.assertArrayEquals(new int[] {4, 4, 4, 4}, counts);
		
		// LL
		for(int i = 0; i < 4; i++) {
			double[] vector = vectors.get(indices[i]);
			Assert.assertTrue(vector[0] < 1.5);
			Assert.assertTrue(vector[1] < 1.5);
		}
		
		// UL
		for(int i = 0; i < 4; i++) {
			double[] vector = vectors.get(indices[4 + i]);
			Assert.assertTrue(vector[0] > 1.5);
			Assert.assertTrue(vector[1] < 1.5);
		}
		
		// LU
		for(int i = 0; i < 4; i++) {
			double[] vector = vectors.get(indices[8 + i]);
			Assert.assertTrue(vector[0] < 1.5);
			Assert.assertTrue(vector[1] > 1.5);
		}
		
		// UU
		for(int i = 0; i < 4; i++) {
			double[] vector = vectors.get(indices[12 + i]);
			Assert.assertTrue(vector[0] > 1.5);
			Assert.assertTrue(vector[1] > 1.5);	
		}
	}
	
	@Test
	public void shouldBeAbleToGroup16PointsThatOnlyOccupyTwoExtremeQuadrants() {
		double[][] vectors = IntStream.range(0, 16).mapToDouble(i -> i < 8 ? -2.0 : 2.0)
			.mapToObj(v -> new double[] {v, -v, v})
			.toArray(n -> new double[n][]);
				
		
		int[] indices = IntStream.range(0, vectors.length).toArray();
		
		FractalSort2D fsort = new FractalSort2D(0, 2, i -> new int[0]);
		double[] comps = fsort.init(Arrays.asList(vectors));
		Assert.assertArrayEquals(new int[] {8, 0, 0, 8}, 
			fsort.groupQuad(comps, 
				new Quadrant(0, vectors.length, Fractal2D.Z_ORDER, 0), 
				indices, 
				new int[indices.length]
		));
		
		for(int i = 0; i < 8; i++) {
			double[] vector = vectors[indices[i]];
			Assert.assertTrue(Arrays.toString(vector), vector[0] < 0.0);
			Assert.assertTrue(Arrays.toString(vector), vector[2] < 0.0);
		}
	
		for(int i = 0; i < 8; i++) {
			double[] vector = vectors[indices[8 + i]];
			Assert.assertTrue(vector[0] > 0.0);
			Assert.assertTrue(vector[2] > 0.0);
		}
	}
	
	@Test
	public void shouldBeAbleToGroup16PointsThatOnlyOccupyTwoMiddleQuadrants() {
		double[][] vectors = IntStream.range(0, 16).mapToDouble(i -> i < 8 ? -2.0 : 2.0)
			.mapToObj(v -> new double[] {v, -v, v})
			.toArray(n -> new double[n][]);
				
		
		int[] indices = IntStream.range(0, vectors.length).toArray();
		
		FractalSort2D fsort = new FractalSort2D(2, 1, i -> new int[0]);
		double[] comps = fsort.init(Arrays.asList(vectors));
		Assert.assertArrayEquals(new int[] {0, 8, 8, 0}, 
			fsort.groupQuad(comps, 
				new Quadrant(0, vectors.length, Fractal2D.Z_ORDER, 0), 
				indices, 
				new int[indices.length]
		));
		
		for(int i = 0; i < 8; i++) {
			double[] vector = vectors[indices[i]];
			Assert.assertTrue(Arrays.toString(vector), vector[2] > 0.0);
			Assert.assertTrue(Arrays.toString(vector), vector[1] < 0.0);
		}
	
		for(int i = 0; i < 8; i++) {
			double[] vector = vectors[indices[8 + i]];
			Assert.assertTrue(vector[2] < 0.0);
			Assert.assertTrue(vector[1] > 0.0);
		}
	}
	
	@Test
	public void shouldBeAbleToSort16x16GridByZCurve() throws IOException {
		List<double[]> points = this.grid(16);
		int[] order = new FractalSort2D(0, 1, Fractal2D.Z_CURVE).sort(points);
		
		for(int i = 1; i < order.length; i++) {
			double[] a = points.get(order[i - 1]);
			double[] b = points.get(order[i]);
			
			double[] res = {15.1, 15.1};						
			
			for(int k = 0; k < 16; k++) {
				boolean aParity = a[k % 2] < res[k % 2];
				boolean bParity = b[k % 2] < res[k % 2];
				
				if(aParity == bParity) {
					break;
				}
				
				Assert.assertTrue(Arrays.toString(a)
						+ " and "
						+ Arrays.toString(b)
						+ " failed at " + res[k % 2],
						aParity && !bParity);
			}
		}
		
		this.render(this.orderBy(points, order), 0, 1).exportTo(null);
	}
	
	@Test
	public void shouldBeAbleToSort16x16GridByHilbertCurve() throws IOException {
		List<double[]> points = this.grid(16);
		int[] order = new FractalSort2D(0, 1, Fractal2D.HILBERT).sort(points);
		/*
		for(int i = 1; i < order.length; i++) {
			double[] a = points.get(order[i - 1]);
			double[] b = points.get(order[i]);
			
			double dist = (a[0] - b[0]) * (a[0] - b[0]) + (a[1] - b[1]) * (a[1] - b[1]);
			
			Assert.assertTrue(Arrays.toString(a)
					+ " and " + Arrays.toString(b)
					+ " failed at " + dist,
				(a[0] - b[0]) * (a[0] - b[0]) 
			  + (a[1] - b[1]) * (a[1] - b[1]) < 3.0);
		}
		*/
		this.render(this.orderBy(points, order), 0, 1).exportTo(null);
	}
	
	protected List<double[]> orderBy(List<double[]> vectors, int[] order) {
		return new AbstractList<double[]>() {

			@Override
			public double[] get(int index) {
				return vectors.get(order[index]);
			}

			@Override
			public int size() {
				return order.length;
			}
			
		};
	}
	
	protected JacobiSvg render(List<double[]> vectors, int xIdx, int yIdx) {
		float[] start = new Color(30, 150, 0).getColorComponents(new float[3]);
		float[] mid = new Color(255, 150, 0).getColorComponents(new float[3]);
		float[] finish = new Color(255, 0, 0).getColorComponents(new float[3]);
		
		JacobiSvg svg = new JacobiSvg();
		
		for(int i = 1; i < vectors.size(); i++) {
			double[] from = vectors.get(i - 1);
			double[] to = vectors.get(i);	
			
			float grad = i / (float) vectors.size();
			float[] a = start;
			float[] b = mid;
			if(grad > 0.5) {
				a = mid;
				b = finish;
				grad -= 0.5;
			}
			
			svg.line(from[xIdx], from[yIdx], 
				to[xIdx], to[yIdx], 
				new Color(
					a[0] + 2 * grad * (b[0] - a[0]),
					a[1] + 2 * grad * (b[1] - a[1]),
					a[2] + 2 * grad * (b[2] - a[2])
				)
			);
		}
		
		vectors.stream()
			.forEach(v -> svg.dot(v[xIdx], v[yIdx], Color.RED));
		return svg;
	}
	
	protected List<double[]> grid(int n) {
		return new AbstractList<double[]>() {

			@Override
			public double[] get(int index) {
				return new double[] { index % n, index / n};
			}

			@Override
			public int size() {
				return n * n;
			}
			
		};
	}

}
