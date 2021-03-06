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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.PrimitiveIterator.OfInt;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.stream.Collectors;

import jacobi.api.Matrix;
import jacobi.api.spatial.SpatialIndex;
import jacobi.core.util.IntStack;
import jacobi.core.util.MinHeap;

/**
 * Implementation of a static inline R-Tree.
 * 
 * <p>An inline R-Tree is an R-Tree which an all siblings of the same depth are represented
 * as a data structure called a R-Layer. R-Layer contains the end index and minimum bounding
 * box for each nodes serialized into a cut array and bounds array. In this way memory locality
 * is better utilitized.</p>
 * 
 * <p>All leaves are serialized in similar way, with cut array represents the index of the vector
 * in the original data matrix. It is also an option to serialize the matrix as well for better
 * access performance in order.</p>
 * 
 * @author Y.K. Chan
 *
 */
public class RInlineTree implements SpatialIndex<Integer> {

	/**
	 * Constructor
	 * @param index  Index of nodes in R-Tree, from top to bottom
	 * @param leaves  Leave nodes
	 * @param matrix  Data matrix
	 */
	public RInlineTree(List<RLayer> index, RLayer leaves, Matrix matrix) {
		this.index = index;
		this.leaves = leaves;
		this.matrix = matrix;
		this.lazy = DEFAULT_LAZY_LENGTH;
	}
	
	protected void setLazy(int lazy) {
		this.lazy = lazy;
	}

	@Override
	public List<Integer> queryKNN(double[] query, int kMax) {
		if(query == null){
			throw new IllegalArgumentException("No query point.");
		}
		
		RLayer root = this.index.get(0);
		if(query.length != root.dim()){
			throw new IllegalArgumentException("Dimension mismatch.");
		}
		
		MinHeap heap = this.queryAStar(query, kMax);
		
		int[] kNN = heap.flush();
		// in asc order
		for(int i = 0, j = kNN.length - 1; i < j; i++, j--){
			int temp = kNN[i]; kNN[i] = kNN[j]; kNN[j] = temp;
		}
		
		return Arrays.stream(kNN).boxed().collect(Collectors.toList());
	}

	@Override
	public Iterator<Integer> queryRange(double[] query, double dist) {
		if(query == null){
			throw new IllegalArgumentException("No query point.");
		}
		
		RLayer root = this.index.get(0);
		if(query.length != root.dim()){
			throw new IllegalArgumentException("Dimension mismatch.");
		}
		
		if(dist < 0.0){
			return Collections.emptyIterator();
		}
		
		double qDist = dist * dist;
		int[] filter = {root.length()};
		
		for(RLayer rLayer : this.index){
			filter = this.queryFilter(rLayer, query, qDist, filter);
		}
		
		return this.queryRangeByLeaves(query, qDist, filter);
	}
	
	/**
	 * Query the first k nearest neighbor found by A*-star search
	 * @param query  Query point
	 * @param kMax  Maximum number of nearest neighbor
	 * @return  Min-heap contains at least k
	 */
	protected MinHeap queryAStar(double[] query, int kMax) {
		Queue<Node> queue = new PriorityQueue<>(Comparator.comparingDouble(n -> n.dist));
		MinHeap heap = MinHeap.ofMax(kMax);
		
		RLayer root = this.index.get(0);
		
		queue.offer(new Node(-1, 0, 0.0));
		while(!queue.isEmpty()){
			Node node = queue.remove();
			if(heap.size() >= kMax && -heap.peek().weight < node.dist){
				break;
			}
			
			RLayer parent = node.depth < 0 ? null : this.index.get(node.depth);
			int begin = parent == null || node.index == 0 ? 0 : parent.cuts[node.index - 1];
			int end = parent == null ? root.length() : parent.cuts[node.index];
			
			int depth = node.depth + 1;
			if(depth < this.index.size()){
				RLayer rLayer = this.index.get(depth);
				
				double[] bounds = rLayer.bounds;
				
				for(int i = begin; i < end; i++){
					double dist = this.distMbb(query, bounds, i, Double.MAX_VALUE);
					queue.offer(new Node(depth, i, dist));
				}
				continue;
			}
			
			double[] bounds = this.leaves.bounds;
			int[] seq = this.leaves.cuts;
			// query leaves
			for(int i = begin; i < end; i++){				
				double dist = this.distFn(query, bounds, i);
				heap.push(seq[i], -dist);
			}
		}
		return heap;
	}
	
