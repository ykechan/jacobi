package jacobi.core.spatial.rtree;

import java.util.Arrays;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;

import jacobi.core.spatial.rtree.FractalSort2D.Aabb2;
import jacobi.core.spatial.rtree.FractalSort2D.Arguments;

public class FractalSort2DTest {
	
	@Test
	public void shouldBeAbleToSortA4x4GridInZOrder( ) {
		double[][] gridPts = this.grid(4);
		int[] result = new FractalSort2D(0, 1, Fractal2D.Z_CURVE).apply(Arrays.asList(gridPts));
		
		for(int r : result) {
			System.out.println(Arrays.toString(gridPts[r]));
		}
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
	
	protected double[][] grid(int n) {
		double[][] pts = new double[n * n][];
		int k = 0;
		for(int i = 0; i < n; i++){
			for(int j = 0; j < n; j++){
				System.out.println("i = " + i + ", j = " + j + ", k = " + k);
				pts[k++] = new double[] {i, j};
			}
		}
		return pts;
	}

}
