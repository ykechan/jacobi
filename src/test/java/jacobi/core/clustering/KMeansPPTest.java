package jacobi.core.clustering;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.DoubleSupplier;

import org.junit.Test;
import org.junit.runner.RunWith;

import jacobi.api.Matrix;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
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
	@JacobiImport("test 4 Centroids RR")
	@JacobiEquals(expected=10, actual=10)
	public void shouldBeAbleToPick4CentroidsRR() {
		KMeansPP kmeansPP = new KMeansPP(this.mockRand(), 4, 0, Integer.MAX_VALUE);
		Matrix init = kmeansPP.apply(this.input);
		
		this.output = init;
	}
	
	@Test
	@JacobiImport("test rand 2-D 3 centroids")
	@JacobiEquals(expected=10, actual=10)
	public void shouldBeAbleToPickRand2D3Centroids() {
		KMeansPP kmeansPP = new KMeansPP(this.mockRand(), 3, 12, Integer.MAX_VALUE);
		Matrix init = kmeansPP.apply(this.input);
		
		this.output = init;
	}
	
	protected DoubleSupplier mockRand() {
		AtomicInteger autoInc = new AtomicInteger(0);
		return () -> this.rand.get(0, autoInc.getAndIncrement() % this.rand.getColCount());
	}

}