	/**
	 * Filter child nodes by MBBs
	 * @param rLayer  R-Layer of current Nodes
	 * @param query  Query position
	 * @param maxDist  Dismiss if distance is greater than this limit
	 * @param filter  Sequence of access to current nodes
	 * @return  Sequence of access to child nodes
	 */
	protected int[] queryFilter(RLayer rLayer, double[] query, double maxDist, int[] filter) {
		int[] cuts = rLayer.cuts;
		double[] bounds = rLayer.bounds;
		
		IntStack stack = IntStack.newInstance();
		
		int at = 0;
		int run = 0;
		for(int i = 0; i < filter.length; i++){
			int len = filter[i];
			if(len < 0){
				int end = at - len - 1;
				int skip = cuts[end] - (at == 0 ? 0 : cuts[at - 1]);
				at -= len;
				
				if(run > 0){
					stack.push(run);
					run = 0;
				}
				
				run -= skip;
				continue;
			}
			
			for(int j = 0; j < len; j++){
				int span = cuts[at + j] - (at + j == 0 ? 0 : cuts[at + j - 1]);
				double dist = this.distMbb(query, bounds, at + j, maxDist);
				
				int sgn = dist > maxDist ? -1 : 1;
				
				if(sgn * run < 0){
					
					stack.push(run);
					run = sgn * span;
				}else{
					run += sgn * span;
				}
			}
			
			at += len;
		}
		
		if(run > 0){
			stack.push(run);
		}
		
		return stack.toArray();
	}
	
	/**
	 * Query all vectors in leaf nodes within range with the query point by filter. This method returns a lazy
	 * iterator if number of matches are for short circuit if result found is sufficient.
	 * @param query  Query point
	 * @param qDist  Range distance
	 * @param filter  Sequence of access to leaf nodes
	 * @return  Iterator to indices of all vectors within range
	 */
	protected Iterator<Integer> queryRangeByLeaves(double[] query, double qDist, int[] filter) {
		int total = Arrays.stream(filter).sum();		
		if(total < this.lazy){
			return this.queryRangeListByLeaves(query, qDist, filter);
		}
		
		double[] bounds = this.leaves.bounds;
		int[] seq = this.leaves.cuts;
		
		OfInt iter = Arrays.stream(filter).iterator();
		
		int dim = bounds.length / 2 / seq.length;
		int gallop = Math.max(1, GALLOP_WIDTH / dim);
		
		// lazy
		return new Iterator<Integer>() {

			@Override
			public boolean hasNext() {
				if(!this.stack.isEmpty()){
					return true;
				}
				
				int done = 0;
				while(this.begin < this.end){
					if(!this.stack.isEmpty() && done++ > gallop){
						return true;
					}
					
					int index = this.begin++;
					double pDist = distFn(query, bounds, index);
					if(pDist > qDist){
						continue;
					}
					
					this.stack.push(seq[index]);
				}
				
				if(!this.stack.isEmpty()){
					return true;
				}
				
				if(iter.hasNext()){
					int span = iter.nextInt();
					if(span > 0){
						this.end = this.begin + span;
					} else {
						this.begin -= span;
					}
					return this.hasNext();
				}
				
				return false;
			}

			@Override
			public Integer next() {
				return this.stack.pop();
			}
			
			private int begin, end;
			private IntStack stack = IntStack.newInstance();
		};
	}
	
