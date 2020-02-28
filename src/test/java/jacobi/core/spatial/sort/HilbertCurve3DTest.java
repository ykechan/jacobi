package jacobi.core.spatial.sort;

import java.util.Arrays;
import java.util.function.IntBinaryOperator;
import java.util.function.ToIntBiFunction;
import java.util.function.ToLongBiFunction;
import java.util.function.ToLongFunction;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;

import jacobi.core.spatial.sort.HilbertSort3D.BasisType;

/**
 * This class contains code generation for Hilbert Sort 3-D.
 * 
 * <p>A Hilbert Curve 3-D in its simplest order can be obtained by joining 2 Hilbert curve in 2-D
 * in different dimension and moving direction, for example
 * 
 * <pre>
 *      4 ---- 5               4 ---- 5               4      5   
 *       \      \               \                     |\     |\  
 *        \      \               \                    | \    | \ 
 *         6      7               6------7            |  6------7
 *                |                      |            |      |  
 *      0 ---- 1  |            0 ---- 1  |            0      1  
 *       \      \ |             \      \ |             \      \  
 *        \      \|              \      \|              \      \ 
 *         2      3               2      3               2      3
 * </pre>
 * 
 * There are multiple ways to construct the Hilbert curve in 3-D depending on the choice
 * of dimension in connecting or rotating. This library adopts the following construction.
 * </p>
 * 
 * <p>The 8 octants are encoding by their upper/lower parity in x, y, z dimension in
 * respective least to most significant bit. To better visualize in the mental picture,
 * this text refers to moving in x-dimension left/right, y-dimension forward/backward,
 * z-dimension ascending/descending.</p>
 * 
 * <p>Consider the first and second curve illustrated above, these are constructed by
 * connecting two Hilbert curve in the x-y dimension in reverse or rotated direction.
 * Regardless of the starting position, these are the only two possible construction
 * of connecting 2 x-y curve, and it ends up in either the same x-y position transcended,
 * or diagonal x-y position transcended. This text refers to the first kind stay curve,
 * and the other diag curve.</p>
 * 
 * <p>Consider the basic x-y curve, its resolution can be enhance by going through the
 * enhance octants by diag -&gt; stay -&gt; stay -&gt; diag. The x-y curve transended
 * can be enhanced similarly. Unfortunately, since the stay and diag curve are both 
 * transending, and there are 4 quadrants after enhancing a x-y curve, we stay at the
 * same z-plane at the end of the curve but to connect with other curves, we need to
 * start at the bottom z-plane and ends at the top z-plane, otherwise the z-plane
 * connection is getting stretched.</p>
 * 
 * <p>Thus the third curve illustrated above is introduced, which is constructed by
 * connecting two x-z or y-z curve. This kind of curve ends up in the adjacent octant
 * of the same z-plane. This text refers to the curve that ends up in adjacent x octant
 * right curve, and adjacent y octant forward curve.</p>
 * 
 * <p>For each starting position, there are 4 ending position available using these curves:
 * stay, diag, right and forward, thus there are totally 32 curves. This text refers them
 * as basis curve. For example stay[0] is the stay curve starts at the 0 octant.</p>
 * 
 * <p>Thus the stay/diag curve can be enhanced by diag -&gt; stay -&gt; stay -&gt; 
 * right, and transend in the other plane, right -&gt; stay -&gt; stay -&gt; diag.
 * 
 * A overhead view would be similar to the following:
 * <pre>
 *       + +               + +
 *       . x--*            x .--*     
 *      /                 /
 *     x                 .
 * </pre>
 * where x refers to starting position, . be the ending position, + be the stay curve, and
 * * is transcending to another plane. Stay/diag curve differs from the direction after
 * transcension.
 * 
 * </p>
 * 
 * @author Y.K. Chan
 *
 */
public class HilbertCurve3DTest {
	
