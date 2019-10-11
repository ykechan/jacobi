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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import jacobi.api.graph.AdjList;
import jacobi.api.graph.Edge;
import jacobi.core.util.Enque;

/**
 * Implementation of traversing a Graph.
 * 
 * <p>To traverse a graph is to visit all vertices through the edges, 
 * starting from a given vertex. This class returns an iterable of edges of travelling
 * so the traversing can be stopped at any given point since the task is done.</p>
 * 
 * <p>A cycle can be traversed endlessly. If a cycle is detected, the cycle is checked
 * by a predicate to indicate continue the search or not. If continue, it ignores the
 * cycle and travel along other edges. If not, the traversing ends.</p>
 * 
 * <p>This class returns also so-called "virtual" edges, which is not an actual edge defined
 * in the graph but an indication of an event. A virtual edge has negative start or finish,
 * and have zero weight. A virtual edge (-1, v) indicates a traversing starts on v, while a
 * virtual edge (v, -1) indicates a traversing ends on v.</p>
 * 
 * <p>This class accepts a factory or container of edges. If the factory creates a stack,
 * the traversing would become a depth-first search. If the factory creates a queue, the
 * traversing would become a breadth-first search. An A*-search can be be achieved by 
 * providing a priority queue.</p>
 * 
 * @author Y.K. Chan
 *
 */
public class Traverser {
	
	/**
	 * Constructor.
	 * @param enqueFactory  Factory to create edge container
	 */
	public Traverser(Supplier<Enque<Edge>> enqueFactory) {
		this.enqueFactory = enqueFactory;
	}
	
	/**
	 * Visit all vertex by starting the traversing on each un-visited vertex.
	 * @param adjList  Adjacency list representing a graph
	 * @param onCycle  Listener on cycles
	 * @return  An iterable that would traverse the graph
	 */
	public Iterable<Edge> search(AdjList adjList, Predicate<Edge[]> onCycle) {
		return () -> {
			byte[] markers = this.init(adjList.order());
			return new Iterator<Edge>() {

				@Override
				public boolean hasNext() {
					if(this.iter.hasNext()){
						return true;
					}
					
					if(this.vertex >= adjList.order()) {
						return false;
					}
					
					this.iter = iterator(adjList, this.vertex++, markers, onCycle);					
					return this.hasNext();
				}

				@Override
				public Edge next() {
					
					return this.iter.next();
				}
				
				private int vertex = 0;
				private Iterator<Edge> iter = Collections.emptyIterator();
			};
		};
	}
	
	/**
	 * Visit all reachable vertex by starting the traversing on a particular vertex.
	 * @param adjList  Adjacency list
	 * @param start  Starting vertex
	 * @param onCycle  Listener on cycles
	 * @return  An iterable that would traverse all reachable vertex from the start
	 */
	public Iterable<Edge> search(AdjList adjList, int start, Predicate<Edge[]> onCycle) {
		return () -> this.iterator(adjList, start, this.init(adjList.order()), onCycle);
	}
	
	/**
	 * Create an iterator that would iterate all reachable vertex from a starting vertex
	 * @param adjList  Adjacency list
	 * @param start  Starting vertex
	 * @param markers  Vertex markers
	 * @param onCycle  Listener on cycles
	 * @return  An iterator that would traverse all reachable vertex from the start
	 */
	protected Iterator<Edge> iterator(AdjList adjList, 
			int start, byte[] markers, 
			Predicate<Edge[]> onCycle) {
		if(markers[start] == PERM) {
			return Collections.emptyIterator();
		}
		
		Enque<Edge> enque = this.enqueFactory.get().push(new Edge(-1, start, 0.0));
		
		return new Iterator<Edge>() {

			@Override
			public boolean hasNext() {
				
				while(!enque.isEmpty()){
					if(markers[enque.peek().to] != PERM){
						break;
					}
					
					enque.pop();
				}
				return !enque.isEmpty();
			}

			@Override
			public Edge next() {
				if(enque.isEmpty()) {
					throw new NoSuchElementException();
				}
				
				Edge edge = enque.peek();
				switch(markers[edge.to]) {
					case NEW :
						markers[edge.to] = TEMP;
						List<Edge[]> cycles = reach(adjList, edge.to, enque, markers);
						if(cycles.stream().anyMatch(onCycle.negate())){
							enque.clear();
						}						
						return edge;
						
					case TEMP :
						enque.pop();
						markers[edge.to] = PERM;
						return new Edge(edge.to, -1, 0.0);
						
					case PERM :
						break;
						
					default :
						break;
				}
				throw new IllegalStateException("Unknown marker " + markers[edge.to] + " found." + edge);
			}
			
		};
	}
	
	/**
	 * Add edges that would reach un-visited vertices from a given vertex, and return any cycle found.
	 * @param adjList  Adjacency list
	 * @param from  From vertex
	 * @param path  Edges up to the from vertex
	 * @param markers  Vertex markers
	 * @return  A list of cycles found
	 */
	protected List<Edge[]> reach(AdjList adjList, int from, Enque<Edge> path, byte[] markers) {
		
		return adjList.edges(from).sequential().map(e -> {
			switch(markers[e.to]) {
				case NEW :
					path.push(e);
					return null;
				
				case TEMP :
					return this.findCycle(path, e);
				
				case PERM :
					return null;
				
				default :
					break;
			}
			
			throw new IllegalStateException("Unknown marker " + markers[e.to] + " found.");
		}).filter(c -> c != null).collect(Collectors.toList());
	}
	
	/**
	 * Find all edges forming a cycle given the edges walked so far and the last edge of the cycle
	 * @param path  Edges walked
	 * @param back  Edge that would return to a visited vertex
	 * @return  All edge that would from a cycle
	 */
	protected Edge[] findCycle(Enque<Edge> path, Edge back) {
		Edge[] edges = path.toArray(n -> new Edge[n]);
		List<Edge> cycle = new ArrayList<>();
		cycle.add(back);
		
		int from = back.from;
		for(int i = edges.length - 1; i >= 0; i--){
			if(from == back.to) {
				break;
			}
			
			if(edges[i].to == from) {
				cycle.add(edges[i]);
				from = edges[i].from;
			}
		}
		
		Collections.reverse(cycle);
		return cycle.toArray(new Edge[0]);
	}
	
	/**
	 * Factory method for an initialized vertex markers
	 * @param len  Number of vertices
	 * @return  Initialized vertex markers
	 */
	protected byte[] init(int len) {
		byte[] markers = new byte[len];
		Arrays.fill(markers, NEW);
		return markers;
	}

	private Supplier<Enque<Edge>> enqueFactory;
	
	/**
	 * Marker value of unvisited vertex
	 */
	protected static final byte NEW = ' ';
	
	/**
	 * Marker value of temporary vertex
	 */
	protected static final byte TEMP = '.';
	
	/**
	 * Marker value of finished vertex
	 */
	protected static final byte PERM = '?';
}
