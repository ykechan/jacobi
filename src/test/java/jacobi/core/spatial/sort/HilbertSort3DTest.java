package jacobi.core.spatial.sort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;


/**
 * This is unit tests to HilbertSort3D and also code generator for Hilbert curve in 3-D.
 * 
 * There are multiple way to generalize Hilbert curve into 3-D and this implementation uses
 * the following construction.
 * 
 * A basic 3-D Hilbert curve can be constructed by joining two 2-D Hilbert curve end-to-front:
 * 
 * 
 *       4--------6
 *        \        \
 *         5        7
 *                  |
 *       0-------2  |
 *        \       \ |
 *         \       \|
 *          1       3
 *          
 * The numbers are the code for each octrants. This curve is joined by two 2-D Hilbert curve
 * goes in opposite direction in different plane in the z-dimension, and ends up in the starting
 * quadrant but in different plane in the z-dimension. Another curve is constructed if the curve
 * goes in the other direction, and would ends up in the diagonal quadrant but in different plane.
 * 
 * Depending on the starting and ending position, there are 16 curves, refers to as basis in this
 * text. (Always starts and ends in different plane).
 * 
 * To enhance the resolution, consider enhancing the curve in a plane:
 *  +---+
 *  |   |
 *  +   +
 *  
 *  The 4 octrants are crafted up into 8 octrants, which can be traversed by the basis like the following
 *  
 *    +--+ +--+
 *    |  | |  |
 *    +--* *--+
 *       x o
 *      /   \
 *     /     \
 *    o       x
 *    
 *  A o--x represents the basis curve which goes diagonally, and * curve represents the basis curve the
 *  stays transcended. The curve in another plane can be constructed similarly.
 *  
 *  Consider a basis curve that is ascending in z-dimension, to join the two halves 
 *  the first halve of the curve should end up in the upper half on the enhanced 
 *  resolution. Since each basis curve transends in z-dimension, and given 4 curves,
 *  the curve should start in the upper half on the enhanced resolution as well.
 * 
 * @author Y.K. Chan
 *
 */
public class HilbertSort3DTest {
	
	@Test
	public void shouldBeAbleToGenerateLowerBasis() {
		IntFunction<String> toStr = i -> IntStream.range(0, 4)
				.map(k -> (i >> 3 * k) % 8)
				.mapToObj(j -> (j / 2 == 0 ? "L" : "U") + (j % 2 == 0 ? "L" : "U"))
				.collect(Collectors.joining(","));
		
		int base = this.curve();
		int[] curves = new int[4];
		for(int i = 0; i < 4; i++) {
			int basis = this.rotate(base, i);
			curves[basis % 8] = basis;			
		}
		
		Assert.assertEquals("LU,LL,UL,UU", toStr.apply(curves[1]));
		Assert.assertEquals("LL,UL,UU,LU", toStr.apply(curves[0]));
		Assert.assertEquals("UL,UU,LU,LL", toStr.apply(curves[2]));
		Assert.assertEquals("UU,LU,LL,UL", toStr.apply(curves[3]));
	}	
	
	@Test
	public void shouldBeAbleToReverseLowerBasis() {
		IntFunction<String> toStr = i -> IntStream.range(0, 4)
				.map(k -> (i >> 3 * k) % 8)
				.mapToObj(j -> (j / 2 == 0 ? "L" : "U") + (j % 2 == 0 ? "L" : "U"))
				.collect(Collectors.joining(","));
		
		int base = this.curve();
		int[] curves = new int[4];
		for(int i = 0; i < 4; i++) {
			int basis = this.rotate(base, i);
			curves[basis % 8] = this.reverse(basis);			
		}
		
		Assert.assertEquals("UU,UL,LL,LU", toStr.apply(curves[1]));
		Assert.assertEquals("LU,UU,UL,LL", toStr.apply(curves[0]));
		Assert.assertEquals("LL,LU,UU,UL", toStr.apply(curves[2]));
		Assert.assertEquals("UL,LL,LU,UU", toStr.apply(curves[3]));
	}
	
