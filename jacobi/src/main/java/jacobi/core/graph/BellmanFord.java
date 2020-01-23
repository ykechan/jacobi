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
 * <p>The standard Bellman-Ford examines all edges at each iteration. However only edges
 * starting from a reachable vertex at the stage would be relaxed, and the first few 
 * phases when the reachable zone is usually small, a lot of edges are retrieved only to
 * be discarded.</p>
 * 
 * <p>This implementation maintains a set of reachable vertex at first, and retrieve edges
 * only from starting in the vertex in this set. When the set grows too large, it is dropped
 * and loop all edges as to saving the effort of maintaining the set.</p>
 * 
 * @author Y.K. Chan
 *
 */
public class BellmanFord {

	/**
	 * Compute the shortest route from a source to each vertex in a graph
	 * @param adjList  Input graph
	 * @param src  Index of source vertex
	 * @return  Route map, or empty if negative cycle detected
	 */
	public Optional<RouteMap> compute(AdjList adjList, int src) {
		Routes routes = Routes.init(adjList.order());
		routes.via[src] = src;
		
		IntContainer reached = this.newContainer(DEFAULT_SET_MIN, adjList.order());
		reached.add(src);
		
		return this.compute(adjList, routes, reached)
				.map(r -> RouteMap.wrap(r.dist, r.via));
	}
	
	/**
	 * Compute the shortest route given intermediate route found in a graph
	 * @param adjList  Input graph
	 * @param routes  Route information 
	 * @return  Route map, or empty if negative cycle detected
	 */
	public Optional<RouteMap> compute(AdjList adjList, Routes routes) {
		IntContainer reached = this.newContainer(DEFAULT_SET_MIN, adjList.order());
		for(int i = 0; i < routes.via.length; i++) {
			if(routes.via[i] < 0){
				continue;
			}
			
			reached.add(i);
		}
		
		return this.compute(adjList, routes, reached)
				.map(r -> RouteMap.wrap(r.dist, r.via));
	}
	
	/**
	 * Compute the shortest route given intermediate route found in a graph
	 * @param adjList  Input graph
	 * @param routes  Route information
	 * @param reachable  Set of vertices that is reachable
	 * @return  Route map, or empty if negative cycle detected
	 */
	protected Optional<Routes> compute(AdjList adjList, Routes routes, IntContainer reachable) {
		int limit = adjList.order() + 1;
		
		for(int i = 0; i < limit; i++){
			if(this.relax(adjList, reachable.iterator(), routes, reachable::add) == 0){
				return Optional.of(routes);
			}
		}
		
		return Optional.empty();
	}
	
	/**
	 * Given a set of reachable vertices, relax the set to include their neighbors that 
	 * is reachable with shorter total distance
	 * @param adjList  Input graph
	 * @param iter  Iterator of reachable vertices
	 * @param routes  Route found so far
	 * @param discover  Invoked when a vertex reached that is previously un-reachable
	 * @return  Number of vertex that a shorter route is discovered in this iteration
	 */
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
					if(routes.via[e.to] < 0){
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
	
	/**
	 * Create a new integer container
	 * @param min  Minimum capacity of the container
	 * @param num  Total number of vertices that will potentially be stored
	 * @return  Integer container
	 */
	protected IntContainer newContainer(int min, int num) {
		int max = num / 3;
		if(max <= min) {
			return new IntArray(new int[Math.min(min, num)], 1);
		}
		
		int lower = 1;
		int upper = min / 2;
		
		while(upper - lower > 1) {
			int mid = (upper + lower) / 2;
			if(mid * mid < min) {
				lower = mid;
			} else {
				upper = mid;
			}
		}
		return new IntLimitedContainer(max, num, new IntArray(new int[min], 2 * lower + 1));
	}
	
	/**
	 * An integer container that stores elements upto a certain limit, and
	 * iterates all integer between 0 and a given value when the limit is exceeded.
	 * 
	 * @author Y.K. Chan
	 *
	 */
	protected static class IntLimitedContainer implements IntContainer {

		/**
		 * Constructor
		 * @param limit  Limit of number of elements to store
		 * @param total  End of iteration when limit is exceeded
		 * @param base  Container base for actually store the elements
		 */
		public IntLimitedContainer(long limit, int total, IntContainer base) {			
			this.limit = limit;
			this.total = total;
			this.base = base;
		}

		@Override
		public void add(int elem) {
			if(++this.size > this.limit) {
				this.base = null;
				return;
			}
			
			this.base.add(elem);
		}

		@Override
		public IntIterator iterator() {
			return this.base == null ? new IntIterator() {

				@Override
				public boolean hasNext() {
					return 1 + this.curr < total;
				}

				@Override
				public int next() {
					return ++this.curr;
				}
				
				private int curr = -1;
				
			} : this.base.iterator();
		}
		
		private long size, limit;
		private int total;
		private IntContainer base;
	}
	
	/**
	 * Integer container that is an extensible array
	 * 
	 * @author Y.K. Chan
	 */
	protected static class IntArray implements IntContainer {
		
		/**
		 * Constructor
		 * @param array  Initial back array
		 * @param step  Initial capacity to increase
		 */
		public IntArray(int[] array, int step) {			
			this.array = array;
			this.step = step;
		}

		@Override
		public void add(int elem) {
			this.array = this.ensureCapacity(this.array, this.numElem + 1);
			this.array[this.numElem++] = elem;
		}

		@Override
		public IntIterator iterator() {
			//int limit = this.numElem;
			return new IntIterator() {

				@Override
				public boolean hasNext() {
					
					return curr + 1 < numElem;
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
	
	/**
	 * Container for primitive integers
	 * 
	 * @author Y.K. Chan
	 *
	 */
	protected interface IntContainer {
		
		/**
		 * Add an integer element to this storage
		 * @param elem  Integer element
		 */
		public void add(int elem);
		
		/**
		 * Get an iterator which iterates the elements in this container 
		 * @return  Element iterator
		 */
		public IntIterator iterator();
		
	}
	
	/**
	 * Minimal interface of an interface for primitive integers
	 * 
	 * @author Y.K. Chan
	 *
	 */
	protected interface IntIterator {
		
		public boolean hasNext();
		
		public int next();
		
	}
		
	protected static final int DEFAULT_SET_MIN = 256;
}
