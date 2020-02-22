package jacobi.core.stats.select;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;

public class QuickSelectTest {
	
	@Test
	public void shouldBeAbleToSelect3From5() {
		double[] array = {1, 2, 3, 4, 5};
		Assert.assertEquals(3, 
			new QuickSelect((a, i, j, t) -> i).select(array, 0, 5, 3));
		Assert.assertEquals(4, array[3], 1e-12);
		
		array = new double[]{10, 9, 8, 7, 6};
		Assert.assertEquals(3, 
			new QuickSelect((a, i, j, t) -> i).select(array, 0, 5, 3));
		Assert.assertEquals(9, array[3], 1e-12);
	}
	
	@Test
	public void shouldBeAbleToSelect2From5() {
		double[] array = {1, 2, 3, 4, 5};
		Assert.assertEquals(2, 
			new QuickSelect((a, i, j, t) -> i).select(array, 0, 5, 2));
		Assert.assertEquals(3, array[2], 1e-12);
		
		array = new double[]{10, 9, 8, 7, 6};
		Assert.assertEquals(2, 
			new QuickSelect((a, i, j, t) -> i).select(array, 0, 5, 2));
		Assert.assertEquals(8, array[2], 1e-12);
	}
	
	@Test
	public void shouldBeAbleToSelectFromRandomSeqWithHeadAsPivot() {
		//double[] seq = Stream.generate(arg0)
		double[] seq = new Random(Double.doubleToLongBits(Math.E))
				.doubles().limit(10).toArray();	
		Assert.assertEquals(4,
			new QuickSelect((a, i, j, t) -> i).select(seq, 0, seq.length, 4)
		);
		Assert.assertEquals(4,
			Arrays.stream(seq).filter(v -> v < seq[4]).count()
		);
	}	
	
	@Test
	public void shouldBeAbleToSelectFromRandomSeqWithTailAsPivot() {
		double[] seq = new Random(Double.doubleToLongBits(Math.PI))
				.doubles().limit(10).toArray();	
		Assert.assertEquals(4,
			new QuickSelect((a, i, j, t) -> j - 1).select(seq, 0, seq.length, 4)
		);
		Assert.assertEquals(4,
			Arrays.stream(seq).filter(v -> v < seq[4]).count()
		);
	}
	
	@Test
	public void shouldBeAbleToSelectFromRandomSeqWithMiddleAsPivot() {
		double[] seq = new Random(Double.doubleToLongBits(-Math.PI))
				.doubles().limit(10).toArray();	
		Assert.assertEquals(4,
			new QuickSelect((a, i, j, t) -> (i + j) / 2).select(seq, 0, seq.length, 4)
		);
		Assert.assertEquals(4,
			Arrays.stream(seq).filter(v -> v < seq[4]).count()
		);
	}
	
	@Test
	public void shouldBeAbleToDetectAllEqualNumbers() {
		
		double[] seq = Stream.generate(() -> 33.33)				
				.mapToDouble(Double::doubleValue)
				.limit(100L).toArray();
		AtomicInteger count = new AtomicInteger(0);
		Select selector = new QuickSelect((a, i, j, t) -> i) {

			@Override
			public int select(double[] items, int begin, int end, int target) {
				count.incrementAndGet();
				return super.select(items, begin, end, target);
			}
			
		};
		
		Assert.assertEquals(33, selector.select(seq, 0, seq.length, 33));
		Assert.assertEquals(33.33, seq[33], 1e-12);
		Assert.assertEquals(1, count.get());
	}

}
