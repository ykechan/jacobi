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
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.function.Consumer;
import java.util.function.IntFunction;

import jacobi.api.spatial.SpatialIndex;
import jacobi.core.util.Enque;
import jacobi.core.util.Weighted;

/**
 * Implementation of R-Tree.
 * 
 * <p>This is a static R-Tree that insertion/deletion is not supported</p>
 * 
 * @author Y.K. Chan 
 * @param <T>  Type of indexed spatial object
 */
public class RTree<T> implements SpatialIndex<T> {
	
	/**
	 * Constructor.
	 * @param root  Root node of the R-tree
	 * @param bDist  Distance function to compute between an aabb and a point
	 * @param pDist  Distance function to compute between two points
	 */
	public RTree(RObject<T> root, RDistance<Aabb> bDist, RDistance<double[]> pDist) {
		this.root = root;
		this.bDist = bDist;
		this.pDist = pDist;
	}
	
	@Override
	public List<T> queryKNN(double[] query, int kMax) {
		if(kMax < 1){
			return Collections.emptyList();
		}
		
		REnque knn = new REnque(Enque.of(new PriorityQueue<>(Weighted.desc())));
		
		REnque queue = new REnque(Enque.of(new PriorityQueue<>()));
		queue.push(this.root, 0.0);	
		
		Consumer<REnque> flush = q -> { while(q.size() > kMax) { q.pop(); } };
		
		while(!queue.isEmpty()){
			Weighted<RObject<T>> top = queue.pop();
			double limit = knn.size() < kMax ? -1 : knn.peek().weight;
			if(limit >= 0.0 && top.weight > limit){
				continue;
			}
			
			this.findNext(queue, query, limit).forEach(obj -> {
				double dist = this.pDist.between(this.toArray(obj.minBoundBox()), query);
				if(knn.size() < kMax || knn.peek().weight > dist){
					knn.push(obj, dist);
					flush.accept(knn);
				}				
			});		
			
			flush.accept(knn);
		}
		
		List<T> results = new ArrayList<>();
		
		while(!knn.isEmpty()) {
			RObject<T> rObj = knn.pop().item;
			results.add(rObj.get().orElseThrow(() -> this.leafIsEmptyException(rObj)));
		}
		
		Collections.reverse(results);
		return results.size() > kMax ? results.subList(0, kMax) : results;
	}

	@Override
	public Iterator<T> queryRange(double[] query, double dist) {		
		double rootDist = this.between(this.root, query);
				
		if(rootDist > dist) {
			return Collections.emptyIterator();
		}
		
		if(this.root.nodeList().isEmpty()){
			return Collections.singleton(this.root.get()
				.orElseThrow(() -> leafIsEmptyException(this.root))
			).iterator();
		}
		
		REnque queue = new REnque(Enque.of(new PriorityQueue<>()));
		queue.push(new Weighted<>(this.root, rootDist));
		
		Enque<T> items = Enque.queueOf(new ArrayDeque<>());
		
		return new Iterator<T>() {

			@Override
			public boolean hasNext() {
				if(!items.isEmpty()){
					return true;
				}
				
				findNext(queue, query, dist).stream()
					.map(obj -> obj.get().orElseThrow( () -> leafIsEmptyException(obj)))
					.forEach(items::push);
				
				return !items.isEmpty();
			}

			@Override
			public T next() {
				if(this.hasNext()){
					return items.pop();
				}
				
				throw new NoSuchElementException();
			}			
			
		};
	}
	
	/**
	 * Get root node of this tree
	 * @return  Root node
	 */
	protected RObject<T> getRoot() {
		return this.root;
	}
	
	/**
	 * Find the next batch of leaf nodes that is within the distance limit to the query point. 
	 * @param queue  Search queue
	 * @param query  Query point
	 * @param limit  Distance limit
	 * @return  List of leaf nodes following the search
	 */
	protected List<RObject<T>> findNext(REnque queue, double[] query, double limit) {
		List<RObject<T>> result = new ArrayList<>();		
		while(!queue.isEmpty() && result.isEmpty()){
			Weighted<RObject<T>> top = queue.pop();
			if(top.weight > limit){
				continue;
			}
			
			for(RObject<T> child : top.item.nodeList()){
				double dist = this.between(child, query);
				if(dist > limit && limit >= 0.0) {
					continue;
				}
				
				queue.push(child, dist);
			}
			
			if(top.item.nodeList().isEmpty()){
				result.add(top.item);
			}
		}
		return result;
	}
	
	/**
	 * Find the distance between a MBB of a R-Object and query point. If the R-Object
	 * is a leaf, the MBB is assumed to be degenerate. 
	 * @param rObj  Input R-Object
	 * @param query  Input query points
	 * @return  Distance between the MBB and the query point
	 */
	protected double between(RObject<?> rObj, double[] query) {		
		
		Aabb mbb = rObj.minBoundBox();
		return rObj.nodeList().isEmpty()
			? this.pDist.between(this.toArray(mbb), query)
			: this.bDist.between(mbb, query);
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
	
	/**
	 * Factory method for an UnsupportedOperationException when a R-node 
	 * checked to be a leaf but no indexed item
	 * @param node  Input node
	 * @return  UnsupportedOperationException
	 */
	protected UnsupportedOperationException leafIsEmptyException(RObject<?> node) {
		throw new UnsupportedOperationException("Expected " + node + " to be a leaf but is empty.");
	}

	private RObject<T> root;
	private RDistance<Aabb> bDist;
	private RDistance<double[]> pDist;
	
	/**
	 * A shorthand for a weighted RObject enque.
	 * 
	 * @author Y.K. Chan
	 *
	 */
	protected class REnque implements Enque<Weighted<RObject<T>>> {
		
		/**
		 * Constructor
		 * @param base  Backing enque
		 */
		public REnque(Enque<Weighted<RObject<T>>> base) {
			this.base = base;
		}

		@Override
		public int size() {
			return this.base.size();
		}
		
		/**
		 * Push a R-Object with distance as weight
		 * @param rObj  R-Object as item
		 * @param weight   Distance as weight
		 * @return  this
		 */
		public REnque push(RObject<T> rObj, double weight) {
			return this.push(new Weighted<>(rObj, weight));
		}

		@Override
		public REnque push(Weighted<RObject<T>> item) {
			this.base.push(item);
			return this;
		}

		@Override
		public Weighted<RObject<T>> pop() {
			return this.base.pop();
		}

		@Override
		public Weighted<RObject<T>> peek() {
			return this.base.peek();
		}

		@Override
		public Weighted<RObject<T>>[] toArray(IntFunction<Weighted<RObject<T>>[]> factory) {
			return this.base.toArray(factory);
		}
		
		private Enque<Weighted<RObject<T>>> base;
	}
}
