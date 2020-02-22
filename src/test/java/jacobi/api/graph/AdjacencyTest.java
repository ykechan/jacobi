package jacobi.api.graph;

import org.junit.Assert;
import org.junit.Test;

import jacobi.api.Matrices;
import jacobi.api.ext.Graph;

public class AdjacencyTest {
	
	@Test
	public void shouldBeAbleToCreateProxyOnMatrix() {
		AdjList adjList = Matrices.identity(5).ext(Graph.class).init().get();
		Assert.assertEquals(5, adjList.order());
		for(int i = 0; i < 5; i++) {
			Assert.assertArrayEquals(
				new Edge[] { new Edge(i, i, 1.0) }, 
				adjList.edges(i).toArray(n -> new Edge[n])
			);
		}
	}
	
	

}
