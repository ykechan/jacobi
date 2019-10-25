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
import java.util.Optional;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

import jacobi.api.graph.AdjList;
import jacobi.api.graph.RouteMap;

/**
 * Implementation of the Bellman-Ford algorithm.
 * 
 * <p>Any path from a starting vertex to a destination vertex can at most passing through 
 * |V| vertices without containing any cycle. Thus the shortest path can be found by
 * relaxing all edges at most for |V| times.</p>
 * 
 * @author Y.K. Chan
 *
 */
public class BellmanFord {
	
	public Optional<RouteMap> compute(AdjList adjList, int src) {
		double[] dist = new double[adjList.order()];
		int[] via = new int[adjList.order()];
		Arrays.fill(via, -1);
		
		via[src] = src;
		
		return this.compute(adjList, dist, via);
	}
	
	protected Optional<RouteMap> compute(AdjList adjList, double[] dist, int[] via) {
		int num = adjList.order() + 1;
		
		for(int i = 0; i < num; i++) {
			if(this.relax(adjList, dist, via, IntStream.range(0, adjList.order())) == 0){
				return Optional.of(RouteMap.wrap(dist, via));
			}
		}
		
		return Optional.empty();
	}
	
	protected int relax(AdjList adjList, double[] dist, int[] via, IntStream boundaries) {
		
		return boundaries.filter(i -> via[i] >= 0).boxed().flatMap(adjList::edges).sequential()
			.mapToInt(e -> {			
				
				if(via[e.to] < 0 || dist[e.from] + e.weight < dist[e.to]){
					via[e.to] = e.from;
					dist[e.to] = dist[e.from] + e.weight;
					return 1;
				}
				
				return 0;
		}).sum();
	}

	private IntFunction<Set<Integer>> setFactory;
	private int limit;
}