	@Test
	public void shouldBeAbleToEnhanceAllBasisCurves() {
		Octant[] octants = Octant.all();
		long[] enhances = new long[octants.length * BasisType.values().length];
				
		for(Octant start : octants) {
			int index = BasisType.values().length * start.toInt();
			for(BasisType type : BasisType.values()) {
				enhances[index + type.ordinal()] = this.enhance(type, start);
			}
		}
		
		Assert.assertArrayEquals(enhances, HilbertSort3D.ENHANCE);
	}
	
	@Test
	public void shouldBeAbleToEnhanceAllStayCurves() {
		Octant[] octants = Octant.all();
		for(Octant start : octants) {
			Octant[] lowRes = this.basisOf(BasisType.STAY, start);
			long indices = this.enhance(BasisType.STAY, start);
			this.assertExitCloseToEntrance(lowRes, indices);
		}
		
		long enhanceStay = this.enhance(BasisType.STAY, octants[0]);
		
		System.out.println(enhanceStay);
		System.out.println(enhanceStay % 32);
	}
	
	@Test
	public void shouldBeAbleToEnhanceAllDiagCurves() {
		Octant[] octants = Octant.all();
		for(Octant start : octants) {
			Octant[] lowRes = this.basisOf(BasisType.DIAG, start);
			long indices = this.enhance(BasisType.DIAG, start);
			this.assertExitCloseToEntrance(lowRes, indices);
		}
	}
	
	@Test
	public void shouldBeAbleToEnhanceAllRightCurves() {
		Octant[] octants = Octant.all();
		for(Octant start : octants) {
			Octant[] lowRes = this.basisOf(BasisType.RIGHT, start);
			long indices = this.enhance(BasisType.RIGHT, start);
			this.assertExitCloseToEntrance(lowRes, indices);
		}
	}
	
	@Test
	public void shouldBeAbleToGenerateAllBasisCurve() {
		Octant[] octants = Octant.all();
		int[] basis = new int[octants.length * BasisType.values().length];
		for(Octant start : octants) {
			int index = start.toInt() * BasisType.values().length;
			for(BasisType type : BasisType.values()) {
				basis[index + type.ordinal()] = this.encode(this.basisOf(type, start));
			}
		}
		
		Assert.assertArrayEquals(HilbertSort3D.BASIS, basis);
	}
	
	@Test
	public void shouldBeAbleToGenerateStayCurveWithHilbertProperties() {
		for(Octant start : Octant.all()) {	
			Octant[] stay = this.stayAt(start);
			Assert.assertEquals("Invalid stay curve " 
					+ " @" + start + ": "
					+ Arrays.asList(stay), 
					start.asc(), stay[stay.length - 1]);
			
			this.assertVisitAllOnce(stay);
			this.assertNeighbourhood(stay);
			this.assertTranscendence(stay);
		}
	}
	
	@Test
	public void shouldBeAbleToGenerateDiagCurveWithHilbertProperties() {
		for(Octant start : Octant.all()) {			
			Octant[] diag = this.diagOf(start);
			Assert.assertEquals("Invalid diag curve " 
					+ " @" + start + ": "
					+ Arrays.asList(diag), 
					new Octant(!start.x, !start.y, !start.z), 
					diag[diag.length - 1]);
			
			this.assertVisitAllOnce(diag);
			this.assertNeighbourhood(diag);
			this.assertTranscendence(diag);
		}
	}
	
	@Test
	public void shouldBeAbleToGenerateRightCurveWithHilbertProperties() {
		for(Octant start : Octant.all()) {
			Octant[] right = this.rightOf(start);
			Assert.assertEquals("Invalid right curve " 
					+ " @" + start + ": "
					+ Arrays.asList(right), 
					start.right(), right[right.length - 1]);
			
			this.assertVisitAllOnce(right);
			this.assertNeighbourhood(right);
			
			for(int i = 0; i < 4; i++) {
				Assert.assertTrue(right[i].x ^ right[i + 4].x);
			}
			Assert.assertEquals(start.z, right[0].z);
			Assert.assertEquals(start.z, right[1].z);
			
			Assert.assertEquals(start.z, right[7].z);
			Assert.assertEquals(start.z, right[6].z);
			
			for(int i = 2; i < 6; i++) {
				Assert.assertEquals(!start.z, right[i].z);
			}
		}
	}
	
