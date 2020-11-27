/* 
 * The MIT License
 *
 * Copyright 2020 Y.K. Chan
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
package jacobi.core.spatial.rtree;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import jacobi.core.util.IntStack;

/**
 * Implementation of packing function of spatial objects using greedy algorithm 
 * with distance heuristics.
 * 
 * <p>Given a sorted sequence of spatial objects, the spatial objects are packed
 * in order into nodes in a R-Tree. Assuming acceptable result in the sorting,
 * problem arises when a cluster ends and far away objects are grouped within 
 * a node.</p>
 * 
 * <p>This implementation use a greedy algorithm to find the largest breaking
 * distance within an acceptable number of nodes.</p>
 * 
 * @author Y.K. Chan
 *
 */
public class RDistHeuristicPacker implements Function<List<double[]>, RLayer> {
	
	/**
	 * Constructor.
	 * @param min  Minimum number of objects to include in a node
	 * @param max  Maximum number of objects to include in a node
	 */
	public RDistHeuristicPacker(int min, int max) {
		this.min = min;
		this.max = max;
	}

	@Override
	public RLayer apply(List<double[]> vectors) {
		IntStack stack = IntStack.newInstance();
		
		int done = 0;
		while(done < vectors.size()){
			int pack = this.packFront(vectors, done);
			stack.push(pack);
			done += pack;
		}
		return RLayer.coverOf(stack.toArray(), vectors);
	}
	
	/**
	 * 
	 * @param vectors
	 * @param begin
	 * @return
	 */
	protected int packFront(List<double[]> vectors, int begin) {
		int end = Math.min(begin + this.max, vectors.size());
		if(end - begin <= this.min){
			return end - begin;
		}
		
		double[] first = vectors.get(begin);
		double[] minBd = Arrays.copyOf(first, first.length);
		double[] maxBd = Arrays.copyOf(first, first.length);
		
		double maxDist = 0.0;
		int max = -1;
		
		for(int i = begin + 1; i < end; i++){
			double[] vector = vectors.get(i);
			double dist = 0.0;
			for(int j = 0; j < vector.length; j++){
				double x = vector[j];
				double dx = Math.max(minBd[j] - x, x - maxBd[j]);
				if(dx < 0.0){
					continue;
				}
				
				dist += dx * dx;
				
				if(x < minBd[j]){
					minBd[j] = x;
				}
				
				if(x > maxBd[j]){
					maxBd[j] = x;
				}
			}
			
			System.out.println("#" + i + ": Dist=" + dist);
			if(i - begin < this.min){
				continue;
			}
			
			if(max < 0 || dist > maxDist){
				maxDist = dist;
				max = i;
			}
		}
		System.out.println("max=" + max);
		return max - begin;
	}

	private int min, max;
}
