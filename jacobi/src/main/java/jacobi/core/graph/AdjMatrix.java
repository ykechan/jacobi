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
import java.util.stream.IntStream;
import java.util.stream.Stream;

import jacobi.api.Matrix;

/**
 * Implementation of adjacency list by an adjacency matrix.
 * 
 * <p>An adjacency matrix is another representation of a Graph. An adjacency list can be
 * constructed from an adjacency matrix by extract the non-zero entries of the matrix.</p>
 * 
 * @author Y.K. Chan
 */
public class AdjMatrix implements AdjList {
	
	public static AdjMatrix of(Matrix matrix) {
		int[] temp = new int[matrix.getColCount()];
		int[][] index = new int[matrix.getRowCount()][];
		for(int i = 0; i < index.length; i++){
			index[i] = AdjMatrix.nonzeros(matrix.getRow(i), temp, 0);
		}
		
		return null;
	}
	
	protected AdjMatrix(List<int[]> index, Matrix matrix) {
		this.index = index;
		this.matrix = matrix;
	}

	@Override
	public int order() {
		return this.matrix.getRowCount();
	}
	
	@Override
	public Stream<Edge> edges(int from) {
		int[] map = this.index.get(from);
		double[] weights = this.matrix.getRow(from);
		
		return map == null
			? IntStream
				.range(0, weights.length)
				.filter(i -> weights[i] != 0.0)
				.mapToObj(i -> new Edge(from, i, weights[i]))
			: Arrays.stream(map).mapToObj(i -> new Edge(from, i, weights[i]));
	}
	
	protected static int[] nonzeros(double[] row, int[] temp, int limit) {
		int k = 0;
		for(int i = 0; i < row.length; i++) {
			if(row[i] == 0) {
				continue;
			}
			
			temp[k++] = i;
			
			if(k > limit) {
				return null;
			}
		}
		return Arrays.copyOfRange(temp, 0, k);
	} 
	
	private List<int[]> index;
	private Matrix matrix;
	
	protected static final double DEFAULT_SPARSE_LIMIT = 0.6;
}