	@Test
	public void shouldAllBasisCurveBeValid() {		
		final int[] basis = this.generateBasis();
		
		String[] octals = Arrays.stream(basis)
			.mapToObj(Integer::toOctalString)
			.map(s -> s.length() < 8 ? "0" + s : s)
			.map(s -> new StringBuilder(s).reverse().toString())
			.toArray(n -> new String[n]);		
		
		for(String oct : octals) {			
			for(int i = 0; i < 4; i++) {
				Assert.assertEquals(oct.charAt(0) - '0' < 4, oct.charAt(i) - '0' < 4);
			}
			
			for(int i = 4; i < 8; i++) {
				Assert.assertEquals(oct.charAt(4) - '0' < 4, oct.charAt(i) - '0' < 4);
			}
			
			for(int i = 0; i < 8; i++) {
				Assert.assertTrue("012345678".indexOf(oct.charAt(i)) >= 0);
			}
			
			boolean[] marked = new boolean[8];
			Arrays.fill(marked, false);
			for(int i = 0; i < 8; i++) {
				Assert.assertFalse(marked[oct.charAt(i) - '0']);
				marked[oct.charAt(i) - '0'] = true;
			}
		}
	}
	
	@Test
	public void shouldEvenBasisCurvesEndInTheSamePositionButAlteratePlane() {		
		final int[] basis = this.generateBasis();
		
		String[] octals = Arrays.stream(basis)
			.mapToObj(Integer::toOctalString)
			.map(s -> s.length() < 8 ? "0" + s : s)
			.map(s -> new StringBuilder(s).reverse().toString())
			.toArray(n -> new String[n]);		
		
		for(int i = 0; i < octals.length; i += 2) {
			String oct = octals[i];
			int start = oct.charAt(0) - '0';
			int finish = oct.charAt(7) - '0';			
			Assert.assertEquals(oct, 4, Math.abs(start - finish));
		}
	}
	
	@Test
	public void shouldOddBasisCurvesEndInTheDiagPositionButAlteratePlane() {		
		final int[] basis = this.generateBasis();
		
		String[] octals = Arrays.stream(basis)
			.mapToObj(Integer::toOctalString)
			.map(s -> s.length() < 8 ? "0" + s : s)
			.map(s -> new StringBuilder(s).reverse().toString())
			.toArray(n -> new String[n]);
		
		int[] diag = {7, 6, 5, 4, 3, 2, 1, 0};
		
		for(int i = 1; i < octals.length; i += 2) {
			String oct = octals[i];
			int start = oct.charAt(0) - '0';
			int finish = oct.charAt(7) - '0';
			Assert.assertEquals(oct, diag[start], finish);
		}
	}
	
	@Test
	public void shouldAllBasisCurvesVisitsItsImmediateNeighbor() {
		final int[] basis = this.generateBasis();		
		for(int curve : basis){
			for(int i = 0; i < 7; i++){
				int next = (curve >> 3 * i) % 8;
				int curr = (curve >> 3 * (i + 1)) % 8;
				
				int delta = Math.abs(next - curr);
				
				String errMsg = "curve " + Integer.toOctalString(curve)
					+ ", next = " + next
					+ ", curr = " + curr;
				if(next < 4 ^ curr < 4) {
					Assert.assertEquals(errMsg, 4, delta);
				}else {
					Assert.assertTrue(errMsg, delta > 0);
					Assert.assertTrue(errMsg, delta < 3);
				}
			}
		}
		//System.out.println(Arrays.toString(basis));
	}	
	
	@Test
	public void shouldTheBasisCurveUsedInImplSameAsGenerated() {
		Assert.assertArrayEquals(this.generateBasis(), HilbertSort3D.BASIS);
	}
	
