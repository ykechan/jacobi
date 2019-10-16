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
package jacobi.api.graph;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 *  A route map is a graph s.t. an edge {u, v, w} i.e. from u to v with weight w indicates
 *  a unique path of destination v immediately passing through u before has distance w.
 *  
 *  <p>A route to a destination v is unique in a route map, thus every vertices have
 *  at most 1 outgoing edge.</p>
 * 
 * @author Y.K. Chan
 *
 */
public class RouteMap implements AdjList {
	
	/**
	 * Factory method on constructing a route map on distances and intermediate vertex array.
	 * @param dists  Distance array
	 * @param via  Vertex array
	 * @return  Route map
	 */
	public static RouteMap wrap(double[] dists, int[] via) { 
		IntStream.range(0, via.length)
			.filter(i -> i == via[i])
			.findAny()
			.orElseThrow(() -> new IllegalArgumentException("No fixed point found."));
		
		return new RouteMap(new AbstractList<Edge>() {

			@Override
			public Edge get(int index) {
				return via[index] < 0 
					? null 
					: new Edge(index, via[index], dists[index]);
			}

			@Override
			public int size() {
				return via.length;
			}
			
		});
	}
	
	/**
	 * Constructor
	 * @param via  Outgoing edge for each vertices
	 */
	public RouteMap(List<Edge> via) {
		this.via = via;
	}

	@Override
	public int order() {
		return this.via.size();
	}

	@Override
	public Stream<Edge> edges(int from) {
		Edge edge = this.via.get(from);
		return edge == null ? Stream.empty() : Stream.of(edge);
	}
	
	/**
	 * Trace a route to a destination
	 * @param dest  Input destination
	 * @return  List of edges to follow
	 */
	public List<Edge> trace(int dest) {
		List<Edge> path = new ArrayList<>();
		
		Edge edge = this.via.get(dest);		
		while(edge.from != edge.to) {
			path.add(edge);
			edge = this.via.get(edge.to);
			
			if(path.size() >= this.via.size()){
				throw new IllegalArgumentException("Invalid route " + path);
			}
		}
		
		return new AbstractList<Edge>() {

			@Override
			public Edge get(int index) {
				Edge step = path.get(path.size() - 1 - index);
				return new Edge(
					step.to, step.from, 
					step.weight - (index == 0.0 ? 0.0 : path.get(path.size() - index).weight)
				);
			}

			@Override
			public int size() {
				return path.size();
			}
			
		};
	}

	private List<Edge> via;	
}
