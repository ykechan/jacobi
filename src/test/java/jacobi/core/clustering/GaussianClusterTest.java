package jacobi.core.clustering;

import java.util.stream.IntStream;

import org.junit.Test;
import org.junit.runner.RunWith;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.core.impl.ColumnVector;
import jacobi.core.util.Pair;
import jacobi.core.util.Weighted;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.Jacobi;
import jacobi.test.util.JacobiJUnit4ClassRunner;

@JacobiImport("/jacobi/test/data/GaussianClusterTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class GaussianClusterTest {
	
	@JacobiInject(0)
	public Matrix input;
	
	@JacobiInject(1)
	public Matrix mean;
	
	@JacobiInject(2)
	public Matrix covar;
	
	@JacobiInject(7)
	public Matrix query;
	
	@JacobiResult(10)
	public Matrix dists;
	
	@JacobiResult(20)
	public Matrix lnDet;
	
	@JacobiResult(21)
	public Matrix chol;
	
	@Test
	@JacobiImport("test std normal")
	@JacobiEquals(expected = 20, actual = 20)
	public void shouldBeAbleToComputeClusterInStdNormalDist() {
		Weighted<Pair> clusterDesc = this.mock().expects(this.input);
		
		this.lnDet = Matrices.scalar(clusterDesc.weight);
		this.chol = clusterDesc.item.getRight();
		
		Jacobi.assertEquals(this.mean, clusterDesc.item.getLeft());
	}
	
	@Test
	@JacobiImport("test sheared circle")
	@JacobiEquals(expected = 10, actual = 10)
	@JacobiEquals(expected = 20, actual = 20)
	@JacobiEquals(expected = 21, actual = 21)
	public void shouldBeAbleToComputeDistsInShearedCircle() {
		Weighted<Pair> clusterDesc = this.mock().expects(this.input);
		
		this.lnDet = Matrices.scalar(clusterDesc.weight);
		this.chol = clusterDesc.item.getRight();
		
		Jacobi.assertEquals(this.mean, clusterDesc.item.getLeft());
		
		this.dists = new ColumnVector(IntStream.range(0, this.query.getRowCount())
			.mapToDouble(i -> this.mock().distanceBetween(clusterDesc, this.query.getRow(i)))
			.toArray()
		);
	}
	
	@Test
	@JacobiImport("test discrete with noise")
	@JacobiEquals(expected = 10, actual = 10)
	@JacobiEquals(expected = 20, actual = 20)
	@JacobiEquals(expected = 21, actual = 21)
	public void shouldBeAbleToComputeDistsInDiscreteWithNoise() {
		Weighted<Pair> clusterDesc = this.mock().expects(this.input);
		
		this.lnDet = Matrices.scalar(clusterDesc.weight);
		this.chol = clusterDesc.item.getRight();
		
		Jacobi.assertEquals(this.mean, clusterDesc.item.getLeft());
		
		this.dists = new ColumnVector(IntStream.range(0, this.query.getRowCount())
			.mapToDouble(i -> this.mock().distanceBetween(clusterDesc, this.query.getRow(i)))
			.toArray()
		);
	}
	
	@Test
	@JacobiImport("test bimodal normal")
	@JacobiEquals(expected = 10, actual = 10)
	@JacobiEquals(expected = 20, actual = 20)
	@JacobiEquals(expected = 21, actual = 21)
	public void shouldBeAbleToComputeDistsInBimodalNormal() {
		Weighted<Pair> clusterDesc = this.mock().expects(this.input);
		
		this.lnDet = Matrices.scalar(clusterDesc.weight);
		this.chol = clusterDesc.item.getRight();
		
		Jacobi.assertEquals(this.mean, clusterDesc.item.getLeft());
		
		this.dists = new ColumnVector(IntStream.range(0, this.query.getRowCount())
			.mapToDouble(i -> this.mock().distanceBetween(clusterDesc, this.query.getRow(i)))
			.toArray()
		);
		
	}
	
	protected GaussianCluster mock() {
		return new GaussianCluster(m -> this.mean.getRow(0), m -> this.covar);
	}

}
