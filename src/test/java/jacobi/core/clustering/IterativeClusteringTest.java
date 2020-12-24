package jacobi.core.clustering;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.ToDoubleBiFunction;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;

import jacobi.api.Matrices;
import jacobi.api.Matrix;

public class IterativeClusteringTest {
	
	@Test
	public void shouldBeAbleToSelectTheBestResult() {
		ToDoubleBiFunction<Matrix, List<int[]>> measureFn = (m, ls) -> ls.get(0).length;
		
		AtomicInteger autoInc = new AtomicInteger(0);
		Clustering clusteringFn = m -> {
			int n = autoInc.incrementAndGet();
			int[] seq = IntStream.range(0, m.getRowCount()).toArray();
			return Arrays.asList(
				Arrays.copyOfRange(seq, 0, n),
				Arrays.copyOfRange(seq, n, seq.length)
			);
		}; 
		
		Clustering impl = new IterativeClustering(measureFn, 12, clusteringFn);
		List<int[]> clusters = impl.compute(Matrices.zeros(256, 3));
		
		Assert.assertEquals(12, clusters.get(0).length);
		Assert.assertEquals(256 - 12, clusters.get(1).length);
	}

}
