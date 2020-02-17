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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import jacobi.api.Matrix;
import jacobi.core.spatial.sort.SpatialSort;

/**
 * Factory for creating a static R-Tree.
 * 
 * @author Y.K. Chan
 *
 */
public class RTreeFactory {

	public RTreeFactory(SpatialSort sorter, RPacker packer) {
		this.sorter = sorter;
		this.packer = packer;
	}
		
	protected RNode<Integer> createNode(Matrix data) {
		List<double[]> list = this.toList(data);
		int[] order = this.sorter.sort(list);
		
		List<RNode<Integer>> nodes = this.bottom(list, order, 3, 12);
		while(nodes.size() > 1){
			nodes = this.collapse(nodes, 3, 4);
		}
		return nodes.get(0);
	}
	
	protected List<RNode<Integer>> collapse(List<RNode<Integer>> nodes, int minItems, int maxItems) {
		int[] groups = this.packer.pack(nodes, minItems, maxItems);
		
		List<RNode<Integer>> parents = new ArrayList<>(groups.length);
		
		int begin = 0;
		for(int i = 0; i < groups.length; i++) {
			List<RObject<Integer>> group = new ArrayList<>();
			group.addAll(nodes.subList(begin, begin + groups[i]));
			parents.add(RNode.of(group));
			begin += groups[i];
		}
		return parents;
	}
	
	protected List<RNode<Integer>> bottom(List<double[]> list, int[] order, int minItems, int maxItems) {
		int[] groups = this.packer.pack(this.orderBy(this.toLeaves(list), order), 
				minItems, maxItems);
		
		List<RNode<Integer>> nodes = new ArrayList<>(groups.length);
		
		int begin = 0;
		for(int i = 0; i < groups.length; i++) {
			List<RObject<Integer>> group = new RPointList(list::get, order, begin, begin + groups[i]);
			nodes.add(RNode.of(group));
			begin += groups[i];
		}
		return nodes;
	}
	
	protected List<RObject<?>> toLeaves(List<double[]> list) {
		return new AbstractList<RObject<?>>() {

			@Override
			public RObject<?> get(int index) {
				return new RObject<Boolean>() {

					@Override
					public Optional<Boolean> get() {
						return Optional.of(Boolean.TRUE);
					}

					@Override
					public Aabb minBoundBox() {
						return Aabb.wrap(list.get(index));
					}

					@Override
					public List<RObject<Boolean>> nodes() {
						return Collections.emptyList();
					}
					
				};
			}

			@Override
			public int size() {
				return list.size();
			}
			
		};
	}
	
	protected List<double[]> toList(Matrix data) {
		return new AbstractList<double[]>() {

			@Override
			public double[] get(int index) {
				return data.getRow(index);
			}

			@Override
			public int size() {
				return data.getRowCount();
			}
			
		};
	}
	
	protected <T> List<T> orderBy(List<T> list, int[] order) {
		return new AbstractList<T>() {

			@Override
			public T get(int index) {
				return list.get(order[index]);
			}

			@Override
			public int size() {
				return list.size();
			}
			
		};
	}
	
	private SpatialSort sorter;
	private RPacker packer;
}
