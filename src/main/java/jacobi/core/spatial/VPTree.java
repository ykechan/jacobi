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
package jacobi.core.spatial;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import jacobi.api.Matrix;
import jacobi.api.spatial.SpatialIndex;
import jacobi.core.util.IntStack;
import jacobi.core.util.MinHeap;

/**
 * Implementation of the vantage-point tree.
 * 
 * <p>
 * The bounding box approach used by R-Tree doesn't scale well in high dimensions, due to
 * high level of overlaps. VP-Tree provides an alternative in such cases. 
 * </p>
 * 
 * <p>VP-Tree is a binary tree that divides the data into left and right sub-trees, with
 * the left being all the points with distance to a certain vantage point under a threshold,
 * and the right all points otherwise. If the median of the distances to the vantage point
 * is chosen as the threshold, it would result in a balanced tree.</p>
 * 
 * <p>In range query, let q be the query point and d be the query distance. If the distance
 * of q to the vantage point v |q - v| &lt; r - d, where r is the threshold distance of the 
 * tree, it goes to search the left sub-tree. If |q - v| &gt; r + d, it goes to search the
 * right sub-tree. Otherwise the query window overlaps both sub-trees and both need to be
 * searched.</p>
 * 
 * <p>This implementation uses an integer array to represent the binary tree. The integer
 * are index of the point in the data matrix. For a node with a[k] in the array, a[2k + 1]
 * would be its left child and a[2k + 2] its right child, with k being zero-based. A separate
 * array would be used to store the radius of the entire sub-tree. The radius array is thus
 * shorter since leaf needs no such information.</p>
 * 
 * @author Y.K. Chan
 */
public class VPTree implements SpatialIndex<Integer> {

	/**
	 * Constructor.
	 * @param data  Input data vectors
	 * @param index  Array representation of the binary tree
	 * @param radius  Radius for each internal node
	 */
	protected VPTree(Matrix data, int[] index, double[] radius) {
		this.data = data;
		this.index = index;
		this.radius = radius;
	}

	@Override
	public List<Integer> queryKNN(double[] query, int kMax) {
		if(this.index.length == 0){
			return Collections.emptyList();
		}
		
		MinHeap heap = MinHeap.ofMax(kMax);
		
		IntStack stack = IntStack.newInstance().push(0);
		while(!stack.isEmpty()){
			int node = stack.pop();
			double[] vector = this.data.getRow(this.index[node]);
			double vDist = DirectQuery.sqDist(query, vector);
			
			if(heap.size() >= kMax && heap.min() < vDist){
				continue;
			}
			
			heap.push(node, vDist);
			
			this.push(stack, node, heap.min(), vDist);
		}
		return Arrays.stream(heap.flush()).boxed().collect(Collectors.toList());
	}

	@Override
	public Iterator<Integer> queryRange(double[] query, double dist) {
		if(this.index.length == 0){
			return Collections.emptyIterator();
		}
		
		double qDist = dist * dist;
		IntStack result = IntStack.newInstance();
		
		IntStack stack = IntStack.newInstance().push(0);
		while(!stack.isEmpty()){
			int node = stack.pop();
			double[] vector = this.data.getRow(this.index[node]);
			double vDist = DirectQuery.sqDist(query, vector);
			
			if(vDist < qDist){
				result.push(node);
			}
			
			this.push(stack, node, qDist, vDist);
		}
		
		return Arrays.stream(result.toArray()).boxed().iterator();
	}
	
	/**
	 * Push the next query node onto the stack, given query distance and vantage point distance
	 * @param stack  Query stack
	 * @param node  Current node
	 * @param qDist  Query distance
	 * @param vDist  Distance to vantage point
	 * @return  Query stack
	 */
	protected IntStack push(IntStack stack, int node, double qDist, double vDist) {
		if(node >= this.radius.length){
			return stack;
		}
		
		double rDist = this.radius[node];
		if(vDist <= rDist + qDist){
			// fail to reject left sub-tree
			stack.push(2 * node + 1);
		}
		
		if(vDist <= rDist - qDist){
			// fail to reject right sub-tree
			stack.push(2 * node + 2);
		}
		return stack;
	}
	
	private Matrix data;
	private int[] index;
	private double[] radius;
}
