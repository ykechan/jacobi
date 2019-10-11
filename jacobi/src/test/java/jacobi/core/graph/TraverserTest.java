package jacobi.core.graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import jacobi.api.graph.Edge;
import jacobi.core.graph.Traverser;
import jacobi.core.util.Enque;

public class TraverserTest {
	
	@Test
	public void shouldBeAbleToDFSA2x2Grid() {
		Iterator<Edge> iter = new Traverser(() -> Enque.stackOf(new ArrayDeque<>()))
				.search(new Grid2D(2,2), 0, c -> true)
				.iterator();
		
		List<Edge> path = new ArrayList<>();
		while(iter.hasNext()) {
			path.add(iter.next());
		}
		
		Assert.assertEquals(new Edge(-1, 0, 0.0), path.get(0));
		Assert.assertArrayEquals(new Edge[] {
				new Edge(-1, 0, 0.0),
				new Edge(0, 2, 1.0),
				new Edge(2, 3, 1.0),
				new Edge(3, -1, 0.0),
				new Edge(2, -1, 0.0),
				new Edge(0,  1, 1.0),
				new Edge(1, -1, 0.0),
				new Edge(0, -1, 0.0),
			}, 
			path.stream().toArray(n -> new Edge[n])
		);
				
	}
	
	@Test
	public void shouldBeAbleToBFSA2x2Grid() {
		Iterator<Edge> iter = new Traverser(() -> Enque.queueOf(new ArrayDeque<>()))
				.search(new Grid2D(2, 2), 0, c -> true)
				.iterator();
		
		List<Edge> path = new ArrayList<>();
		while(iter.hasNext()) {
			path.add(iter.next());
		}
		/*
		Assert.assertArrayEquals(new Edge[] {
				new Edge(-1, 0, 0.0),
				new Edge(0, -1, 0.0),
				new Edge(0, 1, 1.0),
				new Edge(1, -1, 0.0),
				new Edge(0, 2, 1.0),
				new Edge(2, -1, 1.0),
				new Edge(1, 3, 0.0),
				new Edge(0, -1, 0.0),
			}, 
			path.stream().toArray(n -> new Edge[n])
		);
		*/
		System.out.println(path);
	}
	
	@Test
	public void shouldBeAbleToStopWhenCycleFound() {
		
	}
	
	@Test
	public void shouldBeAbleToExtractCycleElementsGivenPureCycle() {
		Edge[] cycle = new Traverser(() -> null).findCycle(
			Enque.stackOf(new ArrayDeque<Edge>())
				.push(new Edge(0, 1, 1.0))
				.push(new Edge(1, 2, 1.0))
				.push(new Edge(2, 3, 1.0)),
			new Edge(3, 0, 1.0)
		);
		
		Assert.assertArrayEquals(Arrays.asList(
			new Edge(0, 1, 1.0),
			new Edge(1, 2, 1.0),
			new Edge(2, 3, 1.0),
			new Edge(3, 0, 1.0)
		).toArray(new Edge[0]), cycle);
	}
	
	@Test
	public void shouldBeAbleToFilterUnrelatedEdgesInCycle() {
		Edge[] cycle = new Traverser(() -> null).findCycle(
			Enque.queueOf(new ArrayDeque<Edge>())
				.push(new Edge(0, 1, 1.0))
				.push(new Edge(1, 2, 1.0))
				.push(new Edge(1, 4, 1.0))
				.push(new Edge(2, 3, 1.0))
				.push(new Edge(2, 5, 1.0)),
			new Edge(3, 0, 1.0)
		);
			
		Assert.assertArrayEquals(Arrays.asList(
			new Edge(0, 1, 1.0),
			new Edge(1, 2, 1.0),
			new Edge(2, 3, 1.0),
			new Edge(3, 0, 1.0)
		).toArray(new Edge[0]), cycle);
	}
	
	@Test
	public void shouldBeAbleToStopOnCycleStart() {
		Edge[] cycle = new Traverser(() -> null).findCycle(
			Enque.queueOf(new ArrayDeque<Edge>())
				.push(new Edge(8, 7, 1.0))
				.push(new Edge(7, 6, 1.0))
				.push(new Edge(6, 0, 1.0))
				.push(new Edge(0, 1, 1.0))
				.push(new Edge(1, 2, 1.0))
				.push(new Edge(1, 4, 1.0))
				.push(new Edge(2, 3, 1.0))
				.push(new Edge(2, 5, 1.0)),
			new Edge(3, 0, 1.0)
		);
				
		Assert.assertArrayEquals(Arrays.asList(
			new Edge(0, 1, 1.0),
			new Edge(1, 2, 1.0),
			new Edge(2, 3, 1.0),
			new Edge(3, 0, 1.0)
		).toArray(new Edge[0]), cycle);
	}

}