	@Test
	public void shouldBeAbleToGenerateForwardCurveWithHilbertProperties() {
		for(Octant start : Octant.all()) {
			Octant[] right = this.forwardOf(start);
			Assert.assertEquals("Invalid forward curve " 
					+ " @ " + start + ": "
					+ Arrays.asList(right), 
					start.forward(), right[right.length - 1]);
			
			this.assertVisitAllOnce(right);
			this.assertNeighbourhood(right);
			
			for(int i = 0; i < 4; i++) {
				Assert.assertTrue(right[i].y ^ right[i + 4].y);
			}
			Assert.assertEquals(start.z, right[0].z);
			Assert.assertEquals(start.z, right[1].z);
			
			Assert.assertEquals(start.z, right[7].z);
			Assert.assertEquals(start.z, right[6].z);
			
			for(int i = 2; i < 6; i++) {
				Assert.assertEquals(!start.z, right[i].z);
			}
		}
	}
	
	protected void assertExitCloseToEntrance(Octant[] lowRes, long resolve) {
		Octant[] octants = Octant.all();
		
		int count = 0;
		for(int i = 1; i < lowRes.length; i++) {
			int before = (int) ((resolve >> (i - 1) * 5) % 32);
			Octant exit = octants[before / BasisType.values().length];
			BasisType type = Arrays.stream(BasisType.values())
				.filter(b -> b.ordinal() == before % BasisType.values().length)
				.findAny()
				.orElseThrow(() -> new IllegalArgumentException("Basis Type not found for " + before));
			
			switch(type) {
				case DIAG:
					exit = exit.across();
					break;
				case FORWARD:
					exit = exit.forward();
					break;
				case RIGHT:
					exit = exit.right();
					break;
				case STAY:
					exit = exit.asc();
					break;
				default:
					throw new UnsupportedOperationException("Unknown basis " + type);			
			}
			
			int exitX = (lowRes[i - 1].x ? 2 : 0) + (exit.x ? 1 : 0);
			int exitY = (lowRes[i - 1].y ? 2 : 0) + (exit.y ? 1 : 0);
			int exitZ = (lowRes[i - 1].z ? 2 : 0) + (exit.z ? 1 : 0);
			
			Octant enter = octants[(int) ((resolve >> i * 5) % 32) 
			                       / BasisType.values().length];
			
			int enterX = (lowRes[i].x ? 2 : 0) + (enter.x ? 1 : 0);
			int enterY = (lowRes[i].y ? 2 : 0) + (enter.y ? 1 : 0);
			int enterZ = (lowRes[i].z ? 2 : 0) + (enter.z ? 1 : 0);
			
			System.out.println("[" + exitX +"," + exitY + "," + exitZ +"] -> "
					+ "[" + enterX + "," + enterY + "," + enterZ + " ]");
			
			Assert.assertEquals("[" + exitX +"," + exitY + "," + exitZ +"] -> "
				+ "[" + enterX + "," + enterY + "," + enterZ + " ]",
				1, 
				Math.abs(enterX - exitX)
			  + Math.abs(enterY - exitY)
			  + Math.abs(enterZ - exitZ)
			);
			count++;
		}
		
		Assert.assertEquals(7, count);
	}
	
	protected void assertTranscendence(Octant[] curve) {
		int n = curve.length / 2;
		for(int i = 0; i < n; i++) {
			Assert.assertTrue(curve[i].z ^ curve[n + i].z);
		}
	}
	