	@Test
	public void shouldBeAbleToSortABasicCube() {
		int n = 2 * 2 * 2;
		double[][] grid = new double[n][];
		
		for(int i = 0; i < n; i++) {
			grid[i] = new double[] {i % 2, (i / 2) % 2, (i / 2 / 2) % 2};
		}
		
		HilbertSort3D sort3 = new HilbertSort3D(0, 1, 2);
		double[] comps = sort3.init(Arrays.asList(grid));
		
		int[] order = IntStream.range(0, grid.length).toArray();
		
		int[] counts = new HilbertSort3D(0, 1, 2).sort(comps, 
			new Category(0, grid.length, HilbertSort3D.BASIS[0], 0), 
			order
		);		
		Assert.assertEquals(8, order.length);
		Assert.assertEquals(8, counts.length);
		
		Assert.assertArrayEquals(new int[] {1, 1, 1, 1, 1, 1, 1, 1},  counts);
		
		Assert.assertArrayEquals(new double[] {0.0, 0.0, 0.0}, grid[order[0]], 1e-12);
		Assert.assertArrayEquals(new double[] {1.0, 0.0, 0.0}, grid[order[1]], 1e-12);
		Assert.assertArrayEquals(new double[] {1.0, 1.0, 0.0}, grid[order[2]], 1e-12);
		Assert.assertArrayEquals(new double[] {0.0, 1.0, 0.0}, grid[order[3]], 1e-12);
		
		Assert.assertArrayEquals(new double[] {0.0, 1.0, 1.0}, grid[order[4]], 1e-12);
		Assert.assertArrayEquals(new double[] {1.0, 1.0, 1.0}, grid[order[5]], 1e-12);
		Assert.assertArrayEquals(new double[] {1.0, 0.0, 1.0}, grid[order[6]], 1e-12);
		Assert.assertArrayEquals(new double[] {0.0, 0.0, 1.0}, grid[order[7]], 1e-12);
		
		Assert.assertArrayEquals(this.generateBasis(), HilbertSort3D.BASIS);
	}
	
	@Test
	public void shouldBeAbleToGroupACubeIntoOctrantsInLowerZ() {
		int n = 4 * 4 * 4;
		double[][] grid = new double[n][];
		
		for(int i = 0; i < n; i++) {
			grid[i] = new double[] {i % 4, (i / 4) % 4, (i / 4 / 4) % 4};
		}
		
		HilbertSort3D sort3 = new HilbertSort3D(0, 1, 2);
		double[] comps = sort3.init(Arrays.asList(grid));
		
		int[] order = IntStream.range(0, grid.length).toArray();
		
		int[] counts = new HilbertSort3D(0, 1, 2).sort(comps, 
			new Category(0, grid.length, HilbertSort3D.BASIS[0], 0), 
			order
		);		
		
		
		Assert.assertArrayEquals(new int[] {8, 8, 8, 8, 8, 8, 8, 8}, counts);
		// lower before upper in z-dimension
		for(int i = 0; i < order.length; i++) {
			int k = order[i];
			Assert.assertTrue(i < n / 2 ^ grid[k][2] > 1.5);
		}
		
		this.assertAllMatch(Arrays.asList(grid), 0, 8, order, 
				v -> v[0] < 1.5 || v[1] < 1.5);
		this.assertAllMatch(Arrays.asList(grid), 8, 8, order, 
				v -> v[0] > 1.5 || v[1] < 1.5);
		this.assertAllMatch(Arrays.asList(grid), 16, 8, order, 
				v -> v[0] > 1.5 || v[1] > 1.5);
		this.assertAllMatch(Arrays.asList(grid), 24, 8, order, 
				v -> v[0] < 1.5 || v[1] > 1.5);
	}
	
	@Test
	public void shouldBeAbleToGroupACubeIntoOctrantsInUpperZ() {
		int n = 4 * 4 * 4;
		double[][] grid = new double[n][];
		
		for(int i = 0; i < n; i++) {
			grid[i] = new double[] {i % 4, (i / 4) % 4, (i / 4 / 4) % 4};
		}
		
		HilbertSort3D sort3 = new HilbertSort3D(0, 1, 2);
		double[] comps = sort3.init(Arrays.asList(grid));
		
		int[] order = IntStream.range(0, grid.length).toArray();
		
		int[] counts = new HilbertSort3D(0, 1, 2).sort(comps, 
			new Category(0, grid.length, HilbertSort3D.BASIS[0], 0), 
			order
		);		
		
		
		Assert.assertArrayEquals(new int[] {8, 8, 8, 8, 8, 8, 8, 8}, counts);
		// lower before upper in z-dimension
		for(int i = 0; i < order.length; i++) {
			int k = order[i];
			Assert.assertTrue(i < n / 2 ^ grid[k][2] > 1.5);
		}
		this.assertAllMatch(Arrays.asList(grid), 56, 8, order, 
				v -> v[0] < 1.5 || v[1] < 1.5);
		this.assertAllMatch(Arrays.asList(grid), 48, 8, order, 
				v -> v[0] > 1.5 || v[1] < 1.5);
		this.assertAllMatch(Arrays.asList(grid), 40, 8, order, 
				v -> v[0] > 1.5 || v[1] > 1.5);
		this.assertAllMatch(Arrays.asList(grid), 32, 8, order, 
				v -> v[0] < 1.5 || v[1] > 1.5);
	}
	
