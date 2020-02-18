package jacobi.core.spatial.sort;

import static org.hamcrest.CoreMatchers.instanceOf;

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
	public void shouldBeAbleToGenerateAllBasisCurves() {		
		final int prime = this.curve();
		
		final int[] basis = new int[16];
		
		for(int i = 0; i < 4; i++){			
			int elevate = this.rotate(prime, i);
			int start = elevate % 8;
			// elevate stay
			basis[2 * start] = this.duplex(elevate, false);
			// elevate across
			basis[2 * start + 1] = this.duplex(elevate, true);
			
			int demote = this.rotate(prime + (4 + (4 << 3) + (4 << 6) + (4 << 9)), i);
			start = demote % 8;
			// demote stay
			basis[2 * start] = this.duplex(demote, false);
			// demote across
			basis[2 * start + 1] = this.duplex(demote, true);
		}
		
		System.out.println(Arrays.toString(basis));
	}
	
	protected int duplex(int floor, boolean diag) {
		int dir = floor % 8 < 4 ? 1 : -1;
		int next = diag ? this.rotate(floor, 3) : this.reverse(floor);
		return floor + (next + dir * (4 + 4 << 3 + 4 << 6 + 4 << 9) ) << 12;
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
}
