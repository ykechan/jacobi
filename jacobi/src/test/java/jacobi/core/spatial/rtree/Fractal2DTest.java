package jacobi.core.spatial.rtree;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.IntUnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;

public class Fractal2DTest {
	
	@Test
	public void shouldBeAbleToGenerateZCurveInGrid4() {
		int[][] grid1 = this.expand(new int[0][], Fractal2D.Z_CURVE);
		int[][] grid2 = this.expand(grid1, Fractal2D.Z_CURVE);
		int[][] grid4 = this.expand(grid2, Fractal2D.Z_CURVE);
		
		Assert.assertEquals(1, grid1.length);
		Assert.assertArrayEquals(new int[] { Fractal2D.Z_ORDER }, grid1[0]);
		
		int[][] visit2 = this.visit(grid2);
		Assert.assertEquals(2, visit2.length);
		Assert.assertArrayEquals(new int[] {0, 1}, visit2[0]);
		Assert.assertArrayEquals(new int[] {2, 3}, visit2[1]);
		
		int[][] visit4 = this.visit(grid4);
		Assert.assertEquals(4, visit4.length);
		Assert.assertArrayEquals(new int[] {0, 1, 4, 5}, visit4[0]);
		Assert.assertArrayEquals(new int[] {2, 3, 6, 7}, visit4[1]);
		Assert.assertArrayEquals(new int[] { 8, 9, 12, 13}, visit4[2]);
		Assert.assertArrayEquals(new int[] {10,11, 14, 15}, visit4[3]);
	}
	
	@Test
	public void shouldAllParityCurvesAreTheSameForZCurve() {
		int[][] grid1 = this.expand(new int[0][], Fractal2D.Z_CURVE);
		int[][] grid2 = this.expand(grid1, Fractal2D.Z_CURVE);
		int[][] grid4 = this.expand(grid2, Fractal2D.Z_CURVE);
		
		Consumer<int[][]> assertParity = g -> Arrays.stream(g)
			.flatMap(r -> Arrays.stream(r).boxed())
			.forEach(i -> Assert.assertEquals(Fractal2D.Z_ORDER, i % 256));
			
		assertParity.accept(grid1);
		assertParity.accept(grid2);
		assertParity.accept(grid4);
	}
	
	@Test
	public void shouldBeAbleToGenerateHilbertCurveInGrid4() {
		int[][] grid1 = this.expand(new int[0][], Fractal2D.HILBERT);
		int[][] grid2 = this.expand(grid1, Fractal2D.HILBERT);
		int[][] grid4 = this.expand(grid2, Fractal2D.HILBERT);
		
		Assert.assertEquals(1, grid1.length);
		Assert.assertArrayEquals(new int[] { Fractal2D.HILBERT_A }, grid1[0]);
		
		int[][] visit2 = this.visit(grid2);
		Assert.assertEquals(2, visit2.length);
		Assert.assertArrayEquals(new int[] {1, 2}, visit2[0]);
		Assert.assertArrayEquals(new int[] {0, 3}, visit2[1]);
		
		int[][] visit4 = this.visit(grid4);
		Assert.assertEquals(4, visit4.length);
		Assert.assertArrayEquals(new int[] { 5,  6,  9, 10}, visit4[0]);
		Assert.assertArrayEquals(new int[] { 4,  7,  8, 11}, visit4[1]);
		Assert.assertArrayEquals(new int[] { 3,  2, 13, 12}, visit4[2]);
		Assert.assertArrayEquals(new int[] { 0,  1, 14, 15}, visit4[3]);
	}
	
	@Test
	public void shouldBeAbleToTraceHilbertCurveInGrid8By4Neighbours() {
		int[][] grid1 = this.expand(new int[0][], Fractal2D.HILBERT);
		int[][] grid2 = this.expand(grid1, Fractal2D.HILBERT);
		int[][] grid4 = this.expand(grid2, Fractal2D.HILBERT);
		int[][] grid8 = this.expand(grid4, Fractal2D.HILBERT);
		
		Assert.assertEquals(8, grid8.length);
		Set<Integer> visited = new TreeSet<>();
		
		int start = IntStream.range(0, 8 * 8).filter(i -> grid8[i / 8][i % 8] / 256 == 0)
				.findAny()
				.orElseThrow(() -> new IllegalStateException("Unable to find start position"));
		
		Assert.assertEquals(7 * 8, start);		
		
		int pos = start;
		for(int k = 0; k < 64; k++){
			visited.add(pos);
			int order = grid8[pos / 8][pos % 8] / 256;
			if(order == 63) {
				// end point
				break;
			}
			int next = Arrays.stream(new int[] {
				pos % 8 > 0 ? pos - 1 : -1,
				pos % 8 < 7 ? pos + 1 : -1,
				pos + 8 < 8 * 8 ? pos + 8 : -1,
				pos - 8
			})
			.filter(i -> i >= 0).filter(i -> grid8[i / 8][i % 8] / 256 == order + 1)
			.findAny()
			.orElseThrow(() -> new IllegalStateException("Unable to find next for " + order));
			
			pos = next;
		}
		
		Assert.assertArrayEquals(
			IntStream.range(0, 64).toArray(), 
			visited.stream().mapToInt(Integer::intValue).toArray()
		);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldFailIfInputUnknownCurveInHilbert() {
		Fractal2D.HILBERT.apply(1234);
	}
	
	protected int[][] expand(int[][] grid, IntFunction<int[]> fractal) {
		if(grid.length == 0) {
			return new int[][] {{ fractal.apply(0)[0] }};
		}
		
		int[][] next = new int[2 * grid.length][2 * grid.length];
		for(int i = 0; i < grid.length; i++) {
			int[] row = grid[i];
			for(int j = 0; j < row.length; j++) {
				int mag = row[j] / 256;
				
				int ll = (row[j] % 256) % 4;
				int lu = ((row[j] % 256) / 4) % 4;
				int ul = ((row[j] % 256) / 16)% 4;
				int uu = ((row[j] % 256) / 64) % 4;
				
				int[] curve = fractal.apply(row[j] % 256);
				next[2*i    ][2*j] = 256 * (4 * mag + ll) + curve[0];
				next[2*i + 1][2*j] = 256 * (4 * mag + lu) + curve[1];
				next[2*i    ][2*j + 1] = 256 * (4 * mag + ul) + curve[2];
				next[2*i + 1][2*j + 1] = 256 * (4 * mag + uu) + curve[3];
			}
		}
		return next;
	}
		
	protected int[][] visit(int[][] grid) {
		return Arrays.stream(grid)
			.map(r -> Arrays.stream(r).map(i -> i / 256).toArray())
			.toArray(n -> new int[n][]);
	}
}
