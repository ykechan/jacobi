package jacobi.core.graph;

import java.util.Arrays;

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
	public void shouldBeAbleToEncodeNonZeroList() {
		Assert.assertArrayEquals(new int[] {0, 1, 2, 3, 4, 6, 7, 8}, AdjMatrix.scatters(new double[] {
			1.0, 2.0, 3.0, 4.0, 5.0, 0.0, 6.0, 7.0, 8.0
		}, new int[100]));
		
		Assert.assertArrayEquals(new int[] {0, 1, 2, 3, 7, 8}, AdjMatrix.scatters(new double[] {
			1.0, 2.0, 3.0, 4.0, 0.0, 0.0, 0.0, 7.0, 8.0
		}, new int[100]));
		
		Assert.assertArrayEquals(new int[] {0, 2, 4}, AdjMatrix.scatters(new double[] {
			1.0, 0.0, 2.0, 0.0, 3.0
		}, new int[100]));
	}
	

}
