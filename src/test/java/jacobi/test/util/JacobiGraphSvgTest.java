package jacobi.test.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;

import jacobi.api.graph.AdjList;
import jacobi.api.graph.Edge;

public class JacobiGraphSvgTest {
	
	@Test
	public void shouldBeAbleToDrawAndReadFromStandardExample() throws IOException {
		File tempFile = JacobiGraphSvg.builder()
			.add(100.0, 1000.0 - 250.0)
			.add(266.0, 1000.0 - 100.0)
			.add(266.0, 1000.0 - 400.0)
			.add(433.0, 1000.0 - 100.0)
			.add(433.0, 1000.0 - 300.0)
			.add(600.0, 1000.0 - 250.0)
			.connect(2, 4, 33.0)
			.connect(3, 5, -2.0)
			.connect(3, 4, -20.0)
			.connect(4, 5, 1.0)
			.connect(2, 3, 20.0)
			.connect(1, 4, 10.0)
			.connect(1, 3, 50.0)
			.connect(0, 2, 20.0)
			.connect(0, 1, 10.0)
			.get().render().exportTo(null);
		
		try(InputStream input = new FileInputStream(tempFile)){
			AdjList graph = JacobiGraphSvg.readFrom(input).getGraph();
			Assert.assertEquals(6, graph.order());
			
			Assert.assertArrayEquals(new Edge[] {
				new Edge(0, 1, 10.0),
				new Edge(0, 2, 20.0)   
			}, graph.edges(0).sorted((a, b) -> a.to - b.to).toArray(n -> new Edge[n]));
			
			Assert.assertArrayEquals(new Edge[] {
				new Edge(1, 3, 50.0),
				new Edge(1, 4, 10.0)   
			}, graph.edges(1).sorted((a, b) -> a.to - b.to).toArray(n -> new Edge[n]));
			
			Assert.assertArrayEquals(new Edge[] {
				new Edge(2, 3, 20.0),
				new Edge(2, 4, 33.0)   
			}, graph.edges(2).sorted((a, b) -> a.to - b.to).toArray(n -> new Edge[n]));
			
			Assert.assertArrayEquals(new Edge[] {
				new Edge(3, 4,-20.0),
				new Edge(3, 5, -2.0)   
			}, graph.edges(3).sorted((a, b) -> a.to - b.to).toArray(n -> new Edge[n]));
			
			Assert.assertArrayEquals(new Edge[] {
				new Edge(4, 5, 1.0)   
			}, graph.edges(4).sorted((a, b) -> a.to - b.to).toArray(n -> new Edge[n]));
		}
	}	
	
	@Test
	public void shouldBeAbleToDrawAndReadFromPositiveCircle() throws IOException {
		File tempFile = JacobiGraphSvg.builder()
				.add(100.0, 1000.0 - 250.0)
				.add(200.0, 1000.0 - 250.0)
				.add(350.0, 1000.0 - 100.0)
				.add(350.0, 1000.0 - 400.0)
				.add(500.0, 1000.0 - 250.0)
				.add(600.0, 1000.0 - 250.0)
				.connect(3, 1, 5.0)
				.connect(1, 2, 10.0)
				.connect(4, 3, 5.0)
				.connect(2, 4, 7.0)
				.connect(4, 5, 15.0)
				.connect(0, 1, 10.0)
				.get().render().exportTo(null);
		
		try(InputStream input = new FileInputStream(tempFile)){
			AdjList graph = JacobiGraphSvg.readFrom(input).getGraph();
			
			Assert.assertEquals(6, graph.order());
			
			Assert.assertArrayEquals(new Edge[] {
				new Edge(0, 1, 10.0)   
			}, graph.edges(0).sorted((a, b) -> a.to - b.to).toArray(n -> new Edge[n]));
			
			Assert.assertArrayEquals(new Edge[] {
				new Edge(1, 2, 10.0)   
			}, graph.edges(1).sorted((a, b) -> a.to - b.to).toArray(n -> new Edge[n]));
			
			Assert.assertArrayEquals(new Edge[] {
				new Edge(2, 4, 7.0)   
			}, graph.edges(2).sorted((a, b) -> a.to - b.to).toArray(n -> new Edge[n]));
			
			Assert.assertArrayEquals(new Edge[] {
				new Edge(3, 1, 5.0)   
			}, graph.edges(3).sorted((a, b) -> a.to - b.to).toArray(n -> new Edge[n]));
			
			Assert.assertArrayEquals(new Edge[] {
				new Edge(4, 3, 5.0),
				new Edge(4, 5, 15.0)
			}, graph.edges(4).sorted((a, b) -> a.to - b.to).toArray(n -> new Edge[n]));
		}
	}
	
