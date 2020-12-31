package jacobi.api.unsupervised;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import jacobi.api.Matrix;
import jacobi.api.ext.Stats;
import jacobi.core.clustering.AbstractEMClustering;
import jacobi.core.clustering.SimpleKMeans;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
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
		List<int[]> clusters = this.data.ext(Unsupervised.class).kMeans(3, 3);
		List<double[]> centroids = this.data.ext(Unsupervised.class).segregate(clusters)
			.stream().map(m -> m.ext(Stats.class).mean()).collect(Collectors.toList());
		Collections.sort(centroids, Comparator.comparingDouble(a -> a[0]));
		
		Assert.assertEquals(this.oracle.getRowCount(), centroids.size());
		
		double err = 0.0;
		for(int i = 0; i < this.oracle.getRowCount(); i++){
			double[] expects = this.oracle.getRow(i);
			double[] actual = centroids.get(i);
			
			for(int j = 0; j < actual.length; j++){
				double dx = Math.abs(actual[j] - expects[j]);
				err += dx;
			}
		}
		err /= this.oracle.getRowCount();
		Assert.assertTrue(err < 1.0);
	}
	
	@Test
	@JacobiImport("wine")
	public void shouldBeAbleToClusterWineDataByGMM() {
		List<int[]> clusters = this.data.ext(Unsupervised.class).gmm(3);
		List<double[]> centroids = this.data.ext(Unsupervised.class).segregate(clusters)
				.stream().map(m -> m.ext(Stats.class).mean()).collect(Collectors.toList());
		Collections.sort(centroids, Comparator.comparingDouble(a -> a[0]));
		
		Assert.assertEquals(this.oracle.getRowCount(), centroids.size());
		
		double[] sd = this.data.ext(Stats.class).stdDev();
		
		double err = 0.0;
		for(int i = 0; i < this.oracle.getRowCount(); i++){
			double[] expects = this.oracle.getRow(i);
			double[] actual = centroids.get(i);
			
			for(int j = 0; j < actual.length; j++){
				double dx = Math.abs(actual[j] - expects[j]);
				err += dx / sd[j];
			}
		}
		err /= this.oracle.getRowCount() * this.oracle.getColCount();
		Assert.assertTrue(err < 0.3);
	}
	
	@Test
	@JacobiImport("balance-scale")
	public void shouldBeAbleToClusterBalanceScaleByFullGMM() {
		List<int[]> clusters = this.data.ext(Unsupervised.class).gmm(3);
		List<double[]> centroids = this.data.ext(Unsupervised.class).segregate(clusters)
				.stream().map(m -> m.ext(Stats.class).mean()).collect(Collectors.toList());;
		Collections.sort(centroids, Comparator.comparingDouble(a -> a[0]));
		
		int hash = 0;
		
		for(double[] centroid : centroids){
			double forceL = centroid[0] * centroid[1];
			double forceR = centroid[2] * centroid[3];
			
			if(forceR - forceL > 2.5){
				// tip to the right
				hash += 4;
			}else if(forceR - forceL < -2.5){
				// tip to the left
				hash += 2;
			}else if(Math.abs(forceR - forceL) < 2){
				// balanced
				hash += 1;
			}
		}
		
		Assert.assertEquals(7, hash); // should be different cluster for each case
	}

}
