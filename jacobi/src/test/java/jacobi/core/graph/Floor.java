package jacobi.core.graph;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.Test;

import jacobi.api.graph.AdjList;
import jacobi.api.graph.Edge;

public class Floor implements AdjList {
	
	@Test
	public void shouldBeAbleToConstructFloorPlan() {
		AdjList adjList = Floor.of(
			"     ",
			" ##  ",
			"  ## ",			
			"     "
		);
		
		adjList.edges(0).forEach(System.out::println);
	}
	
	public static Floor of(String... plan) {
		int width = Arrays.stream(plan).mapToInt(s -> s.length()).reduce((a, b) -> {
			if(a == b) {
				return a;
			}
			
			throw new IllegalArgumentException("Jagged width");
		}).orElse(0);
		
		//return new Floor(Collections.unmodifiableList(Arrays.asList(plan)), width, plan.length);
		Floor floor = new Floor();
		floor.plan = Collections.unmodifiableList(Arrays.asList(plan));
		floor.width = width;
		floor.height = plan.length;
		return floor;
	}
	
	@Override
	public int order() {
		return this.width * this.height;
	}

	@Override
	public Stream<Edge> edges(int from) {
		int x = from % width;
		int y = from / width;
		double sqrt2 = Math.sqrt(2.0);
		return IntStream.range(y == 0 ? 3 : 0, y + 1 < height ? 6 : 9)
			.filter(i -> (x > 0 || i % 3 > 0) && (x + 1 < width || i % 3 < 2) )
			.filter(i -> i != 4 && plan.get(y + (i / 3) - 1).charAt(x + (i % 3) - 1) != '#')
			.mapToObj(i -> { 
				int u = x + (i % 3) - 1;
				int v = y + (i / 3) - 1;
				return new Edge(from, u * width + u, u == x || v == y ? 1.0 : sqrt2); 
			});
	}

	private int width, height;
	private List<String> plan;
}