	protected void assertNeighbourhood(Octant[] curve) {
		for(int i = 1; i < curve.length; i++) {
			Assert.assertEquals(1, curve[i].delta(curve[i - 1]));
		}
	}
	
	protected void assertVisitAllOnce(Octant[] curve) {
		boolean[] visit = new boolean[8];
		Arrays.fill(visit, false);
		
		for(Octant oct : curve) {
			Assert.assertFalse(visit[oct.toInt()]);
			visit[oct.toInt()] = true;
		}
		
		for(int i = 0; i < visit.length; i++) {
			Assert.assertTrue(visit[i]);
		}
	}
	
	protected long enhance(BasisType type, Octant start) {
		
		Octant[] curve = this.basisOf(type, start);
		Octant[] init = new Octant[curve.length];
		init[0] = curve[0];
		for(int i = 1; i < init.length; i++) {
			init[i] = curve[i].forward().right();
		}
		
		if(type == BasisType.STAY || type == BasisType.DIAG){
			init[4] = curve[3].x == curve[3].y 
					? init[3].right()
					: init[3].forward();
		}
		
		if(type == BasisType.RIGHT || type == BasisType.FORWARD){
			init[2] = type == BasisType.RIGHT ? init[2].right() : init[2].forward();
			init[6] = type == BasisType.RIGHT ? init[6].right() : init[6].forward();
		}
			
		int base = 5;
		BasisType[] resolve = {};
			
		if(type == BasisType.STAY || type == BasisType.DIAG){
			resolve = new BasisType[]{
				BasisType.DIAG, BasisType.STAY, BasisType.STAY, 
				curve[3].x == curve[3].y ? BasisType.RIGHT : BasisType.FORWARD,
				curve[3].x == curve[3].y ? BasisType.RIGHT : BasisType.FORWARD, 
				BasisType.STAY, BasisType.STAY, BasisType.DIAG
			};	
			
		}
		
		if(type == BasisType.RIGHT || type == BasisType.FORWARD){
			resolve = new BasisType[] {
				BasisType.DIAG, type, type,
				BasisType.STAY, BasisType.STAY,
				type, type, BasisType.DIAG
			};			
		}
		
		if(resolve.length != init.length) {
			throw new UnsupportedOperationException("Unknown basis " + type);
		}
		
		boolean upper = init[0].z;
		for(int i = 0; i < resolve.length; i++) {
			BasisType octtype = resolve[i];	
			if(i > 0 && curve[i].z != curve[i - 1].z) {
				upper = curve[i - 1].z;
			}
			
			if(upper != init[i].z){
				init[i] = init[i].asc();
			}
			
			if(octtype == BasisType.STAY || octtype == BasisType.DIAG) {
				upper = !upper;
			}
			
		}
		
		ToLongBiFunction<Octant, BasisType> idxFn = (oct, t) -> 
			BasisType.values().length * oct.toInt() + t.ordinal();
					
		long code = 0L;
		for(int i = 0; i < resolve.length; i++) {
			code += idxFn.applyAsLong(init[i], resolve[i]) << (base * i);
		}
		return code;
	}
	
	protected Octant[] basisOf(BasisType type, Octant start) {
		switch(type) {
			case STAY:
				return this.stayAt(start);
				
			case DIAG:
				return this.diagOf(start);
				
			case RIGHT:
				return this.rightOf(start);
				
			case FORWARD:
				return this.forwardOf(start);
				
			default:
				break;
		}
		throw new IllegalArgumentException("Unknown basis type " + type);
	}
	
	protected Octant[] stayAt(Octant start) {
		Octant[] xy = this.xyCurve(start);
		Octant[] curve = Arrays.copyOf(xy, 2 * xy.length);
		
		for(int i = 0; i < xy.length; i++) {
			curve[xy.length + i] = xy[xy.length - 1 - i].asc(); 
		}
				
		return curve;
	}
	
