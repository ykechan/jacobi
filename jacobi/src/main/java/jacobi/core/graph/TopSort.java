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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import jacobi.api.graph.AdjList;
import jacobi.api.graph.Edge;
import jacobi.core.util.Enque;

/**
 * Implementation of topological sort.
 * 
 * <p>Topological sort on a graph is sorting the vertices by the following comparing criteria:
 * if there is an edge from u to v, u is less than v.</p>
 * 
 * @author Y.K. Chan
 *
 */
public class TopSort {
	
	/**
	 * Constructor.
	 */
	public TopSort() {
		this(new Traverser(() -> Enque.stackOf(new ArrayDeque<>())));
	}
	
	/**
	 * Constructor.
	 * @param dfs  Implementation of Depth-First Search
	 */
	protected TopSort(Traverser dfs) {
		this.dfs = dfs;
	}

	/**
	 * Sort a graph topologically
	 * @param adjList  A graph represented by an adjacency list
	 * @return  The topological order of vertices, or empty if any cycle exists.
	 */
	public Optional<int[]> sort(AdjList adjList) {
		int[] array = new int[adjList.order()];
		int k = 0;
		
		List<Edge[]> cycles = new ArrayList<>();
		
		Iterator<Edge> iter = this.dfs.search(adjList, c -> !cycles.add(c)).iterator();		
		
		while(iter.hasNext()){
			Edge edge = iter.next();
			
			if(edge.to < 0){
				array[k++] = edge.from;
			}			
		}
		
		return cycles.isEmpty()
			? Optional.of(array).map(this::reverse)
			: Optional.empty();
	}
	
	/**
	 * Reverse the ordering of elements in an array
	 * @param array  Input array
	 * @return  Instance of input array with ordering of elements reversed
	 */
	protected int[] reverse(int[] array) {
		for(int i = 0, j = array.length - 1; i < j; i++, j--) {
			int temp = array[i];
			array[i] = array[j];
			array[j] = temp;
		}
		return array;
	}

	private Traverser dfs;
}