	/**
	 * Query all vectors in leaf nodes within range with the query point by filter
	 * @param query  Query point
	 * @param qDist  Range distance
	 * @param filter  Sequence of access to leaf nodes
	 * @return  Iterator to indices of all vectors within range
	 */
	protected Iterator<Integer> queryRangeListByLeaves(double[] query, double qDist, int[] filter) {
		int[] seq = this.leaves.cuts;
		double[] array = this.leaves.bounds;
		
		List<Integer> result = new ArrayList<>();
		
		int begin = 0;
		for(int s : filter){
			if(s < 0){
				begin -= s;
				continue;
			}
			
			int end = begin + s;
			for(int i = begin; i < end; i++){
				double pDist = this.distFn(query, array, i);
				if(qDist < pDist){
					continue;
				}
				
				result.add(seq[i]);
			}
			begin = end;
		}
		return result.iterator();
	}
	
	/**
	 * Query all vectors in leaf nodes to heap with the query point by filter
	 * @param query  Query point
	 * @param heap   Min heap
	 * @param filter  Sequence of access to leaf nodes
	 * @return  Iterator to indices of all vectors within range
	 */
	protected List<Integer> queryRangeHeapByLeaves(double[] query, MinHeap heap, int[] filter) {
		int[] seq = this.leaves.cuts;
		double[] array = this.leaves.bounds;
		
		int begin = 0;
		for(int s : filter){
			if(s < 0){
				begin -= s;
				continue;
			}
			
			int end = begin + s;
			for(int i = begin; i < end; i++){
				double pDist = this.distFn(query, array, i);
				heap.push(seq[i], pDist);
			}
			begin = end;
		}
		return Arrays.stream(heap.flush()).boxed().collect(Collectors.toList());
	}
	
	/**
	 * Compute the distance between a query point and the MBB of a node
	 * @param query  Query point
	 * @param bounds MBBs of the R-layer which the node resides
	 * @param at  Index of the node in the layer
	 * @param maxDist  Dismiss if distance is greater than this limit
	 * @return  Distance between a query point and the MBB of a node
	 */
	protected double distMbb(double[] query, double[] bounds, int at, double maxDist) {
		int mbbLen = 2 * query.length;
		int index = at * mbbLen;
		
		double dist = 0.0;
		for(int i = 0; i < mbbLen; i+=2){
			
			double x = query[i / 2];
			double dx = Math.max(bounds[index + i] - x, x - bounds[index + i + 1]);
			if(dx < 0.0){
				continue;
			}
			
			dist += dx * dx;
			if(dist > maxDist){
				return dist;
			}
		}
		return dist;
	}
	
	/**
	 * Compute the distance between a query point and a serialized array of vectors
	 * @param query  Query point
	 * @param array  Serialized array of vectors
	 * @param at  Index of the vector to compute the distance against
	 * @return  Distance between the query point and the vector
	 */
	protected double distFn(double[] query, double[] array, int at) {
		int idx = at * query.length;
		
		double dist = 0.0;
		for(int i = 0; i < query.length; i++){
			double dx = query[i] - array[idx + i];
			dist += dx * dx;
		}
		return dist;
	}
	
	private List<RLayer> index;
	private RLayer leaves;
	private Matrix matrix;
	private int lazy;
	
	/**
	 * Data object of an address to a node in the R-Tree
	 * 
	 * @author Y.K. Chan
	 *
	 */
	protected static class Node {
		
		/**
		 * Distance to this node
		 */
		public final double dist;
		
		/**
		 * Depth of this node
		 */
		public final int depth;
		
		/**
		 * Index of this node
		 */
		public final int index;
		
		/**
		 * Constructor
		 * @param depth  Depth of this node
		 * @param index  Index of this node
		 * @param dist  Distance to this node
		 */
		public Node(int depth, int index, double dist) {
			this.dist = dist;
			this.depth = depth;
			this.index = index;
		}
		
	}
	
	protected static final int DEFAULT_LAZY_LENGTH = 8;
	
	protected static final int GALLOP_WIDTH = 32;
}
