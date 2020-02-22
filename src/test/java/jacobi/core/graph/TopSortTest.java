package jacobi.core.graph;

import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;

import jacobi.api.graph.AdjList;
import jacobi.api.graph.Edge;

public class TopSortTest {
	
	@Test
	public void shouldBeAbleToSortALine() {
		int[] vertices = new TopSort().sort(this.line(5))
			.orElseThrow(() -> new IllegalStateException("A line should not contain any cycle"));
		
		Assert.assertArrayEquals(new int[] {0, 1, 2, 3, 4}, vertices);
	}
	
	@Test
	public void shouldNotBeAbleToSortARing() {
		Optional<int[]> vertices = new TopSort().sort(this.ring(5));
		Assert.assertFalse(vertices.isPresent());
	}
	
	@Test
	public void shouldBeAbleToSortBipartiteGraph() {
		int[] vertices = new TopSort().sort(this.bipartite(3, 2, true))
				.orElseThrow(() -> new IllegalStateException("A bipartite should not contain any cycle"));
			
		Assert.assertArrayEquals(new int[] {2, 1, 0, 3, 4}, vertices);
	}
	
	@Test
	public void shouldBeAbleToSortRadial() {
		int[] vertices = new TopSort().sort(this.radial(10))
				.orElseThrow(() -> new IllegalStateException("A radial network should not contain any cycle"));
		Assert.assertEquals(0, vertices[0]);
		for(int i = 1; i < vertices.length; i++) {
			Assert.assertTrue(vertices[i] > 0);
		}
	}
	
	protected AdjList line(int n) {
		return new AdjList() {

			@Override
			public int order() {
				return n;
			}

			@Override
			public Stream<Edge> edges(int from) {
				return from < n - 1 
					? Stream.of(new Edge(from, from + 1, 1.0))
					: Stream.empty();
			}
			
		};
	}
	
	protected AdjList ring(int n) {
		return new AdjList() {

			@Override
			public int order() {
				return n;
			}

			@Override
			public Stream<Edge> edges(int from) {
				return Stream.of(new Edge(from, (from + 1) % n, 1.0));
			}
			
		};
	}
	
	protected AdjList bipartite(int m, int n, boolean forward) {
		return new AdjList() {

			@Override
			public int order() {
				return m + n;
			}

			@Override
			public Stream<Edge> edges(int from) {
				int p = forward ? m : n;
				int q = forward ? n : m;
				return from < p
					? IntStream.range(0, q)
						.map(i -> p + i)
						.mapToObj(j -> new Edge(from, j, 1.0))
					: Stream.empty();
			}
			
		};
	}
	
	protected AdjList radial(int n) {
		return new AdjList() {

			@Override
			public int order() {
				return n;
			}

			@Override
			public Stream<Edge> edges(int from) {
				if(from != 0) {
					return Stream.empty();
				}
				
				return IntStream.range(1, n)
					.map(i -> n - i)
					.mapToObj(i -> new Edge(0, i, 1.0));
			}
			
		};
	}

}
