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
package jacobi.api.spatial;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

/**
 * Common interface of a spatial index.
 * 
 * @author Y.K. Chan
 * @param <T>  Indexed item
 */
public interface SpatialIndex<T> {
	
	/**
	 * Find k nearest neighbours from a query point 
	 * @param query  Query point
	 * @param kMax  Value of k
	 * @return  List of at most k items that are nearest to the query point 
	 */
	public List<T> queryKNN(double[] query, int kMax);
	
	/**
	 * Find all items within a given distance of a query point
	 * @param query  Query point
	 * @param dist  Query range
	 * @return  Iterator of items that are within the given distance
	 */
	public Iterator<T> queryRange(double[] query, double dist);	
	
	/**
	 * Return a spatial index with indexed items mapped by a mapping function
	 * @param mapper  Mapping function
	 * @return  Spatial index in mapped type
	 */
	public default <V> SpatialIndex<V> map(Function<T, V> mapper) {
		SpatialIndex<T> self = this;
		return new SpatialIndex<V>() {

			@Override
			public List<V> queryKNN(double[] query, int kMax) {
				List<T> result = self.queryKNN(query, kMax);
				return new AbstractList<V>() {

					@Override
					public V get(int index) {
						return mapper.apply(result.get(index));
					}

					@Override
					public int size() {
						return result.size();
					}
					
				};
			}

			@Override
			public Iterator<V> queryRange(double[] query, double dist) {
				Iterator<T> iter = self.queryRange(query, dist);
				return new Iterator<V>() {

					@Override
					public boolean hasNext() {
						return iter.hasNext();
					}

					@Override
					public V next() {
						return mapper.apply(iter.next());
					}
					
				};
			}
			
		};
	}

}
