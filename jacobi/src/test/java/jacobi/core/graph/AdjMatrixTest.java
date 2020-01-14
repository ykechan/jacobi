package jacobi.core.graph;

import java.util.Arrays;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;

import jacobi.api.Matrices;
import jacobi.api.graph.AdjList;
import jacobi.api.graph.Edge;

public class AdjMatrixTest {
	
	@Test
	public void shouldIdentityMatrixBeConsistingOnlySelfLoop() {
		AdjList adjList = AdjMatrix.of(Matrices.identity(7));
		Assert.assertEquals(7, adjList.order());
		
		for(int i = 0; i < adjList.order(); i++) {
			Assert.assertArrayEquals(
				new Edge[] { new Edge(i, i, 1.0) }, 
				adjList.edges(i).toArray(n -> new Edge[n]));
		}
	}
	
	@Test
	public void shouldOnlyOneRowHavingNonZeroEntryBeRadiationNetwork() {
		AdjList adjList = AdjMatrix.of(Matrices.zeros(5).getAndSet(2, r -> {
			Arrays.fill(r, 1.0);
			r[2] = 0.0;
		}));
		
		Assert.assertEquals(5, adjList.order());
		for(int i = 0; i < adjList.order(); i++) {
			if(i == 2) {
				continue;
			}
			
			Assert.assertArrayEquals(
				new Edge[0], 
				adjList.edges(i).toArray(n -> new Edge[n])
			);
		}
		
		Assert.assertArrayEquals(
			new Edge[] {
				new Edge(2, 0, 1.0),
				new Edge(2, 1, 1.0),
				//new Edge(2, 2, 1.0),
				new Edge(2, 3, 1.0),
				new Edge(2, 4, 1.0)
				
			}, 
			adjList.edges(2).toArray(n -> new Edge[n])
		);
	}	
	
	@Test
	public void shouldBeAbleToEncodeSparseList() {
		
		Assert.assertArrayEquals(new int[] {0, 2, 4}, AdjMatrix.scatters(new double[] {
			1.0, 0.0, 2.0, 0.0, 3.0
		}, new int[100]));
		
		Assert.assertArrayEquals(new int[] {2, 4, 9}, AdjMatrix.scatters(new double[] {
			0.0, 0.0, 1.0, 0.0, 3.0, 0.0, 0.0, 0.0, 0.0, 9.0, 0.0
		}, new int[100]));
	}
	
	@Test
	public void shouldBeAbleToReturnNullIfAllAreNonZero() {
		double[] weights = new double[100];
		Arrays.fill(weights, Math.PI);
		
		Assert.assertNull(AdjMatrix.scatters(weights, new int[weights.length]));
	}
	
	@Test
	public void shouldBeAbleToReturnSparseIndexIfEntriesAreFew() {		
		int[] map = AdjMatrix.scatters(new double[]{
			0.0, 0.0, 0.0, 
			1.0, 0.0, 3.0, 
			0.0, 0.0, 0.0, 
			0.0}, new int[100]);
		Assert.assertArrayEquals(new int[] {3, 5}, map);
	}
	
	@Test
	public void shouldBeAbleToReturnSparseIndexIfEntriesAreNotContinuous() {	
		int[] map = AdjMatrix.scatters(IntStream.range(0, 30)
				.mapToDouble(i -> i % 2 == 0 ? Math.PI : 0)
				.toArray(),  
				new int[100]);
		
		Assert.assertArrayEquals(
			IntStream.range(0, 30).filter(i -> i % 2 == 0).toArray(), 
			map
		);
	}
	
	@Test
	public void shouldBeAbleToReturnSparseIndexIfContinuousRangeBreaksTooOften() {	
		int[] map = AdjMatrix.scatters(IntStream.range(0, 30)
				.mapToDouble(i -> i % 4 == 0 ? 0 : Math.PI)
				.toArray(),  
				new int[100]);
		
		Assert.assertArrayEquals(
			IntStream.range(0, 30).filter(i -> i % 4 > 0).toArray(), 
			map
		);
	}
	
	@Test
	public void shouldBeAbleToReturnRangePairsWhenOnly1ContinousRange() {
		double[] weights = new double[30];
		Arrays.fill(weights, 3, 23, Math.E);
		
		Assert.assertArrayEquals(new int[] {-1, 3, 23}, AdjMatrix.scatters(weights, new int[100]));
	}
	
	@Test
	public void shouldBeAbleToReturnRangePairsWhenAreMultipleRanges() {
		double[] weights = IntStream.range(0, 100)
				.mapToDouble(i -> i % 10 == 0 ? 0 : Math.PI)
				.toArray();
		
		//Assert.assertArrayEquals(new int[] {-1, 3, 23}, AdjMatrix.scatters(weights, new int[100]));
		int[] map = AdjMatrix.scatters(weights, new int[100]);
		Assert.assertEquals(-1, map[0]);
		for(int i = 1; i < map.length; i += 2) {
			int off = 10 * ((i - 1) / 2);
			Assert.assertEquals(off + 1, map[i]);
			Assert.assertEquals(off + 10, map[i + 1]);
		}
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldFailWhenInputMatrixIsNotSquare() {
		AdjMatrix.of(Matrices.zeros(3, 2));
	}
	

}
