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
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.function.Function;

import jacobi.api.Matrix;
import jacobi.api.spatial.SpatialIndex;
import jacobi.core.spatial.sort.SpatialSort;

/**
 * Factory class for building an inline R-Tree.
 * 
 * @author Y.K. Chan
 *
 */
public class RInlineTreeFactory {
	
	private static final int DEFAULT_MAX_DEPTH = 64;

	/**
	 * Constructor
	 * @param sortingFn  Sorting function on spatial data
	 * @param rPacker  Packing function on R-Nodes
	 * @param sPacker  Packing function on spatial data
	 */
	public RInlineTreeFactory(SpatialSort sortingFn, 
			Function<RLayer, RLayer> rPacker,
			Function<List<double[]>, RLayer> sPacker) {
		this.sortingFn = sortingFn;
		this.rPacker = rPacker;
		this.sPacker = sPacker;
	}

	/**
	 * Create spatial index on row vectors of a matrix
	 * @param matrix  Input data matrix
	 * @return  Spatial index
	 */
	public SpatialIndex<Integer> create(Matrix matrix) {
		List<RLayer> hierarchy = this.buildHierarchy(matrix, true);
		
		int depth = hierarchy.size() - 1;
		RLayer base = hierarchy.get(depth);
		
		List<RLayer> index = hierarchy.subList(0, depth);
		return null;
	}
	
	/**
	 * Build the hierarchy of the R-Tree. 
	 * @param matrix  Input data matrix
	 * @return  List of layers from top to bottom
	 */
	protected List<RLayer> buildHierarchy(Matrix matrix, boolean inline) {
		RLayer base = this.serialize(matrix, inline);
		
		Deque<RLayer> stack = new ArrayDeque<>();
		stack.push(base);
		
		RLayer nodes = this.sPacker.apply(this.toList(matrix, base.spans));
		stack.push(nodes);
		
		int max = DEFAULT_MAX_DEPTH;
		while(stack.size() < max){
			RLayer children = stack.peek();
			RLayer parent = this.rPacker.apply(children);
			
			if(parent.length() < 2){
				break;
			}
			
			if(parent.length() >= children.length()){
				throw new UnsupportedOperationException();
			}
			stack.push(parent);
		}
		
		RLayer[] layers = new RLayer[stack.size()];
		int k = 0;
		while(!stack.isEmpty()){
			layers[k++] = stack.pop();
		}
		return Arrays.asList(layers);
	}
	
	/**
	 * Construct the bottom layer by sorting and inlining data if specified. 
	 * Inlining refers to copy all vectors into a single array for better locality.
	 * @param matrix  Input data matrix
	 * @param inline  Flag to inline the data or not
	 * @return  A R-Layer consists of the sequence of the data, and inlined data if inline flag is true
	 */
	protected RLayer serialize(Matrix matrix, boolean inline) {
		int[] seq = this.sortingFn.sort(this.toList(matrix, null));
		
		if(!inline){
			return new RLayer(seq, new double[0]);
		}
		
		int dim = matrix.getColCount();
		int len = matrix.getRowCount() * matrix.getColCount();
		
		double[] serial = new double[len];
		for(int i = 0; i < seq.length; i++){
			double[] vector = matrix.getRow(seq[i]);
			System.arraycopy(vector, 0, serial, i * dim, dim);
		}
		return new RLayer(seq, serial);
	}
	
	/**
	 * Wrap a matrix into a list of arrays.
	 * @param matrix  Input data matrix
	 * @param order  Sequence order, or null for natural order
	 * @return  List of arrays
	 */
	protected List<double[]> toList(Matrix matrix, int[] order) {		
		return new AbstractList<double[]>(){

			@Override
			public double[] get(int index) {
				return matrix.getRow(order == null ? index : order[index]);
			}

			@Override
			public int size() {
				return matrix.getRowCount();
			}
			
		};
	}

	private SpatialSort sortingFn;
	private Function<RLayer, RLayer> rPacker;
	private Function<List<double[]>, RLayer> sPacker;
}
