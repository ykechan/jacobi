package jacobi.core.graph;

import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.stream.Stream;

import jacobi.api.graph.AdjList;
import jacobi.api.graph.Edge;
import jacobi.core.util.Weighted;

public class Dijkstra {
	
	public AdjList compute(AdjList adjList, int src) {
		double[] dist = new double[adjList.order()];
		
		int[] via = new int[adjList.order()];		
		Arrays.fill(via, -1);				
				
		return null;
	}
	
	protected void compute(AdjList adjList, int src, double[] dist, int[] via) {
		
	}
	
	
	
	protected static class Route implements AdjList {
		
		public Route(double[] dist, int[] via) {
			this.dist = dist;
			this.via = via;
		}
						
		@Override
		public int order() {
			return this.via.length;
		}
		
		@Override
		public Stream<Edge> edges(int from) {
			return via[from] < 0
				? Stream.empty()
				: Stream.of(new Edge(from, via[from], dist[from]));
		}
		
		private double[] dist;
		private int[] via;
	}	

}
