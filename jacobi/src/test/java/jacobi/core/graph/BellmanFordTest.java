package jacobi.core.graph;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;

import jacobi.api.graph.AdjList;
import jacobi.api.graph.Edge;
import jacobi.api.graph.RouteMap;
import jacobi.core.graph.BellmanFord.IntContainer;
import jacobi.core.graph.BellmanFord.IntIterator;

public class BellmanFordTest {
	
	@Test
	public void shouldBeAbleToFindRouteWithNegativeWeights() {
		AdjList adjList = this.adjList(Arrays.asList(
			new Edge(0, 1, 1.0),
			new Edge(0, 2, 10.0),
			new Edge(1, 3, 3.0),
			new Edge(2, 3, -9.0)
		));
		
		RouteMap routeMap = new BellmanFord().compute(adjList, 0)
				.orElseThrow(() -> new UnsupportedOperationException("Fail to find routes"));
		
		Assert.assertArrayEquals(new Edge[] {
				new Edge(0, 2, 10.0),
				new Edge(2, 3, -9.0),
			}, 
			routeMap.trace(3).toArray(new Edge[0]));
	}
	
	@Test
	public void shouldBeAbleToStoreIntInIntArray() {
		IntContainer intCont = new BellmanFord.IntArray(new int[4], 5);
		intCont.add(10);
		intCont.add(9);
		intCont.add(8);
		intCont.add(7);
		intCont.add(6);
		intCont.add(5);
		
		AtomicInteger count = new AtomicInteger(10);
		IntIterator iter = intCont.iterator();
		while(iter.hasNext()) {
			Assert.assertEquals(count.getAndDecrement(), iter.next());
		}
		Assert.assertEquals(4, count.get());
	}
	
	@Test
	public void shouldBeAbleToInsertWhileIteratingIntArray() {
		IntContainer intCont = new BellmanFord.IntArray(new int[4], 5);
		intCont.add(10);
		intCont.add(9);
		intCont.add(8);
		intCont.add(7);
		intCont.add(6);
		intCont.add(5);
		
		AtomicInteger count = new AtomicInteger(10);
		IntIterator iter = intCont.iterator();
		while(iter.hasNext()) {
			intCont.add(100 + count.get());
			Assert.assertEquals(count.getAndDecrement(), iter.next());			
		}
		Assert.assertEquals(4, count.get());
		
		count = new AtomicInteger(10);
		iter = intCont.iterator();
		while(iter.hasNext()) {
			Assert.assertEquals(count.getAndDecrement(), iter.next());
			if(count.get() == 4) {
				count.set(110);
			}
		}
		Assert.assertEquals(104, count.get());
		
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
