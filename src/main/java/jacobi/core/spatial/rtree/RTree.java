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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

import jacobi.core.util.Weighted;

/**
 * Implementation of R-Tree.
 * 
 * <p>This is a static R-Tree that insertion/deletion is not supported</p>
 * 
 * @author Y.K. Chan 
 * @param <T>  Type of indexed spatial object
 */
public class RTree<T> {
	
	/**
	 * Constructor.
	 * @param root  Root node of the R-tree
	 * @param bDist  Distance function to compute between an aabb and a point
	 * @param pDist  Distance function to compute between two points
	 */
	public RTree(RNode<T> root, RDistance<Aabb> bDist, RDistance<double[]> pDist) {
		super();
		this.root = root;
		this.bDist = bDist;
		this.pDist = pDist;
	}

	/**
	 * Find the objects within a given distance around a query point q
	 * @param q  Query point q
	 * @param dist  Distance limit
	 * @return  Indexed objects within the given distance
	 */
	public List<T> queryRange(double[] q, double dist) {
		List<T> result = new ArrayList<>();
		
		Deque<RObject<T>> stack = new ArrayDeque<>();		
		stack.push(this.root);
		
		while(!stack.isEmpty()){
			RObject<T> obj = stack.pop();
			if(!this.bDist.isCloser(obj.minBoundBox(), q, dist)){
				continue;
			}
			
			if(!obj.nodes().isEmpty()) {
				obj.nodes().stream().sequential()
					.filter(n -> this.bDist.isCloser(n.minBoundBox(), q, dist))
					.forEach(stack::push);
				continue;
			}	
			
			double[] p = this.toArray(obj.minBoundBox());
			if(this.pDist.isCloser(p, q, dist)){
				result.add(obj.get()
					.orElseThrow(() -> new UnsupportedOperationException(
						"R-Object " + obj + " contains no item or child node."
					))
				);
			}
		}
		return result;
	}
	
	/**
	 * Find the k closest objects from a query point q
	 * @param q  Query point q
	 * @param k  Number of objects interested
	 * @return  At most k indexed closest objects
	 */
	public List<T> queryKNN(double[] q, int k) {
		PriorityQueue<Weighted<T>> result = new PriorityQueue<>();
		PriorityQueue<Weighted<RObject<T>>> queue = new PriorityQueue<>();		
		queue.add(new Weighted<>(this.root, 0.0));
		
		while(!queue.isEmpty()){ 
			Weighted<RObject<T>> head = queue.remove();
			if(result.size() >= k 
			|| !this.bDist.isCloser(head.item.minBoundBox(), q, result.peek().weight)){
				continue;
			}
			
			
		}
		return result.stream().map(w -> w.item).collect(Collectors.toList());
	}	
	
	/**
	 * Create an array from a degenerate aabb.
	 * @param aabb  Input degenerate aabb
	 * @return  Array with the value of the aabb
	 * @throws  UnsupportedOperationException if the aabb is not degenerate
	 */
	protected double[] toArray(Aabb aabb) {
		double[] array = new double[aabb.dim()];
		for(int i = 0; i < array.length; i++){
			array[i] = aabb.min(i);
			if(aabb.max(i) > array[i]) {
				throw new UnsupportedOperationException(
					"Unable to convert non-degenerate AABB to an array."
				);
			}
		}
		return array;
	}

	private RNode<T> root;
	private RDistance<Aabb> bDist;
	private RDistance<double[]> pDist;
}
