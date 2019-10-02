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
import java.util.function.IntFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import jacobi.api.Matrix;
import jacobi.api.graph.AdjList;
import jacobi.api.graph.Edge;

/**
 * Implementation of adjacency list by an adjacency matrix.
 * 
 * <p>An adjacency matrix is another representation of a Graph. An adjacency list can be
 * constructed from an adjacency matrix by extract the non-zero entries of the matrix.</p>
 * 
 * @author Y.K. Chan
 */
public class AdjMatrix implements AdjList {	

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
		
		if(map[0] < 0){
			return this.ranges(weights, map).mapToObj(func);
		}
		return Arrays.stream(map).mapToObj(func);
	}
	
	protected IntStream ranges(double[] weights, int[] map) {
		if(map.length < 4){
			return IntStream.range(map[1], map[2]);
		}
		
		return IntStream.range(0, map.length / 2)
			.flatMap(i -> IntStream.range(map[1 + 2 * i], map[2 + 2 * i]));
	}
	
	private List<int[]> index;
	private Matrix matrix;
	
	protected static int[] scatters(double[] weights, int[] temp) {
		int k = 0;
		for(int i = 0; i < weights.length; i++){
			if(weights[i] != 0.0){
				temp[k++] = i; 
			}
		}
		return Arrays.copyOfRange(temp, 0, k);
	}

	protected static int[] clumps(double[] weights, int[] temp) {
		int k = weights[0] == 0.0 ? 0 : 1;
		temp[0] = 0;
		temp[k] = k;
		for(int i = 1; i < weights.length; i++){
			if(weights[i] == 0.0 ^ weights[i - 1] == 0.0){
				temp[++k] = 0;
			}
			temp[k]++;
		}
		int end = k + (k % 2); // ignore trailing zeros
		int[] regions = new int[1 + end];
		regions[0] = -1;
		
		int pointer = 0;
		for(int i = 1; i < end; i += 2) {
			pointer += temp[i - 1];
			regions[i] = pointer;
			pointer += temp[i];
			regions[i + 1] = pointer;
		}
		
		return regions;
	}
		
}
