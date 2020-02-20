package jacobi.core.spatial.sort;

import java.util.Arrays;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;


/**
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
		System.out.println(Arrays.toString(basis));
	}
	
	@Test
	public void shouldBeAbleToEnhanceResolution() {
		int[] basis = this.generateBasis();
		long[] enhance = Arrays.stream(basis)
				.mapToLong(i -> this.generateEnhance(basis, i))
				.toArray();
		
		System.out.println(Arrays.toString(enhance));
	}
		
	
	protected long generateEnhance(int[] basis, int parity) {
		int[] diags = {3, 2, 1, 0, 7, 6, 5, 4};
		
		long enhance = 0;
		boolean desc = parity % 8 < 4; 
		for(int i = 0; i < 8; i++) {
			if(i == 4){
				desc = !desc;
			}
			
			int oct = (parity >> 3 * i) % 8;
			int start = (i == 0 || i == 4) ? oct : diags[oct];
			if(desc && start < 4) {
				start += 4;
			}
			if(!desc && start >= 4) {
				start -= 4;
			}
			
			enhance += 2 * start + (((i + 3) / 2) % 2);
			enhance *= 16;
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
