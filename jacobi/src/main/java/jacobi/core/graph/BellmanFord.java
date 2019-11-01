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
import java.util.function.IntConsumer;

import jacobi.api.graph.AdjList;
import jacobi.api.graph.RouteMap;
import jacobi.core.graph.util.Routes;

/**
 * Implementation of the Bellman-Ford algorithm.
 * 
 * <p>Any path from a starting vertex to a destination vertex can at most passing through 
 * |V| vertices without containing any cycle. Thus the shortest path can be found by
 * relaxing all edges for |V| times at most.</p>
 * 
 * <p>The standard Bellman-Ford examines all edges at each iteration.</p>
 * 
 * @author Y.K. Chan
 *
 */
public class BellmanFord {

	public Optional<RouteMap> compute(AdjList adjList, int src) {
		Routes routes = Routes.init(adjList.order());
		routes.via[src] = src;
		
		return this.compute(adjList, routes);
	}
	
	public Optional<RouteMap> compute(AdjList adjList, Routes routes) {
		int limit = adjList.order();
		return Optional.empty();
	}
	
	protected Optional<Routes> compute(AdjList adjList, Routes routes, IntContainer intArray) {
		int limit = adjList.order() + 1;
		
		for(int i = 0; i < limit; i++){
			if(this.relax(adjList, intArray.iterator(), routes, intArray::add) == 0){
				return Optional.of(routes);
			}
		}
		
		return Optional.empty();
	}
	
	protected int relax(AdjList adjList, IntIterator iter, Routes routes, IntConsumer discover) {
		int done = 0;
		
		while(iter.hasNext()){
			int v = iter.next();
			if(routes.via[v] < 0){
				continue;
			}
			
			double prevDist = routes.dist[v];
			
			done += (int) adjList.edges(v)
				.filter(e -> routes.via[e.to] < 0 || prevDist + e.weight < routes.dist[e.to])
				.filter(e -> {
					if(routes.via[e.to] < 0) {
						discover.accept(e.to);
					}
					
					routes.via[e.to] = v;
					routes.dist[e.to] = prevDist + e.weight;
					return true;
				})
				.count();
		}
		return done;
	}
	
	protected static class IntArray implements IntContainer {

		@Override
		public void add(int elem) {
			this.array = this.ensureCapacity(this.array, this.numElem + 1);
			this.array[this.numElem++] = elem;
		}

		@Override
		public IntIterator iterator() {
			int limit = this.numElem;
			return new IntIterator() {

				@Override
				public boolean hasNext() {
					
					return curr + 1 < limit;
				}

				@Override
				public int next() {
					
					return array[++this.curr];
				}
				
				private int curr = -1;
			};
		}
		
		protected int[] ensureCapacity(int[] array, int target) {
			int len = array.length;
			while(len < target) {
				len += this.step;
				this.step += 2;
			}
			
			return len > array.length ? Arrays.copyOf(array, len) : array;
		}
		
		private int numElem, step;
		private int[] array;
	}
	
	protected interface IntContainer {
		
		public void add(int elem);
		
		public IntIterator iterator();
		
	}
	
	protected interface IntIterator {
		
		public boolean hasNext();
		
		public int next();
		
	}
		
}