	@Test
	public void shouldBeAbleToDrawAndReadFromNegativeCycle() throws IOException {
		File tempFile = JacobiGraphSvg.builder()
				.add(100.0, 1000.0 - 250.0)
				.add(200.0, 1000.0 - 250.0)
				.add(350.0, 1000.0 - 100.0)
				.add(350.0, 1000.0 - 400.0)
				.add(500.0, 1000.0 - 250.0)
				.add(600.0, 1000.0 - 250.0)
				.connect(0, 1, 10.0)
				.connect(1, 2, -1.0)
				.connect(2, 4, -3.0)
				.connect(4, 3, -10.0)
				.connect(3, 1, -4.0)
				.connect(4, 5, 22.0)
				.get().render().exportTo(null);
		
		try(InputStream input = new FileInputStream(tempFile)){
			AdjList graph = JacobiGraphSvg.readFrom(input).getGraph();
			
			Assert.assertEquals(6, graph.order());
			
			Assert.assertArrayEquals(new Edge[] {
				new Edge(0, 1, 10.0)   
			}, graph.edges(0).sorted((a, b) -> a.to - b.to).toArray(n -> new Edge[n]));
			
			Assert.assertArrayEquals(new Edge[] {
				new Edge(1, 2, -1.0)   
			}, graph.edges(1).sorted((a, b) -> a.to - b.to).toArray(n -> new Edge[n]));
			
			Assert.assertArrayEquals(new Edge[] {
				new Edge(2, 4, -3.0)   
			}, graph.edges(2).sorted((a, b) -> a.to - b.to).toArray(n -> new Edge[n]));
			
			Assert.assertArrayEquals(new Edge[] {
				new Edge(3, 1, -4.0)   
			}, graph.edges(3).sorted((a, b) -> a.to - b.to).toArray(n -> new Edge[n]));
			
			Assert.assertArrayEquals(new Edge[] {
				new Edge(4, 3, -10.0),
				new Edge(4, 5,  22.0)
			}, graph.edges(4).sorted((a, b) -> a.to - b.to).toArray(n -> new Edge[n]));
		}
	}
	
	@Test
	public void shouldBeAbleToDrawAndReadFromACompleteGraphFitInAPentagon() throws IOException {
		JacobiGraphSvg.Builder builder = JacobiGraphSvg.builder().fitUnitCircle(5);
		for(int i = 0; i < 5; i++) {
			for(int j = 0; j < 5; j++) {
				if(i == j) {
					continue;
				}
				
				builder.connect(i, j, 1.0);
			}
		}
		File tempFile = builder.get().render().exportTo(null);
		try(InputStream input = new FileInputStream(tempFile)){
			AdjList graph = JacobiGraphSvg.readFrom(input).getGraph();
			for(int i = 0; i < graph.order(); i++) {
				int from = i;
				Assert.assertArrayEquals(
					IntStream.range(0, graph.order())
						.filter(k -> k != from)
						.mapToObj(k -> new Edge(from, k, 1.0)).toArray(n -> new Edge[n]), 
					graph.edges(i).sorted((a, b) -> a.to - b.to).toArray(n -> new Edge[n])
				);
			}
		}
	}
	
	@Test
	public void shouldBeAbleToDrawAndReadFromARingFitInAHexagon() throws IOException {
		JacobiGraphSvg.Builder builder = JacobiGraphSvg.builder().fitUnitCircle(6);
		for(int i = 0; i < 6; i++) {
			builder.connect(i, (i + 1) % 6, Math.PI);
		}
		
		File tempFile = builder.get().render().exportTo(null);
		try(InputStream input = new FileInputStream(tempFile)){
			AdjList graph = JacobiGraphSvg.readFrom(input).getGraph();
			for(int i = 0; i < graph.order(); i++) {
				Assert.assertArrayEquals(
					new Edge[] {new Edge(i, (i + 1) % 6, 3.14)}, // support 2 decimal places only 
					graph.edges(i).sorted((a, b) -> a.to - b.to).toArray(n -> new Edge[n])
				);
			}
		}
	}
	
	@Test
	public void shouldBeAbleToGenerateRandomGraphWithEdgeIffCloseInEuclideanDist() throws IOException {
		Random rand = new Random(Double.doubleToLongBits(Math.E));
		//JacobiGraphSvg.Builder builder = JacobiGraphSvg.builder();
		double[][] data = new double[64][];
		double limit = 15.0;
		for(int i = 0; i < data.length; i++) {
			data[i] = new double[] {
				Math.round(rand.nextDouble() * 10000.0) / 100.0,
				Math.round(rand.nextDouble() * 10000.0) / 100.0
			};
		}
		JacobiGraphSvg.Builder builder = JacobiGraphSvg.builder();
		for(double[] p : data) {
			builder.add(p[0], p[1]);
		}
		
		for(int i = 0; i < data.length; i++) {
			for(int j = 0; j < data.length; j++) {
				if(i == j) {
					continue;
				}
				
				double dx = data[i][0] - data[j][0];
				double dy = data[i][1] - data[j][1];
				double dist = Math.sqrt(dx * dx + dy * dy);
				
				if(dist < limit) {
					builder.connect(i, j, dist);
				}
			}
		}
		
		builder.get().render().exportTo(null);
	}
	
}
