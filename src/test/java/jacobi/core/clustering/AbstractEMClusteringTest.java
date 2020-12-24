package jacobi.core.clustering;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;

import jacobi.api.Matrices;
import jacobi.api.Matrix;

public class AbstractEMClusteringTest {
	
	@Test
	public void shouldBeAbleToPerformMaximizationStep() {
		List<String> clusters = Arrays.asList(
			"Cluster A",
			"Cluster B",
			"Cluster C",
			"Cluster D"
		);
			
		AbstractEMClustering<String> em = new AbstractEMClustering<String>(m -> Collections.emptyList(), 0L){

			@Override
			protected String expects(Matrix matrix) {
				return null;
			}

			@Override
			protected double distanceBetween(String cluster, double[] vector) {
				if("Cluster A".equals(cluster)){
					return vector[0];
				}
				
				if("Cluster B".equals(cluster)){
					return vector[1];
				}
				
				if("Cluster C".equals(cluster)){
					return vector[2];
				}
				return 1000.0;
			}
			
		};
		
		int[] membership = em.maximization(Matrices.wrap(new double[][]{
			new double[]{1.0, 2.0, 3.0},
			new double[]{2.0, 3.0, 1.0},
			new double[]{3.0, 1.0, 2.0},
			
			new double[]{3.0, 2.0, 1.0},
			new double[]{2.0, 1.0, 3.0},
			new double[]{1.0, 3.0, 2.0},
			
			new double[]{1001, 1001, 1001},
		}), clusters);
		
		Assert.assertArrayEquals(new int[]{0, 2, 1, 2, 1, 0, 3}, membership);
	}
	
	@Test
	public void shouldBeAbleToPerformMaximizationStepInRandomData() {
		List<String> clusters = Arrays.asList(
			"Cluster A",
			"Cluster B",
			"Cluster C",
			"Cluster D"
		);
			
		AbstractEMClustering<String> em = new AbstractEMClustering<String>(m -> Collections.emptyList(), 0L){

			@Override
			protected String expects(Matrix matrix) {
				return null;
			}

			@Override
			protected double distanceBetween(String cluster, double[] vector) {
				if("Cluster A".equals(cluster)){
					return vector[0];
				}
				
				if("Cluster B".equals(cluster)){
					return vector[1];
				}
				
				if("Cluster C".equals(cluster)){
					return vector[2];
				}
				return 1000.0;
			}
			
		};
		
		Random rand = new Random(Double.doubleToLongBits(Math.E * Math.PI));
		Matrix matrix = Matrices.wrap(IntStream.range(0, 256)
			.mapToObj(i -> new double[]{
				rand.nextDouble(), rand.nextDouble(), rand.nextDouble()
			})
			.toArray(n -> new double[n][])
		);
		
		int[] membership = em.maximization(matrix, clusters);
		for(int i = 0; i < membership.length; i++){
			int m = membership[i];
			double[] v = matrix.getRow(i);
			
			Assert.assertFalse(v[(m + 1) % 3] < v[m]);
			Assert.assertFalse(v[(m + 2) % 3] < v[m]);
		}
	}
	
	@Test
	public void shouldBeAbleToSelectTheClosestCluster() {
		List<String> clusters = Arrays.asList(
			"Cluster A",
			"Cluster B",
			"Cluster C",
			"Cluster D"
		);
		
		AbstractEMClustering<String> em = new AbstractEMClustering<String>(m -> Collections.emptyList(), 0L){

			@Override
			protected String expects(Matrix matrix) {
				return null;
			}

			@Override
			protected double distanceBetween(String cluster, double[] vector) {
				if("Cluster A".equals(cluster)){
					return vector[0];
				}
				
				if("Cluster B".equals(cluster)){
					return vector[1];
				}
				
				if("Cluster C".equals(cluster)){
					return vector[2];
				}
				return 1000.0;
			}
			
		};
		
		Assert.assertEquals(0, em.select(clusters, new double[]{ 1.0, Math.PI, Math.E }));
		Assert.assertEquals(1, em.select(clusters, new double[]{ Math.PI, 1.0, Math.E }));
		Assert.assertEquals(2, em.select(clusters, new double[]{ Math.PI, Math.E, 1.0 }));
	}
	
	@Test
	public void shouldBeAbleToGroupByMembership() {
		int[] membership = {2, 2, 0, 1, 1, 1, 0};
		int[] seq = new int[membership.length];
		
		int[] ends = this.mock().groupBy(membership, seq);
		Assert.assertArrayEquals(new int[]{2, 5, 7}, ends);
		Assert.assertArrayEquals(new int[]{
			2, 6, 
			3, 4, 5, 
			0, 1 
		}, seq);
	}
	
	@Test
	public void shouldBeAbleToGroupRandomMemberships() {
		int[] membership = new Random(Double.doubleToLongBits(Math.PI))
			.ints().limit(256).map(i -> Math.abs(i) % 4).toArray();
		int[] seq = new int[membership.length];
		
		int[] ends = this.mock().groupBy(membership, seq);
		Assert.assertEquals(4, ends.length);
		
		boolean[] flags = new boolean[seq.length];
		Arrays.fill(flags, false);
		
		for(int i = 0; i < ends.length; i++){
			int begin = i == 0 ? 0 : ends[i - 1];
			int end = ends[i];
			
			for(int j = begin; j < end; j++){
				if(j > begin){
					Assert.assertTrue(seq[j] > seq[j - 1]);
				}
				
				Assert.assertTrue(seq[j] >= 0 && seq[j] < seq.length);
				
				int s = seq[j];
				Assert.assertEquals(i, membership[s]);
				Assert.assertFalse(flags[s]);
				flags[s] = true;
			}
		}
	}
	
	@Test
	public void shouldBeAbleToGroupEmptyClusters() {
		int[] membership = {3, 1, 3, 1, 3, 1, 3, 1};
		int[] seq = new int[membership.length];
		
		int[] ends = this.mock().groupBy(membership, seq);
		Assert.assertArrayEquals(new int[]{0, 4, 4, 8}, ends);
		Assert.assertArrayEquals(new int[]{
			1, 3, 5, 7,
			0, 2, 4, 6
		}, seq);
	}
	
	protected AbstractEMClustering<Void> mock() {
		return new AbstractEMClustering<Void>(m -> Collections.emptyList(), 0L){

			@Override
			protected Void expects(Matrix matrix) {
				return null;
			}

			@Override
			protected double distanceBetween(Void cluster, double[] vector) {
				return 0;
			}
			
		};
	}

}
