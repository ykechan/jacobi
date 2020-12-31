package jacobi.api.graph;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
	
	@Test
	public void shouldBeAbleToStoreEdgesInHashMap() {
		Map<Edge, Double> map = new HashMap<>();
		Arrays.stream(new Edge[]{
			new Edge(0, 1, 1.0),
			new Edge(1, 2, 3.0)
		}).forEach(e -> map.put(e, e.weight));
		
		Assert.assertEquals(3.0, map.get(new Edge(1, 2, 3.0)), 1e-12);
	}
	
	@Test
	public void shouldEdgeNotEqualsToEdgesWithDifferentWeight(){
		Assert.assertFalse(new Edge(0, 1, 2.0).equals(new Edge(0, 1, 3.0)));
	}
	
	@Test
	public void shouldEdgeNotEqualsToOtherObjects(){
		Assert.assertFalse(new Edge(0, 1, 2.0).equals("Some string"));
	}

}
