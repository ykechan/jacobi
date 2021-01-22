package jacobi.core.clustering;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.junit.Test;
import org.junit.runner.RunWith;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.Jacobi;
import jacobi.test.util.JacobiJUnit4ClassRunner;

@JacobiImport("/jacobi/test/data/KMeansPPTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class KMeansPPTest {
	
	@JacobiInject(0)
	public Matrix input;
	
	@JacobiInject(1)
	public Matrix rand;
	
	@JacobiResult(10)
	public Matrix output;
	
	@Test
	@JacobiImport("test rand 60x4")
	@JacobiEquals(expected = 10, actual = 10)
	public void shouldBeAbleToSelectExtremaInRand60x4() {
		Function<Matrix, List<double[]>> initFn = new KMeansPP(
			EuclideanCluster.getInstance(),
			n -> 0, 3, Integer.MAX_VALUE
		);
		
		this.output = Matrices.wrap(initFn.apply(this.input).toArray(new double[0][]));
	}
	
	@Test
	@JacobiImport("test rand 60x4")
	@JacobiEquals(expected = 10, actual = 10)
	public void shouldBeAbleToSelectExtremaInRand60x4InParallel() {
		Function<Matrix, List<double[]>> initFn = new KMeansPP(
			EuclideanCluster.getInstance(),
			n -> 0, 3, 0L
		);
		
		this.output = Matrices.wrap(initFn.apply(this.input).toArray(new double[0][]));
	}

	@Test
	@JacobiImport("test rand 30x2")
	@JacobiEquals(expected = 10, actual = 10)
	public void shouldBeAbleToSelectByRandInRand30x2() {
		AtomicInteger counter = new AtomicInteger(0);
		
		Function<Matrix, List<double[]>> initFn = new KMeansPP(
			EuclideanCluster.getInstance(),
			n -> (int) this.rand.get(0, counter.getAndIncrement()), 3, Integer.MAX_VALUE
		);
		
		this.output = Matrices.wrap(initFn.apply(this.input).toArray(new double[0][]));
	}
	
	@Test
	@JacobiImport("test rand 30x2")
	@JacobiEquals(expected = 10, actual = 10)
	public void shouldBeAbleToSelectByRandInRand30x2InParallel() {
		AtomicInteger counter = new AtomicInteger(0);
		
		Function<Matrix, List<double[]>> initFn = new KMeansPP(
			EuclideanCluster.getInstance(),
			n -> (int) this.rand.get(0, counter.getAndIncrement()), 3, 0L
		);
		
		this.output = Matrices.wrap(initFn.apply(this.input).toArray(new double[0][]));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldFailWhenNoInputData() {
		new KMeansPP(EuclideanCluster.getInstance(), n -> 0, 3, 0L).apply(null);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldFailWhenKIsLargerThanNumberOfData() {
		new KMeansPP(EuclideanCluster.getInstance(), n -> 0, 3, 0L).apply(Matrices.zeros(2));
	}
	
	@Test
	public void shouldReturnInputMatrixWhenKIsEqualsToNumberOfData() {
		List<double[]> centroids = new KMeansPP(EuclideanCluster.getInstance(), n -> 0, 3, 0L)
				.apply(Matrices.identity(3));
		
		Jacobi.assertEquals(Matrices.identity(3), Matrices.wrap(centroids.toArray(new double[0][])));
	}
}
