package jacobi.core.graph;

import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Stream;

import jacobi.api.graph.AdjList;
import jacobi.api.graph.Edge;
import jacobi.core.util.Enque;
import jacobi.core.util.Weighted;

public class Dijkstra {
	
	public Dijkstra(Supplier<Enque<Weighted<Integer>>> enqueFactory) {
		this.enqueFactory = enqueFactory;
	}

	public AdjList compute(AdjList adjList, int src) {
		double[] dist = new double[adjList.order()];
		
		int[] via = new int[adjList.order()];		
		Arrays.fill(via, -1);				
		
		this.compute(adjList, src, dist, via);
		
		return new Route(dist, via);
	}
	
	protected void compute(AdjList adjList, int src, double[] dist, int[] via) {
		Enque<Weighted<Integer>> enque = this.enqueFactory.get();
		enque.push(new Weighted<>(src, 0.0));
		
		while(!enque.isEmpty()){
			Weighted<Integer> dest = enque.pop();
			
			adjList.edges(dest.item)
				.filter(e -> via[e.to] < 0 || dest.weight + e.weight < dist[e.to])
				.forEach(e -> {
					if(e.weight < 0.0) {
						throw new UnsupportedOperationException("Negative weight not supported.");
					}
					
					via[e.to] = e.from;
					enque.push(new Weighted<>(e.to, dest.weight + e.weight));
				});
		}
	}
	
	private Supplier<Enque<Weighted<Integer>>> enqueFactory;
	
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