	protected Octant[] diagOf(Octant start) {
		Octant[] xy = this.xyCurve(start);
		Octant[] rev = this.xyCurve(xy[xy.length - 1].asc());
		
		Octant[] curve = Arrays.copyOf(xy, xy.length + rev.length);
		System.arraycopy(rev, 0, curve, xy.length, rev.length);
		
		return curve;
	}
	
	protected Octant[] rightOf(Octant start) {
		return this.adj(start, true);
	}
	
	protected Octant[] forwardOf(Octant start) {
		return this.adj(start, false);
	}
	
	protected Octant[] adj(Octant start, boolean toRight) {
		UnaryOperator<Octant> next = toRight ? Octant::forward : Octant::right;
		UnaryOperator<Octant> mirror = toRight ? Octant::right : Octant::forward;
		
		Octant[] curve = new Octant[8];
		curve[0] = start;
		curve[1] = next.apply(start);
		curve[2] = curve[1].asc();
		curve[3] = curve[0].asc();
		
		for(int i = 0; i < 4; i++) {
			curve[i + 4] = mirror.apply(curve[3 - i]);
		}
		return curve;
	}
	
	protected Octant[] xyCurve(Octant start) {
		Octant begin = start.z ? start.asc() : start;
		for(int k = 0; k < 4; k++) {
			Octant[] xy = this.xyCurve(k);
			if(!xy[0].equals(begin)) {
				continue;
			}
			
			if(start.z) {
				for(int i = 0; i < xy.length; i++) {
					xy[i] = xy[i].asc();
				}
			}
			return xy;
		}
		throw new IllegalArgumentException("Invalid octant " + start);
	}
	
	protected Octant[] xyCurve(int rot) {
		Octant[] basic = { 
			Octant.lower(false, false),
			Octant.lower(true, false),
			Octant.lower(true, true),
			Octant.lower(false, true)
		};
		return IntStream.range(0, 4)
			.map(i -> rot + i)
			.mapToObj(k -> basic[k % basic.length])
			.toArray(n -> new Octant[n]);
	}
	
	protected int encode(Octant[] curve) {
		int octs = 0;
		for(int i = 0; i < curve.length; i++) {
			octs += curve[i].toInt() << 3 * i;
		}
		return octs;
	}
		
	
	protected static class Octant {
		
		public static Octant[] all() {
			return IntStream.range(0, 8)
				.mapToObj(k -> new Octant(k % 2 > 0, (k / 2) % 2 > 0, k > 3 ))
				.toArray(n -> new Octant[n]);
		}
		
		public static Octant lower(boolean x, boolean y) {
			return new Octant(x, y, false);
		}
		
		public final boolean x, y, z;

		public Octant(boolean x, boolean y, boolean z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}
		
		public Octant right() {
			return new Octant(!this.x, this.y, this.z);
		}
		
		public Octant forward() {
			return new Octant(this.x, !this.y, this.z);
		}
		
		public Octant asc() {
			return new Octant(this.x, this.y, !this.z);
		}
		
		public Octant across() {
			return new Octant(!this.x, !this.y, !this.z);
		}
		
		public int delta(Octant oct) {
			return (this.x ^ oct.x ? 1 : 0)
				 + (this.y ^ oct.y ? 1 : 0)
				 + (this.z ^ oct.z ? 1 : 0);
		}
		
		public int toInt() {
			return (this.x ? 1 : 0)
				+ (this.y ? 2 : 0)
				+ (this.z ? 4 : 0);
		}

		@Override
		public boolean equals(Object obj) {
			if(obj instanceof Octant) {
				Octant oth = (Octant) obj;
				return this.x == oth.x
					&& this.y == oth.y
					&& this.z == oth.z;
			}
			return false;
		}

		@Override
		public String toString() {
			return (this.x ? "U" : "L")
				+ (this.y ? "U" : "L")
				+ (this.z ? "U" : "L");
		}
		
	}
	
}
