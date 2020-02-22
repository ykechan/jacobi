package jacobi.core.graph;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import jacobi.api.graph.AdjList;
import jacobi.api.graph.Edge;

public class Floor implements AdjList {
	
	private Map<String, Floor> plans;
	
	@Before
	public void init() throws IOException {
		try(InputStream input = this.getClass().getResourceAsStream("/jacobi/test/data/FloorTest.txt")){
			this.plans = Floor.readAll(input);
		}
	}
	
	@Test
	public void shouldBeAbleToReadFloorPlans() throws IOException {
		Map<String, Floor> map = this.plans;
			
		Assert.assertTrue(map.containsKey("cell"));
		Assert.assertEquals(3 * 8, map.get("cell").order());
		Assert.assertTrue(map.containsKey("walled-corridors"));
		Assert.assertEquals(5 * 9, map.get("walled-corridors").order());
		Assert.assertTrue(map.containsKey("obstacle"));
		Assert.assertEquals(4 * 9, map.get("obstacle").order());		
	}
	
	@Test
	public void shouldThereBeNoEdgeGoesOutOfBoundary() {
		Floor cell = this.plans.get("cell");
		
		long totalDeg = IntStream.range(0, cell.order())
				.boxed().flatMap(i -> cell.edges(i)).count();
		
		Assert.assertEquals(8 * 6 // 8 directions in the middle
				+ 2 * 5 * 6  // 5 directions in the top and bottom, exclude corners
				+ 3 * 4  // 3 directions for the corners
				+ 5 * 2, // 5 directions on the boundary
			totalDeg);
	}
	
	@Test
	public void shouldThereBeNoEdgeGoesToWall() {
		Floor cell = this.plans.get("walled-corridors");
		for(int i = 0; i < cell.order(); i++) {
			Edge[] edges = cell.edges(i).toArray(n -> new Edge[n]);
			switch(edges.length) {
				case 0 :
					break;
				case 1 :
					Assert.assertTrue(i == 0 || i == cell.order() - 1);
					break;
				case 2 :
					Assert.assertTrue(
						(i % 9) % 2 == 0 // corridors
					// or turning point
					|| i == 3 || i == 7 
					|| i == 4 * 9 + 1
					|| i == 4 * 9 + 5
					);
					break;
				case 4 :
					Assert.assertEquals(1, (i % 9) % 2);
					Assert.assertTrue((i / 9) == 0 || (i / 9) == 4);
					break;
				default :
					throw new IllegalStateException("Invalid degree " + edges.length
							+ " on vertex " + i + " (" + (i % 9) + "," + (i / 9) + ")");
			}
			
			for(Edge e : edges) {
				Assert.assertTrue(
				   e.to - e.from ==  1
				|| e.to - e.from == -1
				|| e.to - e.from ==  9
				|| e.to - e.from == -9
				);
			}
		}
	}
	
	public static Map<String, Floor> readAll(InputStream input) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(input));
		String line = null;
		String name = null;
		List<String> lines = new ArrayList<>();
		
		Map<String, Floor> map = new TreeMap<>();
		
		while((line = reader.readLine()) != null) {
			line = line.trim();
			if(name == null && line.endsWith(":")) {
				name = line.substring(0, line.length() - 1);
				continue;
			}			
			
			if(name != null && line.startsWith("$")) {
				map.put(name, Floor.of(( 
					lines.get(lines.size() - 1).replaceAll("#", "").isEmpty()
					? lines.subList(0, lines.size() - 1)
					: lines
				).toArray(new String[0])));
				
				name = null;
				lines = new ArrayList<>();
				continue;
			}
			
			int pos = line.lastIndexOf('#');
			if(pos > 0 && lines.isEmpty() && line.substring(0, pos).replace("#", "").isEmpty()) {
				// ignore ceiling
				continue;
			}
			
			if(line.startsWith("#") && pos > 0) {	
				
				lines.add(line.substring(1, pos));
			}
		}
		return map;
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
		int x = from % this.width;
		int y = from / this.width;
		
		if(this.plan.get(y).charAt(x) == '#'){
			// no edge comes out of walls
			return Stream.empty();
		}
		
		return IntStream.range(y == 0 ? 3 : 0, y + 1 < this.height ? 9 : 6)
			.filter(i -> i != 4 && -x < i % 3 && x + (i % 3) <= width)
			.filter(i -> this.plan.get(y + (i / 3) - 1).charAt(x + (i % 3) - 1) != '#')
			.filter(i -> i % 2 > 0 || (
				this.plan.get(y).charAt(x + (i % 3) - 1) != '#'
			 && this.plan.get(y + (i / 3) - 1).charAt(x) != '#'
			))
			.mapToObj(i -> new Edge(from, 
				from + (i % 3)- 1 + (i / 3 - 1) * this.width, 
				i % 2 == 0 ? SQRT_2 : 1.0
			));
	}

	private int width, height;
	private List<String> plan;
	
	protected static final double SQRT_2 = Math.sqrt(2.0);
}
