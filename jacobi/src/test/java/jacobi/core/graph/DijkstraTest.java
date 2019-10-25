package jacobi.core.graph;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;

import jacobi.api.graph.AdjList;
import jacobi.api.graph.Edge;
import jacobi.api.graph.RouteMap;
import jacobi.core.util.Enque;

public class DijkstraTest {
	
	@Test
	public void shouldBeAbleToFindRouteMapOnSimpleGraph() {
		RouteMap routeMap = new Dijkstra(() -> Enque.stackOf(new ArrayDeque<>())).compute(
			this.adjList(Arrays.asList(
				new Edge(0, 1, 2.0),
				new Edge(0, 2, 8.0),
				new Edge(0, 3, 5.0),
				new Edge(1, 2, 1.0),
				new Edge(2, 4, 3.0),
				new Edge(3, 4, 4.0)
			)), 0 
		).get();
		
		Assert.assertArrayEquals(new Edge[] {
			new Edge(4, 2, 6.0),
		}, routeMap.edges(4).toArray(n -> new Edge[n]));
		
		Assert.assertArrayEquals(new Edge[] {
			new Edge(3, 0, 5.0),
		}, routeMap.edges(3).toArray(n -> new Edge[n]));
		
		Assert.assertArrayEquals(new Edge[] {
			new Edge(2, 1, 3.0),
		}, routeMap.edges(2).toArray(n -> new Edge[n]));
	}
	
	@Test
	public void shouldBeAbleToFindRouteMapOnAnotherSimpleGraph() {
		RouteMap routeMap = new Dijkstra(() -> Enque.stackOf(new ArrayDeque<>())).compute(
			this.adjList(Arrays.asList(
				new Edge(0, 1, 7.0),
				new Edge(0, 2, 9.0),
				new Edge(0, 5, 14.0),
				new Edge(1, 3, 15.0),
				new Edge(1, 2, 10.0),
				new Edge(2, 3, 11.0),
				new Edge(2, 5, 2.0),
				new Edge(3, 4, 6.0),					
				new Edge(5, 4, 9.0)
			)), 0 
		).get();
		
		Assert.assertEquals(11.0, routeMap.edges(5).mapToDouble(e -> e.weight).sum(), 1e-12);
		Assert.assertEquals(20.0, routeMap.edges(4).mapToDouble(e -> e.weight).sum(), 1e-12);
		Assert.assertEquals(20.0, routeMap.edges(3).mapToDouble(e -> e.weight).sum(), 1e-12);
		Assert.assertEquals(9.0, routeMap.edges(2).mapToDouble(e -> e.weight).sum(), 1e-12);
		Assert.assertEquals(7.0, routeMap.edges(1).mapToDouble(e -> e.weight).sum(), 1e-12);		
	}
	
	@Test
	public void shouldBeAbleToFindRouteInCorridor() throws IOException {
		try(InputStream input = this.getClass().getResourceAsStream("/jacobi/test/data/FloorTest.txt")){
			AdjList adjList = Floor.readAll(input).get("walled-corridors");
			RouteMap routeMap = new Dijkstra(() -> Enque.stackOf(new ArrayDeque<>()))
					.compute(adjList, 0)
					.orElseThrow(() -> new IllegalStateException(""));
			
			List<Edge> path = routeMap.trace(adjList.order() - 1);
			
			for(int i = 0; i < 4; i++) {
				Assert.assertEquals(9, path.get(i).to - path.get(i).from);
			}
			
			for(int i = 0; i < 2; i++) {
				Assert.assertEquals(1, path.get(4 + i).to - path.get(4 + i).from);
			}
			
			for(int i = 0; i < 4; i++) {
				Assert.assertEquals(-9, path.get(6 + i).to - path.get(6 + i).from);
			}
			
			for(int i = 0; i < 2; i++) {
				Assert.assertEquals(1, path.get(10 + i).to - path.get(10 + i).from);
			}
			
			for(int i = 0; i < 4; i++) {
				Assert.assertEquals(9, path.get(12 + i).to - path.get(12 + i).from);
			}
			
			for(int i = 0; i < 2; i++) {
				Assert.assertEquals(1, path.get(16 + i).to - path.get(16 + i).from);
			}
			
			for(int i = 0; i < 4; i++) {
				Assert.assertEquals(-9, path.get(18 + i).to - path.get(18 + i).from);
			}
			
			for(int i = 0; i < 2; i++) {
				Assert.assertEquals(1, path.get(22 + i).to - path.get(22 + i).from);
			}
			
			for(int i = 0; i < 4; i++) {
				Assert.assertEquals(9, path.get(24 + i).to - path.get(24 + i).from);
			}
		}
	}
	
	@Test
	public void shouldBeAbleToFindRouteAroundObstacles() throws IOException {
		try(InputStream input = this.getClass().getResourceAsStream("/jacobi/test/data/FloorTest.txt")){
			AdjList adjList = Floor.readAll(input).get("obstacle");
			RouteMap routeMap = new Dijkstra(() -> Enque.stackOf(new ArrayDeque<>()))
					.compute(adjList, 0)
					.orElseThrow(() -> new IllegalStateException(""));
						
			Assert.assertEquals(9 + Math.sqrt(2.0), routeMap.edges(adjList.order() - 1)
					.reduce((a, b) -> {
						throw new IllegalStateException();
					})
					.map(e -> e.weight)
					.get(), 1e-12);
		}
	}
	
	@Test
	public void shouldReturnEmptyWhenNegativeEdgesDetected() {
		Assert.assertFalse(new Dijkstra(() -> Enque.queueOf(new ArrayDeque<>())).compute(
			this.adjList(Arrays.asList(
				new Edge(0, 1, 2.0),
				new Edge(0, 2, 10.0),
				new Edge(1, 3, 5.0),
				new Edge(2, 3, -9.0)
			)), 0).isPresent()
		);
	}
	
	protected AdjList adjList(List<Edge> edges) {
		int n = edges.stream()
				.mapToInt(e -> Math.max(e.from, e.to))
				.filter(v -> v >= 0)				
				.max().orElse(-1) + 1;		
		return new AdjList() {

			@Override
			public int order() {
				return n;
			}

			@Override
			public Stream<Edge> edges(int from) {
				return edges.stream().filter(e -> e.from == from);
			}
			
		};
	}	

}
