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

import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntFunction;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.api.graph.AdjList;
import jacobi.api.graph.RouteMap;

/**
 * Implementation of the Floyd-Warshall shortest-path algorithm.
 * 
 * <p>Floyd-Warshall is a dynamic programming algorithm. Any path from u to v, there exists
 * a k s.t. there is an edge from k to v (k can be equal to u). Given the shortest distance 
 * from u to v for all u, v &isin; [0, N), k &isin; [0, m), the shortest distance from 
 * u to v given k &isin; [0, m] can be found by the recursive relation:   
 * </p>
 * 
 * <p>d(u, v, m) = min{ d(u, v, m - 1), d(u, k, m - 1) + d(k, v, m - 1) | &forall; k &isin; [0, m] }</p>
 * 
 * @author Y.K. Chan
 *
 */
public class FloydWarshall {
	
	public List<RouteMap> compute(AdjList adjList) {
		return null;
	}
	
	/**
	 * Compute the distance matrix between each two vertices
	 * @param adjList  Input graph
	 * @return  Distance matrix
	 */
	public Matrix computeDist(AdjList adjList) {
		Matrix dist = this.init(adjList);
		Matrix temp = Matrices.zeros(adjList.order());
		
		for(int k = 0; k < adjList.order(); k++) {
			Matrix tmp = this.dynProg(dist, temp, k, null);
			temp = dist;
			dist = tmp;
		}
		return dist;
	}
	
	/**
	 * Compute the next stage in dynamic programming
	 * @param prev  Previous distance matrix
	 * @param next  Next distance matrix
	 * @param k  Maximum vertex to consider as intermediate vertex
	 * @param via  Map of intermediate vertex
	 * @return  Next distance matrix
	 */
	protected Matrix dynProg(Matrix prev, Matrix next, int k, List<int[]> via) {
		for(int i = 0; i < next.getRowCount(); i++) {
			double[] nextRow = next.getRow(i);
			double[] prevRow = prev.getRow(i);
			
			int[] viaRow = via == null || via.isEmpty() ? null : via.get(i);
			
			for(int j = 0; j < next.getColCount(); j++) {
				double viaK = prevRow[k] + prev.get(k, j);
				nextRow[j] = Math.min(prevRow[j], viaK);
				
				if(viaK < prevRow[j]){
				    viaRow[j] = k;
				}
			}
			
			next.setRow(i, nextRow);
		}
		return next;
	}
	
	/**
	 * Initialize the distance matrix
	 * @param adjList  Input graph
	 * @return  Distance matrix with 0 distance to its own vertex, and the weight of
	 *          direct edge as distance to neighbours, and infinity on any entries else.
	 */
	protected Matrix init(AdjList adjList) {
		Matrix dist = Matrices.zeros(adjList.order());
		
		for(int i = 0; i < adjList.order(); i++){
			double[] row = dist.getRow(i);
			Arrays.fill(row, Double.POSITIVE_INFINITY);
			
			adjList.edges(i).forEach(e -> row[e.to] = e.weight);
			row[i] = 0.0;
			dist.setRow(i, row);
		}
		return dist;
	}
	
	protected <T> List<T> lazyList(IntFunction<T> func, int len) {
		return new AbstractList<T>() {

			@Override
			public T get(int index) {
				return func.apply(index);
			}

			@Override
			public int size() {
				return len;
			}
			
		};
	}

}
