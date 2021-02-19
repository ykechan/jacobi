package jacobi.api.unsupervised;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.api.ext.Learn;
import jacobi.core.clustering.EuclideanCluster;
import jacobi.core.op.Dot;
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
			
			double cos = Dot.prod(actual, expected) 
					/ Math.sqrt(Dot.prod(actual, actual)) 
					/ Math.sqrt(Dot.prod(expected, expected));
			
			Assert.assertTrue(cos > 0.9);
		}
	}
	
	@Test
	@JacobiImport("double spiral")
	public void shouldBeAbleToClusterDoubleSpiralByDbscan() throws IOException {
		List<int[]> clusters = this.data.ext(Learn.class).unsupervised().dbscan(5, 0.2);
		Assert.assertEquals(this.oracle.getColCount(), clusters.size());
		
		int offset = 0;
		for(int i = 0; i < this.oracle.getColCount(); i++){
			int len = (int) Math.floor(this.oracle.get(0, i));
			
			int[] seq = clusters.get(i);
			Arrays.sort(seq);
			
			Assert.assertArrayEquals(IntStream.range(offset, offset + len).toArray(), seq);
			
			offset += len;
		}
	}
	
	@Test
	@JacobiImport("radiation")
	public void shouldBeAbleToMeanShiftInRadiationDataUsingGaussWindow() {
		List<int[]> result = this.data.ext(Learn.class).unsupervised()
				.meanShift().gauss(0.4);
		
		int len = this.data.getRowCount() / 2;
		Assert.assertEquals(2, result.size());
		for(int[] seq : result){
			Arrays.sort(seq);
			
			int[] array = IntStream.range(seq[0], seq[0] + len).toArray(); 
			Assert.assertArrayEquals(array, seq);
		}
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldFailWhenKIsLessThan1() {
		Matrices.zeros(0).ext(Learn.class).unsupervised().kMeans(0);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldFailWhenKIsLessThanNumberOfInstances() {
		Matrices.identity(3).ext(Learn.class).unsupervised().kMeans(4);
	}
	
	@Test
	public void shouldReturnTrivialSequenceWhenKEquals1() {
		Assert.assertArrayEquals(new int[]{0, 1, 2}, 
			Matrices.identity(3).ext(Learn.class).unsupervised().kMeans(1).get(0));
	}

	@Test
	public void shouldBeAbleToApproxErfInMeanShiftsForSpecificValues() {
		MeanShifts ms = Matrices.zeros(0).ext(Learn.class).unsupervised().meanShift();
		Assert.assertEquals(0.045111106, ms.erf(1.0, 0.04), 1e-6);
		Assert.assertEquals(0.067621594, ms.erf(1.0, 0.06), 1e-6);
		Assert.assertEquals(0.090078126, ms.erf(1.0, 0.08), 1e-6);
		Assert.assertEquals(0.112462916, ms.erf(1.0, 0.1), 1e-6);
		
		Assert.assertEquals(3 * 0.045111106, ms.erf(3.0, 0.04), 1e-6);
		Assert.assertEquals(3 * 0.067621594, ms.erf(3.0, 0.06), 1e-6);
		Assert.assertEquals(3 * 0.090078126, ms.erf(3.0, 0.08), 1e-6);
		Assert.assertEquals(3 * 0.112462916, ms.erf(3.0, 0.1), 1e-6);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldFailWhenMinPointsIsNegativeAndNotSpecialValue() {
		MeanShifts ms = Matrices.zeros(0).ext(Learn.class).unsupervised().meanShift();
		ms.setMinPts(-2);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldFailWhenNumSigIsNegative() {
		MeanShifts ms = Matrices.zeros(0).ext(Learn.class).unsupervised().meanShift();
		ms.setNumSig(-1.0);
	}
	
	@Test
	public void shouldUseNumSigAndMinPtsAfterConfig() {
		MeanShifts ms = Matrices.zeros(1, 2).ext(Learn.class).unsupervised().meanShift();
		
		ms.setMinPts(0).setNumSig(1.0);
		Assert.assertEquals(1, ms.gauss(1.0).size());
		
		ms.setMinPts(2).setNumSig(1.0);
		Assert.assertEquals(0, ms.gauss(1.0).size());
	}
	
	@Test
	public void shouldConvergeToSingleClusterForASquareUsingFlatMeanShift() {
		MeanShifts ms = Matrices.wrap(
			new double[]{ 1.0,  1.0},
			new double[]{-1.0, -1.0},
			new double[]{-1.0,  1.0},
			new double[]{ 1.0, -1.0}
		).ext(Learn.class).unsupervised().meanShift();
		List<int[]> seq = ms.flat(2.0, 0.1);
		Assert.assertEquals(1, seq.size());
		Arrays.sort(seq.get(0));
		Assert.assertArrayEquals(new int[]{0, 1, 2, 3},  seq.get(0));
	}
	
	@Test
	public void shouldUseNumberOfVertexInASimplexAsDefaultMinValue() {
		MeanShifts ms = Matrices.zeros(0, 3).ext(Learn.class).unsupervised().meanShift();
		Assert.assertEquals(4, ms.minPoints(3, MeanShifts.DEFAULT_MIN_SIMPLEX));
		Assert.assertEquals(5, ms.minPoints(4, MeanShifts.DEFAULT_MIN_SIMPLEX));
		Assert.assertEquals(6, ms.minPoints(5, MeanShifts.DEFAULT_MIN_SIMPLEX));
	}
	
	@Test
	public void shouldUseSpecifiedMinPts() {
		MeanShifts ms = Matrices.zeros(0, 3).ext(Learn.class).unsupervised().meanShift();
		Assert.assertEquals(0, ms.minPoints(3, 0));
		Assert.assertEquals(1, ms.minPoints(4, 1));
		Assert.assertEquals(2, ms.minPoints(5, 2));
	}
}
