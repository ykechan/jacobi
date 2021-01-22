package jacobi.core.clustering;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.core.clustering.ExpectationMaximization.Array;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.Jacobi;
import jacobi.test.util.JacobiJUnit4ClassRunner;

@JacobiImport("/jacobi/test/data/ExpectationMaximizationTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class ExpectationMaximizationTest {
	
	@JacobiInject(0)
	public Matrix input;
	
	@JacobiInject(1)
	public Matrix init;
	
	@JacobiResult(10)
	public Matrix result;
	
	@JacobiResult(20)
	public Matrix clusters;
	
	@JacobiInject(-1)
	public Map<Integer, Matrix> all;
	
	@Test
	@JacobiImport("test Euclid 4 Centroids RR")
	public void shouldBeAbleToMaximizeInEuclid4CentroidsRR() {
		ExpectationMaximization<double[]> em = new ExpectationMaximization<>(
			m -> Arrays.asList(this.init.toArray()),
			EuclideanCluster.getInstance(),
			Integer.MAX_VALUE
		);
		
		Array memberships = em.maximization(this.input, Arrays.asList(this.init.toArray()));
		for(int i = 0; i < memberships.elements.length; i++){
			Assert.assertEquals(i % 4, memberships.elements[i]);
		}
		
		Assert.assertArrayEquals(new int[]{
			(this.input.getRowCount() / 4) + 1,
			(this.input.getRowCount() / 4) + 1,
			this.input.getRowCount() / 4,
			this.input.getRowCount() / 4
		}, memberships.cuts);
	}
	
	@Test
	@JacobiImport("test Euclid 4 Centroids RR")
	@JacobiEquals(expected = 10, actual = 10)
	public void shouldBeAbleToFindExpectedValueInEuclid4CentroidsRR() {
		ExpectationMaximization<double[]> em = new ExpectationMaximization<>(
			m -> Arrays.asList(this.init.toArray()),
			EuclideanCluster.getInstance(),
			Integer.MAX_VALUE
		);
		
		int[] memberships = IntStream.range(0, this.input.getRowCount()).map(i -> i % 4).toArray();
		int[] counts = new int[]{
			(this.input.getRowCount() / 4) + 1,
			(this.input.getRowCount() / 4) + 1,
			this.input.getRowCount() / 4,
			this.input.getRowCount() / 4
		};
		
		List<double[]> centroids = em.expectation(input, new Array(memberships, counts));
		this.result = Matrices.wrap(centroids.toArray(new double[0][]));
	}
	
	@Test
	@JacobiImport("test Euclid 4 Centroids RR")
	public void shouldBeAbleToIterativeInEuclid4CentroidsRR() {
		ExpectationMaximization<double[]> em = new ExpectationMaximization<>(
				m -> Arrays.asList(this.init.toArray()),
				EuclideanCluster.getInstance(),
				Integer.MAX_VALUE
			);
		List<int[]> seq = em.compute(this.input);
		Assert.assertEquals(4, seq.size());
		for(int i = 0; i < 4; i++){
			int[] array = seq.get(i);
			for(int j = 0; j < array.length; j++){
				Assert.assertEquals(4 * j + i, array[j]);
			}
		}
	}
	
	@Test
	@JacobiImport("test k-means on 3-D grid")
	public void shouldBeAbleToPerformKMeansClusteringOn3DGrid() {
		ExpectationMaximization<double[]> em = this.kmeansMock(true);
		List<int[]> seq = em.compute(this.input);
		Assert.assertEquals(3, seq.size());
	}
	
	@Test
	@JacobiImport("test k-means on 2-D 3 centroids")
	@JacobiEquals(expected = 20, actual = 20)
	public void shouldBeAbleToPerformKMeansOn2D3Centroids() {
		ExpectationMaximization<double[]> em = this.kmeansMock(true);
		List<int[]> seqs = em.compute(this.input);
		
		Assert.assertEquals(3, seqs.size());
		this.clusters = this.toMatrix(seqs);
	}
	
	@Test
	public void shouldBeAbleToSortMembershipsIntoSeqs() {
		ExpectationMaximization<?> em = this.mock();
		Array result = em.sort(new int[]{2, 2, 2, 1, 1, 0, 0, 0, 0}, new int[]{4, 2, 3});
		Assert.assertArrayEquals(new int[]{5, 6, 7, 8, 3, 4, 0, 1, 2}, result.elements);
		Assert.assertArrayEquals(new int[]{4, 6, 9}, result.cuts);
		
		Array result1 = em.sort(new int[]{2, 2, 0, 2, 0, 2, 2}, new int[]{2, 0, 5});
		Assert.assertArrayEquals(new int[]{2, 4, 0, 1, 3, 5, 6}, result1.elements);
		Assert.assertArrayEquals(new int[]{2, 2, 7}, result1.cuts);
	}
	
	protected ExpectationMaximization<?> mock() {
		return new ExpectationMaximization<>(
			m -> Collections.emptyList(),
			EuclideanCluster.getInstance(), 
			Integer.MAX_VALUE
		);
	}
	
	protected ExpectationMaximization<double[]> kmeansMock(boolean serial) {
		return new ExpectationMaximization<double[]>(
				m -> Arrays.asList(this.init.toArray()),
				EuclideanCluster.getInstance(),
				serial ? Integer.MAX_VALUE : 0){
			
			@Override
			public List<int[]> compute(Matrix matrix) {
				List<int[]> result = super.compute(matrix);
				int max = all.keySet().stream().mapToInt(i -> i)
						.filter(i -> i > 10 && i < 20)
						.map(i -> i - 10)
						.max().orElse(0);
				Assert.assertEquals(max + 1, this.step);
				return result;
			}

			@Override
			protected List<double[]> expectation(Matrix matrix, Array memberships) {
				List<double[]> centroids = super.expectation(matrix, memberships);
				Matrix ans = all.get(10 + this.step++);
				
				if(ans != null){
					Jacobi.assertEquals(ans, Matrices.wrap(centroids.toArray(new double[0][])));
				}
				
				return centroids;
			}
		
			private int step = 1;
		};
	}
	
	protected Matrix toMatrix(List<int[]> seqs) {
		int len = seqs.stream().mapToInt(s -> s.length).max().orElse(0);
		return Matrices.wrap(seqs.stream()
			.map(s -> Arrays.stream(s).mapToDouble(i -> i).toArray())
			.map(a -> Arrays.copyOf(a, len))
			.toArray(n -> new double[n][])
		);
	}

}
