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
import java.util.function.Supplier;
import java.util.stream.Stream;

import jacobi.api.graph.AdjList;
import jacobi.api.graph.Edge;
import jacobi.api.graph.RouteMap;
import jacobi.core.util.Enque;
import jacobi.core.util.Weighted;

/**
 * Implementation of the Dijkstra shortest-path algorithm.
 * 
 * <p>This class computes a route graph for a source vertex s. An edge from u to v
 * in the route graph represents for the shortest path from s get to u in the original
 * graph pass through v via a direct edge in the original path. The weight of the edge 
 * from u to v in the route graph represents the total distance of the shortest path
 * from s to u in the original graph. </p>
 * 
 * @author Y.K. Chan
 *
 */
public class Dijkstra {
	
	/**
	 * Constructor
	 * @param enqueFactory  Factory for creating priority queue
	 */
	public Dijkstra(Supplier<Enque<Weighted<Integer>>> enqueFactory) {
		this.enqueFactory = enqueFactory;
	}

	/**
	 * Compute the shortest path from a source s and returns the route map.
	 * @param adjList  Input graph
	 * @param src  Source s
	 * @return  Route map
	 */
	public RouteMap compute(AdjList adjList, int src) {
		double[] dist = new double[adjList.order()];
		
		int[] via = new int[adjList.order()];		
		Arrays.fill(via, -1);				
		
		this.compute(adjList, src, dist, via);
		
		return RouteMap.wrap(dist, via);
	}
	
	protected void compute(AdjList adjList, int src, double[] dist, int[] via) {
		Enque<Weighted<Integer>> enque = this.enqueFactory.get();
		enque.push(new Weighted<>(src, 0.0));
		
		while(!enque.isEmpty()){
			Weighted<Integer> dest = enque.pop();
			
			adjList.edges(dest.item)
				.filter(e -> via[e.to] < 0 || dest.weight + e.weight < dist[e.to])
				.forEach(e -> {
					via[e.to] = e.from;
					enque.push(new Weighted<>(e.to, dest.weight + e.weight));
				});
		}
	}
	
	private Supplier<Enque<Weighted<Integer>>> enqueFactory;
}