	@Test
	public void shouldBeAbleToEnhancedResolutionReferenceTheBasisCurveOnly() {
		int[] basis = this.generateBasis();
		long[] enhance = this.generateEnhance();
		
		Assert.assertEquals(basis.length, enhance.length);
		for(long transit : enhance) {
			// at most 32-bit
			Assert.assertTrue(transit <= 2L * Integer.MAX_VALUE);
		}
	}
	
	@Test
	public void shouldEnhanceFollowModularity() {
		long[] enhance = this.generateEnhance();
		
		for(long transit : enhance) {
			Assert.assertEquals(1, (transit     ) % 2);
			Assert.assertEquals(0, (transit >> 4) % 2);
			Assert.assertEquals(0, (transit >> 8) % 2);
			Assert.assertEquals(1, (transit >> 12) % 2);
			
			Assert.assertEquals(1, (transit >> 16) % 2);
			Assert.assertEquals(0, (transit >> 20) % 2);
			Assert.assertEquals(0, (transit >> 24) % 2);
			Assert.assertEquals(1, (transit >> 28) % 2);
		}
	}
	
	@Test
	public void shouldBeAbleToGenerateEnhance() {
		long[] enhance = this.generateEnhance();
		Assert.assertArrayEquals(HilbertSort3D.ENHANCE, enhance);
	}
	
	@Test
	public void shouldBeAbleToSortA4x4x4CubeInOctrantLLL() {
		int n = 4 * 4 * 4;
		double[][] grid = new double[n][];
		
		for(int i = 0; i < n; i++) {
			grid[i] = new double[] {i % 4, (i / 4) % 4, (i / 4 / 4) % 4};
		}
		
		HilbertSort3D sort3 = new HilbertSort3D(0, 1, 2);
		int[] order = sort3.sort(Arrays.asList(grid));
		
		List<double[]> points = new ArrayList<>();
		for(int i = 0; i < 8; i++) {
			points.add(grid[order[i]]);
		}
		
		Assert.assertArrayEquals(new double[] {0.0, 0.0, 1.0}, points.get(0), 1e-12);
		Assert.assertArrayEquals(new double[] {1.0, 0.0, 1.0}, points.get(1), 1e-12);
		Assert.assertArrayEquals(new double[] {1.0, 1.0, 1.0}, points.get(2), 1e-12);
		Assert.assertArrayEquals(new double[] {0.0, 1.0, 1.0}, points.get(3), 1e-12);
		
		Assert.assertArrayEquals(new double[] {0.0, 1.0, 0.0}, points.get(4), 1e-12);
		Assert.assertArrayEquals(new double[] {0.0, 0.0, 0.0}, points.get(5), 1e-12);
		Assert.assertArrayEquals(new double[] {1.0, 0.0, 0.0}, points.get(6), 1e-12);
		Assert.assertArrayEquals(new double[] {1.0, 1.0, 0.0}, points.get(7), 1e-12);
	}
	
