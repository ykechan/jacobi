package jacobi.core.graph;

import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import jacobi.api.graph.AdjList;
import jacobi.api.graph.Edge;
import jacobi.api.graph.RouteMap;
import jacobi.core.graph.BellmanFord.IntContainer;
import jacobi.core.graph.BellmanFord.IntIterator;
import jacobi.core.graph.util.Routes;
import jacobi.core.util.Real;
import jacobi.test.util.JacobiGraphSvg;
import jacobi.test.util.JacobiSvg;

public class BellmanFordTest {
	
	private File tempDir;
	
	@Before
	public void init() throws IOException {
		Path dir = Files.createTempDirectory("tmp");
		
		try(InputStream input = this.getClass().getResourceAsStream("/jacobi/test/data/BellmanFordTest.zip");			
			ZipInputStream zipIn = new ZipInputStream(new BufferedInputStream(input))){
			
			ZipEntry entry = null;
			while((entry = zipIn.getNextEntry()) != null) {
				if(entry.isDirectory()) {
					Files.createDirectory(dir.resolve(entry.getName()));
					continue;
				}
				
				File outFile = dir.resolve(entry.getName()).toFile();
				try(OutputStream out = new FileOutputStream(outFile)){
					byte[] buf = new byte[4096];
					int len = 0;
					
					while((len = zipIn.read(buf)) > 0) {
						out.write(buf, 0, len);
					}
				}
				
				
			}
		}
		
		this.tempDir = dir.toFile();
	}
	
	@Test
	public void shouldBeAbleToFindRouteInStandardExample() throws IOException {
		try(InputStream input = new FileInputStream(new File(this.tempDir, "standard-example.svg"))){
			JacobiGraphSvg graphSvg = JacobiGraphSvg.readFrom(input);
			AdjList adjList = graphSvg.getGraph();
			
			RouteMap routeMap = new BellmanFord().compute(adjList, 0)
					.orElseThrow(() -> new UnsupportedOperationException("Fail to find routes"));
			
			Assert.assertArrayEquals(new Edge[] {
					new Edge(0, 1, 10.0),
					new Edge(1, 4, 10.0),
					new Edge(4, 5, 1.0)
				}, 
				routeMap.trace(5).toArray(new Edge[0]));
			
			List<Edge> route = routeMap.trace(5);
			JacobiSvg svg = graphSvg.render(); 
			for(Edge e : route) {
				double[] u = graphSvg.getPlacement().getRow(e.from);
				double[] v = graphSvg.getPlacement().getRow(e.to);
				
				svg.arrow(u[0], u[1], v[0], v[1], Color.GREEN);
			}
			
			svg.exportTo(null);			
		}
	}
	
	@Test
	public void shouldBeAbleToFindRouteInRandomClose64() throws IOException {
		try(InputStream input = new FileInputStream(new File(this.tempDir, "random-close-64.svg"))){
			JacobiGraphSvg graphSvg = JacobiGraphSvg.readFrom(input);
			AdjList adjList = graphSvg.getGraph();
			
			RouteMap routeMap = new BellmanFord().compute(adjList, 17)
					.orElseThrow(() -> new UnsupportedOperationException("Fail to find routes"));
			
			List<Edge> route = routeMap.trace(43);
			JacobiSvg svg = graphSvg.render(); 
			for(Edge e : route) {
				double[] u = graphSvg.getPlacement().getRow(e.from);
				double[] v = graphSvg.getPlacement().getRow(e.to);
				
				svg.arrow(u[0], u[1], v[0], v[1], Color.GREEN);
			}
			
			RouteMap revMap = new BellmanFord().compute(adjList, 43)
					.orElseThrow(() -> new UnsupportedOperationException("Fail to find routes"));
			
			Assert.assertTrue(Real.isNegl(
				routeMap.edges(43).findAny().get().weight
			  - revMap.edges(17).findAny().get().weight
			));
			
			List<Edge> backward = revMap.trace(17);
			
			Assert.assertEquals(route.size(), backward.size());
			for(int i = 0; i < route.size(); i++){
				Edge a = route.get(i);
				Edge b = backward.get(backward.size() - 1 - i);
				
				Assert.assertEquals(a.from, b.to);
				Assert.assertEquals(a.to, b.from);
				Assert.assertTrue(Real.isNegl(a.weight - b.weight));
			}
			svg.exportTo(null);
		}
	}		
	
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
	public void shouldBeAbleToFindShortestPathOnALineWithNegativeWeight() {
		AdjList adjList = this.adjList(Arrays.asList(
			new Edge(0, 1, -3.0),
			new Edge(1, 2, 1.0),
			new Edge(2, 3, 1.0),
			new Edge(3, 4, 1.0)
		));
	
		AtomicInteger verify = new AtomicInteger(0);
		
		RouteMap routeMap = new BellmanFord() {

			@Override
			protected int relax(AdjList adjList, 
					IntIterator iter, 
					Routes routes, 
					IntConsumer discover) {
				Assert.assertFalse(finish);
				int done = super.relax(adjList, iter, routes, discover);
				finish = done == 0;
				
				verify.incrementAndGet();
				return done;
			}
			
			private boolean finish = false;			
			
		}.compute(adjList, 0).orElseThrow(() -> new UnsupportedOperationException("Fail to find routes"));
		
		Assert.assertArrayEquals(new Edge[] { new Edge(4, 3, 0.0) }, 
			routeMap.edges(4).toArray(n -> new Edge[n]));
		Assert.assertArrayEquals(new Edge[] { new Edge(3, 2, -1.0) }, 
			routeMap.edges(3).toArray(n -> new Edge[n]));
		Assert.assertArrayEquals(new Edge[] { new Edge(2, 1, -2.0) }, 
			routeMap.edges(2).toArray(n -> new Edge[n]));
		Assert.assertArrayEquals(new Edge[] { new Edge(1, 0, -3.0) }, 
			routeMap.edges(1).toArray(n -> new Edge[n]));
	}
	
	@Test
	public void shouldBeAbleToDetectNegativeCycle() throws IOException {
		try(InputStream input = new FileInputStream(new File(this.tempDir, "contains-negative-cycle.svg"))){
			JacobiGraphSvg graphSvg = JacobiGraphSvg.readFrom(input);
			AdjList adjList = graphSvg.getGraph();
			
			Assert.assertFalse(new BellmanFord().compute(adjList, 0)
				.isPresent());
		}
	}
	
	@Test
	public void shouldBeAbleToFindPathInALongRing() {
		int length = 4096;
		AdjList adjList = new AdjList() {

			@Override
			public int order() {
				return length;
			}

			@Override
			public Stream<Edge> edges(int from) {
				return Collections
					.singletonList(new Edge(from, (from + 1) % length, 1.0))
					.stream();
			}
			
		};
		
		RouteMap routeMap = new BellmanFord().compute(adjList, 0).get();
		Assert.assertEquals(4095, routeMap.edges(4095).findAny().get().weight, 1e-6);
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
		intCont.add(1);
		intCont.add(2);
		intCont.add(3);
		intCont.add(4);
		intCont.add(5);
		
		IntIterator iter = intCont.iterator();
		int k = 0;
		while(iter.hasNext() && ++k <= 10){
			int item = iter.next();
			intCont.add(100 + item);
			
			if(k < 6) {
				Assert.assertEquals(k, item);
			}else{
				Assert.assertEquals(100 + k - 5, item);
			}
		}
	}

	protected AdjList adjList(List<Edge> edges) {
		int n = edges.stream()
				.mapToInt(e -> Math.max(e.from, e.to))
				.filter(v -> v >= 0).max().orElse(-1) + 1;		
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
