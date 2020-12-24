package jacobi.core.clustering;

import java.util.AbstractList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.core.impl.ImmutableMatrix;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.Jacobi;
import jacobi.test.util.JacobiJUnit4ClassRunner;

@JacobiImport("/jacobi/test/data/SimpleKMeansTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class SimpleKMeansTest {
	
	@JacobiInject(0)
	public Matrix input;
	
	@JacobiInject(1)
	public Matrix seed;
	
	@JacobiInject(11)
	public Matrix step1;
	
	@JacobiInject(12)
	public Matrix step2;
	
	@JacobiInject(13)
	public Matrix step3;
	
	@JacobiResult(20)
	public Matrix result;
	
	@JacobiResult(30)
	public Matrix dist;
	
	@Test
	@JacobiImport("test 4 Centroids RR")
	@JacobiEquals(expected = 30, actual = 30)
	public void shouldBeAbleToCluster4CentroidsRR() {
		List<int[]> clusters = this.mock().compute(this.input);
		for(int c = 0; c < clusters.size(); c++){
			int[] cluster = clusters.get(c);
			Assert.assertEquals(c, cluster[0]);
			
			for(int i = 1; i < cluster.length; i++){
				Assert.assertEquals(4 * i + c, cluster[i]);
			}
		}
		
		double wss = this.mock().applyAsDouble(this.input, clusters);
		this.dist = Matrices.scalar(wss);
	}
	
	@Test
	@JacobiImport("test rand 2-D 3 centroids")
	@JacobiEquals(expected = 20, actual = 20)
	@JacobiEquals(expected = 30, actual = 30)
	public void shouldBeAbleToClusterRand2DWith3Centroids() {
		List<int[]> clusters = this.mock(step1, step2).compute(this.input);
		this.result = this.toMatrix(clusters);
		double wss = this.mock().applyAsDouble(this.input, clusters);
		this.dist = Matrices.scalar(wss);
	}
	
	@Test
	@JacobiImport("test gauss 5D 4 centroids")
	@JacobiEquals(expected = 20, actual = 20)
	@JacobiEquals(expected = 30, actual = 30)
	public void shouldBeAbleToClusterGauss5DWith4Centroids() {
		List<int[]> clusters = this.mock(step1, step2).compute(this.input);
		this.result = this.toMatrix(clusters);
		double wss = this.mock().applyAsDouble(this.input, clusters);
		this.dist = Matrices.scalar(wss);
	}
	
	@Test
	@JacobiImport("test mouse tempered")
	@JacobiEquals(expected = 20, actual = 20)
	@JacobiEquals(expected = 30, actual = 30)
	public void shouldBeAbleToClusterMouseTemperedData() {
		List<int[]> clusters = this.mock(step1, step2, step3).compute(this.input);
		double[] hash = new double[clusters.size()];
		for(int c = 0; c < clusters.size(); c++){
			int[] seq = clusters.get(c);
			int h = seq[0];
			for(int i = 1; i < seq.length; i++){
				h = (h * seq.length + seq[i]) % 65536;
			}
			
			hash[c] = h;
		}
		
		this.result = Matrices.wrap(new double[][]{ hash });
		
		double wss = this.mock().applyAsDouble(this.input, clusters);
		this.dist = Matrices.scalar(wss);
	}
	
	protected SimpleKMeans mock(Matrix... steps) {
		return new SimpleKMeans(m -> this.toList(seed), 4096L){

			@Override
			protected int[] maximization(Matrix matrix, List<double[]> clusters) {
				int n = iter++;
				if(n > 0 && steps != null && steps.length > 0){
					Jacobi.assertEquals(steps[n - 1], Matrices.wrap(clusters.toArray(new double[0][])));
				}
				return super.maximization(matrix, clusters);
			}

			private int iter = 0;
		};
	}
	
	protected List<double[]> toList(Matrix matrix) {
		return new AbstractList<double[]>(){

			@Override
			public double[] get(int index) {
				return matrix.getRow(index);
			}

			@Override
			public int size() {
				return matrix.getRowCount();
			}
			
		};
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
