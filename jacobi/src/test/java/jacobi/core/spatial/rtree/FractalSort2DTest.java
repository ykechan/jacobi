package jacobi.core.spatial.rtree;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;

import jacobi.core.spatial.rtree.FractalSort2D.Aabb2;
import jacobi.core.spatial.rtree.FractalSort2D.Arguments;
import jacobi.test.util.JacobiSvg;

public class FractalSort2DTest {
	
	@Test
	public void shouldBeAbleToSortA4x4GridInZOrder( ) {
		double[][] gridPts = this.grid(4);
		int[] result = new FractalSort2D(0, 1, Fractal2D.Z_CURVE).apply(Arrays.asList(gridPts));
		
		Assert.assertArrayEquals(new int[] {
			0, 1, 4, 5,
			2, 3, 6, 7,
			8, 9, 12, 13,
			10, 11, 14, 15
		}, result);
	}
	
	@Test
	public void shouldBeAbleToGroup4CornersInDescendingCodeOrder() {
		double[][] vertices = new double[][] {
			{-1, -1}, {-1, 1}, {1, -1}, {1, 1}
		};
		
		int order = 3 * Fractal2D.LL + 2 * Fractal2D.LU + 1 * Fractal2D.UL + 0 * Fractal2D.UU;
		int[] result = IntStream.range(0, vertices.length).toArray();
		
		int[] quad = new FractalSort2D(0, 1, v -> new int[0]).groupQuadrants(
			vertices, 
			new Arguments(new Aabb2(-1, -1, 1, 1, order), 0, vertices.length, 1), 
			result, new int[vertices.length]
		);
		
		Assert.assertArrayEquals(new int[]{3, 2, 1, 0}, result);
		Assert.assertArrayEquals(new int[]{1, 1, 1, 1}, quad);
	}
	
	@Test
	public void shouldBeAbleToSort4CornersInAnyPermutation() {
		double[][] vertices = new double[][] {
			{-1, -1}, {-1, 1}, {1, -1}, {1, 1}
		};
		
		int[] base = new int[] {
			Fractal2D.LL, Fractal2D.LU, Fractal2D.UL, Fractal2D.UU
		};
		for(int i = 0; i < 4; i++) {
			for(int j = 0; j < 4; j++) {
				if(i == j) {
					continue;
				}
				
				for(int k = 0; k < 4; k++) {
					if(i == k || j == k) {
						continue;
					}
					
					int n = 0;
					for(;n < 3; n++) {
						if(n != i && n != j && n != k) {
							break;
						}
					}
					int order = 0 * base[i] + 1 * base[j] + 2 * base[k] + 3 * base[n];
					
					int[] buffer = IntStream.range(0, vertices.length).toArray();
					int[] quad = new FractalSort2D(0, 1, v -> new int[0]).groupQuadrants(
						vertices,
						new Arguments(new Aabb2(-1, -1, 1, 1, order), 0, vertices.length, 0),
						buffer, new int[4]);
					
					Assert.assertArrayEquals(new int[] {i, j, k, n}, buffer);
					Assert.assertArrayEquals(new int[] {1, 1, 1, 1}, quad);					
				}
			}
		}
	}
	
	@Test
	public void shouldBeAbleToSort16x16GridInHilbertOrder() throws IOException {
		double[][] vertices = this.grid(16);
		int[] ord = new FractalSort2D(0, 1, Fractal2D.HILBERT).apply(Arrays.asList(vertices));

		this.render(vertices, ord).exportTo(null);
	}
	
	@Test
	public void shouldBeAbleToSort32x32GridInHilbertOrder() throws IOException {
		double[][] vertices = this.grid(32);
		int[] ord = new FractalSort2D(0, 1, Fractal2D.HILBERT).apply(Arrays.asList(vertices));
		
		this.render(vertices, ord).exportTo(null);
	}
	
	@Test
	public void shouldBeAbleToSort16x16GridInZOrder() throws IOException {
		double[][] vertices = this.grid(16);
		int[] ord = new FractalSort2D(0, 1, Fractal2D.Z_CURVE).apply(Arrays.asList(vertices));
		
		this.render(vertices, ord).exportTo(null);
	}
	
	protected JacobiSvg render(double[][] vertices, int[] ord) {
		JacobiSvg svg = new JacobiSvg();
		for(int i = 1; i < ord.length; i++){
			double[] from = vertices[ord[i - 1]];
			double[] to = vertices[ord[i]];
			
			svg.line(from[0], from[1], to[0], to[1], Color.BLACK);
		}
		
		for(double[] v : vertices){
			svg.dot(v[0], v[1], Color.RED);
		}
		
		return svg;
	}
	
	protected double[][] grid(int n) {
		double[][] pts = new double[n * n][];
		int k = 0;
		for(int i = 0; i < n; i++){
			for(int j = 0; j < n; j++){
				//System.out.println("i = " + i + ", j = " + j + ", k = " + k);
				pts[k++] = new double[] {j, i};
			}
		}
		return pts;
	}

}
