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

import java.util.stream.Stream;

/**
 * Common interface for a graph represented as an adjacency list.
 * 
 * <p>A graph is a collection of vertex V and a set of edges E &isin; {(u, v, w) | u, v &isin; V, w &isin; R },
 * which is a pair of vertices with a weight associated with it. An adjacency list is a representation
 * of a Graph which supports the operation of querying all edges where u is given.</p>
 * 
 * <p>In this context vertices are denoted by it's index, and for a graph with N vertices, they
 * are denoted by 0, 1, ... N - 1.</p>
 * 
 * @author Y.K. Chan
 */
public interface AdjList {

	/**
	 * Get the order of the graph, i.e. the number of vertices
	 * @return  Number of vertice
	 */
	public int order();
	
	/**
	 * Get the edges of the graph from a particular vertex
	 * @param from  Index of the vertex
	 * @return  List of edges from a particular vertex
	 */
	public Stream<Edge> edges(int from);	
	
}