	@Test
	@Ignore
	public void shouldBeAbleToSortA4x4x4CubeInOctrantULU() {
		int n = 4 * 4 * 4;
		double[][] grid = new double[n][];
		
		for(int i = 0; i < n; i++) {
			grid[i] = new double[] {i % 4, (i / 4) % 4, (i / 4 / 4) % 4};
		}
		
		HilbertSort3D sort3 = new HilbertSort3D(0, 1, 2);
		int[] order = sort3.sort(Arrays.asList(grid));
		
		List<double[]> points = new ArrayList<>();
		for(int i = 0; i < 8; i++) {
			points.add(grid[order[6 * 8 + i]]);
		}
		
		Assert.assertArrayEquals(new double[] {2.0, 1.0, 2.0}, points.get(0), 1e-12);
		Assert.assertArrayEquals(new double[] {3.0, 1.0, 2.0}, points.get(1), 1e-12);
		Assert.assertArrayEquals(new double[] {3.0, 0.0, 2.0}, points.get(2), 1e-12);
		Assert.assertArrayEquals(new double[] {2.0, 0.0, 2.0}, points.get(3), 1e-12);
		
		Assert.assertArrayEquals(new double[] {2.0, 0.0, 3.0}, points.get(4), 1e-12);
		Assert.assertArrayEquals(new double[] {3.0, 0.0, 3.0}, points.get(5), 1e-12);
		Assert.assertArrayEquals(new double[] {3.0, 1.0, 3.0}, points.get(6), 1e-12);
		Assert.assertArrayEquals(new double[] {2.0, 1.0, 3.0}, points.get(7), 1e-12);
	}
	
	protected void assertAllMatch(List<double[]> list, int begin, int length, int[] order,
			Predicate<double[]> fn) {
		int count = 0;
		for(int i = begin; i < begin + length; i++) {
			int k = order[i];
			double[] v = list.get(k);
			
			Assert.assertTrue(Arrays.toString(v), fn.test(v));
			count++;
		}
		Assert.assertEquals(length, count);
	}
		
	protected long[] generateEnhance() {
		int[] basis = this.generateBasis();
		return Arrays.stream(basis)
			.mapToLong(p -> this.generateEnhance(basis, p))
			.toArray();
	}
	
	protected long generateEnhance(int[] basis, int parity) {
		int[] diags = {3, 2, 1, 0, 7, 6, 5, 4};
		
		long enhance = 0;
		boolean desc = parity % 8 < 4; 
		for(int i = 0; i < 8; i++) {
			
			if(i == 4){
				desc = !desc;
			}
			
			int oct = (parity >> (3 * i)) % 8;
			int start = (i == 3 || i == 7) ? diags[oct] : oct;
			if(desc && start < 4) {
				start += 4;
			}
			if(!desc && start >= 4) {
				start -= 4;
			}
			
			int mod = ((i + 3) / 2) % 2;			
			long base = 1 << 4 * i;
			
			enhance += base * (2 * start + mod);			
			desc = !desc;
		}
		return enhance;		
	}
	
	protected int[] generateBasis() {
		final int prime = this.curve();
		
		final int[] basis = new int[16];
		
		for(int i = 0; i < 4; i++){	
			int elevate = this.rotate(prime, i);
			int start = elevate % 8;
			// elevate stay
			basis[2 * start] = this.duplex(elevate, false);
			// elevate across
			basis[2 * start + 1] = this.duplex(elevate, true);
			
			int demote = this.rotate(prime + ELEVATE, i);
			start = demote % 8;
			// demote stay
			basis[2 * start] = this.duplex(demote, false);
			// demote across
			basis[2 * start + 1] = this.duplex(demote, true);
		}
		return basis;
	}
	
	protected int duplex(int floor, boolean diag) {
		int dir = floor % 8 < 4 ? 1 : -1;
		int next = diag ? this.rotate(floor, 3) : this.reverse(floor);
		return floor + ((next + dir * ELEVATE ) << 12);
	}
	
	protected int reverse(int code) {
		int rev = 0;
		for(int i = 0; i < 4; i++) {
			rev += ( (code >> (9 - 3 * i)) % 8 ) << 3 * i ;
		}
		return rev;
	}
	
	protected int rotate(int code, int delta) {
		for(int i = 0; i < delta; i++){
			int mod = code % 8;
			code = (code / 8) + (mod << 9);
		}
		return code;
	}
	

	protected int curve() {
		return 1 + (0 << 3) + (2 << 6) + (3 << 9);
	}
	
	protected static final int ELEVATE = 4 + (4 << 3) + (4 << 6) + (4 << 9);
}
