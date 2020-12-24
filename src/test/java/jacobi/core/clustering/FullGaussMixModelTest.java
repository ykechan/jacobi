package jacobi.core.clustering;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.api.ext.Prop;
import jacobi.core.impl.ColumnVector;
import jacobi.core.impl.ImmutableMatrix;
import jacobi.core.util.Pair;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.Jacobi;
import jacobi.test.util.JacobiJUnit4ClassRunner;

@JacobiImport("/jacobi/test/data/FullGaussMixModelTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class FullGaussMixModelTest {
	
	@JacobiInject(0)
	public Matrix input;
	
	@JacobiInject(1)
	public Matrix mean;
	
	@JacobiInject(2)
	public Matrix covar;
	
	@JacobiResult(10)
	public Matrix result;
	
	@JacobiInject(-1)
	public Map<Integer, Matrix> all;
	
	@Test
	@JacobiImport("test std normal")
	@JacobiEquals(expected = 10, actual = 10)
	public void shouldBeAbleToComputeDistanceInStdNormalDist() {
		Pair pair = this.mock().expects(this.input);
		
		double[] dists = new double[this.input.getRowCount()];
		for(int i = 0; i < dists.length; i++){
			double[] v = this.input.getRow(i);
			dists[i] = this.mock().distanceBetween(pair, v);
		}
		this.result = new ColumnVector(dists);
	}
	
	@Test
	@JacobiImport("test mixed normal")
	@JacobiEquals(expected = 10, actual = 10)
	public void shouldBeAbleToComputeDistanceInMixedNormalDist() {
		
		AtomicInteger step = new AtomicInteger(0);
		Clustering clustering = new FullGaussMixModel(
			m -> Arrays.asList(this.mean.toArray()),
			Integer.MAX_VALUE
		){

			@Override
			protected List<Pair> expectation(Matrix matrix, int[] membership) {
				List<Pair> exp = super.expectation(matrix, membership);
				int n = step.incrementAndGet();
				
				Matrix means = Matrices.wrap(exp.stream()
					.map(p -> p.getLeft().getRow(0))
					.toArray(k -> new double[k][]));

				Jacobi.assertEquals(all.get(100 * n), means);
				for(int i = 0; i < exp.size(); i++){
					Matrix chol = exp.get(i).getRight();
					Matrix covar = all.get(100 * n + i + 1);
					
					double det = covar.ext(Prop.class).det();
					double actual = 1.0;
					for(int j = 0; j < chol.getRowCount(); j++){
						actual *= chol.get(j, j);
					}
					actual *= actual;
					
					Assert.assertEquals(det, actual, 1e-8);
				}
				return exp;
			}
			
		};
		
		List<int[]> clusters = clustering.compute(this.input);
		this.result = this.toMatrix(clusters);
	}
	
	@Test
	public void shouldBeAbleToReflectLowerSquareMatrix() {
		Matrix lower = Matrices.wrap(
			new double[]{1.0, 0.0, 0.0},
			new double[]{2.0, 1.0, 0.0},
			new double[]{3.0, 2.0, 1.0}
		);
		
		FullGaussMixModel gmm = new FullGaussMixModel(m -> Collections.emptyList(), 0L);
		gmm.reflect(lower);
		
		Jacobi.assertEquals(Matrices.wrap(
			new double[]{1.0, 2.0, 3.0},
			new double[]{2.0, 1.0, 2.0},
			new double[]{3.0, 2.0, 1.0}
		), lower);
	}
	
	protected FullGaussMixModel mock() {
		return new FullGaussMixModel(
			m -> Collections.emptyList(),
			m -> this.mean.getRow(0),
			m -> this.covar,
			Integer.MAX_VALUE
		);
	}
	
	protected Matrix toMatrix(List<int[]> clusters) {
		int numCols = clusters.stream().mapToInt(a -> a.length).max().orElse(0);
		return new ImmutableMatrix(){

			@Override
			public int getRowCount() {
				return clusters.size();
			}

			@Override
			public int getColCount() {
				return numCols;
			}

			@Override
			public double[] getRow(int index) {
				double[] row = new double[numCols];
				int[] cluster = clusters.get(index);
				for(int i = 0; i < cluster.length; i++){
					row[i] = cluster[i];
				}
				return row;
			}
			
		};
	}
}
