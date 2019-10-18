/*
 * The MIT License
 *
 * Copyright 2019 Y.K. Chan
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package jacobi.core.graph;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;

import jacobi.api.graph.AdjList;
import jacobi.api.graph.Edge;
import jacobi.api.graph.RouteMap;
import jacobi.core.util.Enque;
import jacobi.core.util.Weighted;

/**
 * Implementation of the Dijkstra's shortest-path algorithm.
 * 
 * <p>This class computes a route graph for a source vertex s. An edge from u to v
 * in the route graph represents for the shortest path from s get to u in the original
 * graph pass through v via a direct edge in the original path. The weight of the edge 
 * from u to v in the route graph represents the total distance of the shortest path
 * from s to u in the original graph.</p>
 * 
 * <p>Dijkstra's algorithm would not terminate when a negative cycle is encountered.
 * This implementation inspects if such case is encountered and return empty if
 * any negative cycle is detected.
 * </p>
 * 
 * @author Y.K. Chan
 *
 */
public class Dijkstra implements PathFinder {
	
	/**
	 * Constructor
	 * @param enqueFactory  Factory for creating priority queue
	 */
	public Dijkstra(Supplier<Enque<Weighted<Integer>>> enqueFactory) {
		this.enqueFactory = enqueFactory;
	}
	
	@Override
	public Optional<List<Edge>> find(AdjList adjList, int start, int dest) {
		Routes routes = new Routes(new double[adjList.order()], new int[adjList.order()]);
		Arrays.fill(routes.via, -1);
		
		Routes shortestPaths = this.compute(adjList, start, dest, routes);
		return Optional.of(shortestPaths)
			.filter(r -> r.via[dest] >= 0)
			.map(r -> RouteMap.wrap(r.dist, r.via))
			.map(m -> m.trace(dest));
	}

	/**
	 * Compute the shortest path from a source s and returns the route map.
	 * @param adjList  Input graph
	 * @param src  Source s
	 * @return  Route map
	 */
	public Optional<RouteMap> compute(AdjList adjList, int src) {
		
		Routes routes = new Routes(new double[adjList.order()], new int[adjList.order()]);
		Arrays.fill(routes.via, -1);
		routes.via[src] = src;
		
		return Optional
			.ofNullable(this.compute(adjList, src, -1, routes))
			.map(r -> RouteMap.wrap(r.dist, r.via));
	}
	
	/**
	 * Compute the shortest path from a source s and returns the route map.
	 * @param adjList  Input graph
	 * @param src  Source
	 * @param dest  Destination, or negative if paths to all vertices are required
	 * @param routes  Route information
	 * @return  Route information, or null if negative cycle found
	 */
	protected Routes compute(AdjList adjList, int src, int dest, Routes routes) {
		Enque<Weighted<Integer>> enque = this.enqueFactory.get();
		enque.push(new Weighted<>(src, 0.0));
		
		double[] dist = routes.dist;
		int[] via = routes.via;
		
		while(!enque.isEmpty()){
			Weighted<Integer> reach = enque.pop();
			
			if(reach.item == dest) {
				return routes;
			}
			
			int worse = adjList.edges(reach.item)
				.filter(e -> via[e.to] < 0 || reach.weight + e.weight < dist[e.to])
				.map(e -> {
					double prevDist = dist[e.to];
					
					via[e.to] = e.from;
					dist[e.to] = reach.weight + e.weight;
					enque.push(new Weighted<>(e.to, reach.weight + e.weight));
					
					return prevDist < 0.0 && dist[e.to] < 0.0 ? e.to : -1;
				})
				.filter(v -> v >= 0).reduce(Math::min).orElse(-1);

			if(worse >= 0 && this.isCyclic(via, worse)){
				return null;
			}
		}
		
		return routes;
	}
	
	/**
	 * Check if the current route map contains a cycle
	 * @param via  Routes
	 * @param dest  A vertex within the cycle
	 * @return  True if the route map contains a cycle containing the vertex, false otherwise
	 */
	protected boolean isCyclic(int[] via, int dest) {
		Set<Integer> set = new TreeSet<>();
		set.add(dest);
		
		int to = dest;
		for(int i = 0; i < via.length; i++){
			if(set.contains(via[to])) {
				return true;
			}
			
			if(via[to] == to) {
				break;
			}
			
			to = via[to];
		}
		throw new IllegalStateException();
	}
	
	private Supplier<Enque<Weighted<Integer>>> enqueFactory;
	
	/**
	 * Data class for Route information.
	 * 
	 * @author Y.K. Chan
	 *
	 */
	protected static class Routes {
		
		/**
		 * Path distances to each vertices
		 */
		public final double[] dist;
		
		/**
		 * Path element to each vertices
		 */
		public final int[] via;

		/**
		 * Constructor.
		 * @param dist  Path distances to each vertices
		 * @param via  Path element to each vertices
		 */
		public Routes(double[] dist, int[] via) {
			this.dist = dist;
			this.via = via;
		}
		
	}
	
}
