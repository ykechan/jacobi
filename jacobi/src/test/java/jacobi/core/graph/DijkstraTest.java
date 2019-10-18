package jacobi.core.graph;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
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
	
	protected AdjList grid(List<String> floor) {
		int width = floor.stream().mapToInt(s -> s.length()).reduce((a, b) -> {
			if(a != b) {
				throw new IllegalArgumentException();
			}
			return a;
		}).orElse(0);
		
		int height = floor.size();
		
		double sqrt2 = Math.sqrt(2.0);
		return new AdjList() {

			@Override
			public int order() {
				return width * height;
			}

			@Override
			public Stream<Edge> edges(int from) {
				int x = from % width;
				int y = from / width;
								
				String curr = floor.get(y);
				if(curr.charAt(x) == '#') {
					return Stream.empty();
				}
				
				String prev = y == 0 ? "" : floor.get(y - 1);
				String next = y + 1 < height ? floor.get(y + 1) : "";
				
				return IntStream.range(0, 8)
					.map(i -> i + 1)
					.filter(i -> i != 4)
					.filter(i -> x > 0 || i % 3 > 0)
					.filter(i -> x + 1 < width || i % 2 < 2)
					.filter(i -> prev.isEmpty() || i > 2)
					.filter(i -> next.isEmpty() || i < 6)
					.mapToObj(i -> {
						int dx = (i % 3) - 1;
						int dy = (i / 3) - 1;
						
						if(floor.get(y + dy).charAt(x + dx) == '#'){
							return null;
						}
						return new Edge(from, i, dx == 0 || dy == 0 ? 1.0 : sqrt2);
					})
					.filter(e -> e != null);
			}
			
		};
	}

}
