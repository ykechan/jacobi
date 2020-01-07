package jacobi.core.spatial.rtree;

import java.util.Arrays;
import java.util.function.IntUnaryOperator;

import org.junit.Assert;
import org.junit.Test;

public class FractalSort2DTest {
	
	@Test
	public void shouldBeAbleToGroupItemsInto4GroupsByMod4() {
		int[] items = {7, 13, 0, 2, 6, 7, 8, 11, 17, 9};
		int[] org = Arrays.copyOf(items, items.length);
		
		int[] len = new FractalSort2D(0, 1, v -> new int[0]).group(
			new FractalSort2D.Buffer(items, new int[items.length], 0, items.length), 			
			v -> v % 4
		);
		
		Assert.assertEquals(len[0], (int) Arrays.stream(org).filter(v -> v % 4 == 0).count());
		Assert.assertEquals(len[1], (int) Arrays.stream(org).filter(v -> v % 4 == 1).count());
		Assert.assertEquals(len[2], (int) Arrays.stream(org).filter(v -> v % 4 == 2).count());
		Assert.assertEquals(len[3], (int) Arrays.stream(org).filter(v -> v % 4 == 3).count());
		
		Assert.assertTrue(Arrays.stream(items).limit(len[0]).allMatch(v -> v % 4 == 0));
		Assert.assertTrue(Arrays.stream(items)
			.skip(len[0])
			.limit(len[1]).allMatch(v -> v % 4 == 1));
		Assert.assertTrue(Arrays.stream(items)
			.skip(len[0] + len[1])
			.limit(len[2]).allMatch(v -> v % 4 == 2));
		Assert.assertTrue(Arrays.stream(items)
			.skip(len[0] + len[1] + len[2])
			.limit(len[3])
			.allMatch(v -> v % 4 == 3));
	}
	
	@Test
	public void shouldBeAbleToGroupItemsWithNoItemInFirstCat() {
		double value = 1.0;
		int k = 0;
		while(value > 1e-12) {
			System.out.println("k = " + (k++) + ", " + (value /= 2.0));
		}
		System.out.println(0x10);
		
		int parity = Fractal2D.HILBERT_A;
		System.out.println((parity >> 4) % 4);
	}

}
