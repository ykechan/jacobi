package jacobi.core.clustering;

import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.test.util.Jacobi;

public class StandardScoreClusterTest {
	
	@Test
	public void shouldBeAbleToComputeZScoreGivenMeanAndVar() {
		ClusterMetric<Matrix> metric = new StandardScoreCluster(
			m -> new double[m.getColCount()],
			(m, u) -> IntStream.range(0, u.length).mapToDouble(i -> 1.0).toArray()
		);
		
		double dist = metric.distanceBetween(Matrices.wrap(
			new double[]{0.0, 0.0},
			new double[]{1.1 * 1.1, 2.3 * 2.3}
		), new double[]{2.2, -4.6});
		
		Assert.assertEquals(2 * 2 + 2 * 2, dist, 1e-12);
		
		double[] u = new double[]{Math.E, Math.PI, 2.0};
		double[] var = new double[]{ 0.81, 0.49, 0.64 };
		
		dist = metric.distanceBetween(Matrices.wrap(u, var),
			new double[]{ u[0], u[1], u[2] }
		);
		
		Assert.assertEquals(0.0, dist, 1e-12);
	}
	
	@Test
	public void shouldBeAbleToGetZeroDistanceAtTheMean() {
		double[] u = new double[]{Math.E, Math.PI, 2.0};
		double[] var = new double[]{ 0.81, 0.49, 0.64 };
		
		ClusterMetric<Matrix> metric = new StandardScoreCluster(m -> u, (m, mu) -> var);
		
		double dist = metric.distanceBetween(Matrices.wrap(u, var),
			new double[]{ u[0], u[1], u[2] }
		);
		
		Assert.assertEquals(0.0, dist, 1e-12);
	}

	@Test
	public void shouldBeAbleToGetDesignedDistanceByStdDev() {
		double[] u = new double[]{Math.E, Math.PI, 2.0};
		double[] var = new double[]{ 0.81, 0.49, 0.64 };
		
		ClusterMetric<Matrix> metric = new StandardScoreCluster(m -> u, (m, mu) -> var);
		
		double dist = metric.distanceBetween(Matrices.wrap(u, var),
			new double[]{ 
				u[0] + 1.1 * 0.9, 
				u[1] + 2.2 * 0.7, 
				u[2] + 3.3 * 0.8
			}
		);
		
		Assert.assertEquals(1.1 * 1.1 + 2.2 * 2.2 + 3.3 * 3.3, dist, 1e-12);
	}
	
	@Test
	public void shouldGetPositiveInfForEmptyCluster() {
		double[] u = new double[]{Math.E, Math.PI, 2.0};
		double[] var = new double[]{ 0.81, 0.49, 0.64 };
		
		ClusterMetric<Matrix> metric = new StandardScoreCluster(m -> u, (m, mu) -> var);
		
		double dist = metric.distanceBetween(Matrices.zeros(0),
			new double[]{ 
				u[0] + 1.1 * 0.9, 
				u[1] + 2.2 * 0.7, 
				u[2] + 3.3 * 0.8
			}
		);
		
		Assert.assertTrue(Double.isInfinite(dist));
		Assert.assertTrue(dist > 0.0);
	}
	
	@Test
	public void shouldGivesMeanAndVarianceAsClusterDescriptor() {
		double[] u = new double[]{Math.E, Math.PI, 2.0};
		double[] var = new double[]{ 0.81, 0.49, 0.64 };
		
		ClusterMetric<Matrix> metric = new StandardScoreCluster(m -> u, (m, mu) -> var);
		
		Jacobi.assertEquals(Matrices.wrap(u, var), metric.expects(Matrices.zeros(5, 3)));
	}
	
	@Test
	public void shouldGivesEmptyAsClusterDescriptorWhenClusterContainsNoMember() {
		double[] u = new double[]{Math.E, Math.PI, 2.0};
		double[] var = new double[]{ 0.81, 0.49, 0.64 };
		
		ClusterMetric<Matrix> metric = new StandardScoreCluster(m -> u, (m, mu) -> var);
		
		Jacobi.assertEquals(Matrices.zeros(0), metric.expects(Matrices.zeros(0)));
	}
}
