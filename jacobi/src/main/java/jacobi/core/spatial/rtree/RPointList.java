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

import java.util.AbstractList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.IntFunction;

/**
 * A list of r-leaf indexing an integer that is a key of a spatial point.
 * 
 * <p></p>
 * 
 * @author Y.K. Chan
 *
 */
public class RPointList extends AbstractList<RObject<Integer>> {		

	/**
	 * Constructor.
	 * @param mapper  Mapper function given an integer key to the spatial point
	 * @param keys  Array of keys to spatial points  
	 * @param begin  Begin index of interest
	 * @param end  End index of interest
	 */
	public RPointList(IntFunction<double[]> mapper, int[] keys, int begin, int end) {
		this.keys = keys;
		this.begin = begin;
		this.end = end;
		this.mapper = mapper;
	}

	@Override
	public RObject<Integer> get(int index) {
		int idx = this.keys[index - this.begin];
		double[] point = this.mapper.apply(idx);
		Aabb aabb = Aabb.wrap(point);
		return new RObject<Integer>() {

			@Override
			public Optional<Integer> get() {
				return Optional.of(idx);
			}

			@Override
			public Aabb minBoundBox() {
				return aabb;
			}

			@Override
			public List<RObject<Integer>> nodes() {
				return Collections.emptyList();
			}
			
		};
	}

	@Override
	public int size() {
		return this.end - this.begin;
	}

	private IntFunction<double[]> mapper;
	private int[] keys;
	private int begin, end;	
}
