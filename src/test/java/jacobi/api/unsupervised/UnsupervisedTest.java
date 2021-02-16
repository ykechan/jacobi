package jacobi.api.unsupervised;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.api.ext.Learn;
import jacobi.api.ext.Stats;
import jacobi.core.clustering.EuclideanCluster;
import jacobi.core.clustering.StandardScoreCluster;
import jacobi.core.impl.ColumnVector;
import jacobi.core.op.Dot;
import jacobi.core.op.MulT;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.util.Jacobi;
import jacobi.test.util.JacobiJUnit4ClassRunner;

@RunWith(JacobiJUnit4ClassRunner.class)
@JacobiImport("/jacobi/test/data/UnsupervisedTest.xlsx")
public class UnsupervisedTest {
	
	@JacobiInject(0)
	public Matrix data;
	
	@JacobiInject(1)
	public Matrix oracle;
	
	@Test
	@JacobiImport("iris")
	public void shouldBeAbleToClusterIrisDataByKMeans() {
		List<int[]> clusters = this.data.ext(Learn.class)
			.unsupervised().kMeans(this.oracle.getRowCount());
		
		List<double[]> centroids = EuclideanCluster.getInstance()
			.expects(this.data, clusters);
		
		Collections.sort(centroids, Comparator.comparingDouble(a -> a[0]));
		Jacobi.assertEquals(this.oracle, Matrices.wrap(centroids.toArray(new double[0][])), 0.5);
	}
	
	@Test
	@JacobiImport("wine")
	public void shouldBeAbleToClusterWineDataByKMeans() {
		List<int[]> clusters = this.data.ext(Learn.class)
				.unsupervised().kMeans(this.oracle.getRowCount());
		
		List<double[]> centroids = EuclideanCluster.getInstance()
				.expects(this.data, clusters);
			
		Collections.sort(centroids, Comparator.comparingDouble(a -> a[0]));
		Assert.assertEquals(this.oracle.getRowCount(), centroids.size());
		
		for(int i = 0; i < this.oracle.getRowCount(); i++){
			double[] actual = centroids.get(i);
			double[] expected = this.oracle.getRow(i);
			
			double norm = Dot.prod(actual, actual);
			
			
		}
	}
	
	@Test
	@JacobiImport("balance-scale")
	public void shouldBeAbleToClusterBalanceScaleByFullGMM() {
		// TODO: not yet implemented
	}

}
