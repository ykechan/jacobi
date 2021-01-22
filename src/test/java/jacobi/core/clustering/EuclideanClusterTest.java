package jacobi.core.clustering;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.core.impl.ColumnVector;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.JacobiJUnit4ClassRunner;

@JacobiImport("/jacobi/test/data/EuclideanClusterTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class EuclideanClusterTest {
	
	@JacobiInject(0)
	public Matrix input;
	
	@JacobiInject(1)
	public Matrix query;
	
	@JacobiResult(10)
	public Matrix mean;
	
	@JacobiResult(11)
	public Matrix dists;
	
	@Test
	@JacobiImport("test rand 30x5")
	@JacobiEquals(expected = 10, actual = 10)
	@JacobiEquals(expected = 11, actual = 11)
	public void shouldBeAbleToComputeCentroidsOnRand30x5() {
		ClusterMetric<double[]> metric = EuclideanCluster.getInstance();
		
		this.mean = Matrices.wrap(metric.expects(this.input));
		double[] ans = new double[this.query.getRowCount()];
		for(int i = 0; i < ans.length; i++){
			ans[i] = metric.distanceBetween(this.mean.getRow(0), this.query.getRow(i));
		}
		
		this.dists = new ColumnVector(ans);
	}
	
	@Test
	public void shouldBeAbleToUserOtherMeanImpl() {
		ClusterMetric<double[]> metric = new EuclideanCluster(m -> new double[]{ Math.PI, 1.0, Math.E });
		Assert.assertArrayEquals(new double[]{
			Math.PI, 1.0, Math.E
		}, metric.expects(Matrices.zeros(0)), 1e-12);
	}

}
