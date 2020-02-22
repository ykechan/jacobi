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
import java.util.Collections;
import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import jacobi.api.Matrix;
import jacobi.api.graph.AdjList;
import jacobi.api.graph.Edge;
import jacobi.core.util.IntStack;
import jacobi.core.util.Throw;

/**
 * Implementation of adjacency list by an adjacency matrix.
 * 
 * <p>An adjacency matrix is another representation of a Graph. An adjacency list can be
 * constructed from an adjacency matrix by extract the non-zero entries of the matrix.</p>
 * 
 * @author Y.K. Chan
 */
public class AdjMatrix implements AdjList {	
	
	/**
	 * Factory method.
	 * @param matrix  Matrix of edge weights
	 * @return  Adjacency matrix
	 */
	public static AdjMatrix of(Matrix matrix) {
		Throw.when()
			.isNull(() -> matrix, () -> "No edge weight matrix.")
			.isTrue(
				() -> matrix.getRowCount() != matrix.getColCount(), 
				() -> "Adjacency matrix must be a square matrix."
			);
		
		int[] temp = new int[matrix.getColCount()];
		int[][] index = new int[matrix.getRowCount()][];
		for(int i = 0; i < index.length; i++) {
			index[i] = AdjMatrix.scatters(matrix.getRow(i), temp);
		}
		
		return new AdjMatrix(
			Collections.unmodifiableList(Arrays.asList(index)), 
			matrix
		);
	}

	/**
	 * Constructor
	 * @param index  Index array for non-zero entries
	 * @param matrix  Adjacency matrix
	 */
	protected AdjMatrix(List<int[]> index, Matrix matrix) {
		this.index = index;
		this.matrix = matrix;
	}

	@Override
	public int order() {
		return this.index.size();
	}

	@Override
	public Stream<Edge> edges(int from) {
		int[] map = this.index.get(from);
		double[] weights = this.matrix.getRow(from);
		IntFunction<Edge> func = i -> new Edge(from, i, weights[i]);
		
		if(map == null) {
			return IntStream.range(0, weights.length)
				.filter(i -> weights[i] != 0.0)
				.mapToObj(func);
		}
		
		if(map.length == 0){
			return Stream.empty();
		}
		
		return (map[0] < 0 ? this.ranges(map) : Arrays.stream(map))
				.mapToObj(func);
	}
	
	/**
	 * Create a integer stream from pairs of begin and end index
	 * @param map  Pairs of begin and end index
	 * @return  Integer stream travsing from begin to end for each pair
	 */
	protected IntStream ranges(int[] map) {
		if(map.length < 4){
			return IntStream.range(map[1], map[2]);
		}
		
		return IntStream.range(0, map.length / 2)
			.flatMap(i -> IntStream.range(map[1 + 2 * i], map[2 + 2 * i]));
	}
	
	private List<int[]> index;
	private Matrix matrix;
	
	/**
	 * Extract indices of non-zero entries from a row of adjacency matrix. 
	 * If continuous entries dominates, this method returns the range of entries in pairs, 
	 * with a -1 in front instead.
	 * @param weights  Input edge weights
	 * @param temp  Temp buffer
	 * @return  Indices of non-zero entries, or -1 followed by range of non-zeo entries in pairs, or
	 *          null if all entries are non-zero
	 */
	protected static int[] scatters(double[] weights, int[] temp) {
		int k = 0;	
		int br = 0;
		for(int i = 0; i < weights.length; i++){
			if(weights[i] != 0.0) {				
				temp[k++] = i;
				continue;
			}			
			
			if(k > 0 && temp[k - 1] == i - 1){
				br++;
			}
		}				
		
		if(k == weights.length) {
			// fully connected
			return null;
		}
		
		return RATIO_TO_EDGES * br < k && RATIO_TO_VERTICES * br < weights.length
			? runLength(temp, k) 
			: Arrays.copyOfRange(temp, 0, k);
	}
	
	/**
	 * Encode separate indices to pairs of begin and end indices, with -1 at front.
	 * @param array  Input array of indices
	 * @param length  Length of input array of interest
	 * @return  -1 followed by range of non-zeo entries in pairs
	 */
	protected static int[] runLength(int[] array, int length) {
		IntStack stack = new IntStack(4);
		stack.push(-1);
		
		int offset = array[0];
		int span = 1;
		
		for(int i = 1; i < length; i++) {
			if(array[i] == offset + span){
				span++;
				continue;
			}
			
			stack.push(offset).push(offset + span);
			offset = array[i];
			span = 1;
		}
		
		if(span > 0) {
			stack.push(offset).push(offset + span);
		}
		return stack.toArray();
	}
	
	/**
	 * Minimum ratio of total number of edges to number of ranges to use pair of ranges
	 */
	protected static final long RATIO_TO_EDGES = 3L;
	
	/**
	 * Minimum ratio of total number of vertices to number of ranges to use pair of ranges
	 */
	protected static final long RATIO_TO_VERTICES = 10L;
}
