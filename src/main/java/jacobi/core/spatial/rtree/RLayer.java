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

/**
 * Data object for a serialized siblings layer in a R-Tree.
 * 
 * <p>A layer consists of a span array and a bounds array. The span array is the sequence
 * {s[0], m[0], s[1], m[1] ...} which s[i] is the starting index of the i-th node in the underlying
 * layer, and m[i] is the number of children contains in the i-th node. The bounds array is
 * the sequence of axis-aligned bounded boxes for each node, with minimum and maximum for each
 * dimension.</p>
 * 
 * @author Y.K Chan
 *
 */
public class RLayer {
	
	/**
	 * Construct a layer by combining nodes in an another layer
	 * @param cover  Array of number of nodes in each groups
	 * @param layer  Underlying layer
	 * @return  Combined layer
	 */
	public static RLayer coverOf(int[] cover, RLayer layer){
		int width = 0;
		for(int i = 0; i < cover.length; i++){
			if(cover[i] < 1){
				throw new IllegalArgumentException("Invalid cover #" + i + ": " + cover[i]);
			}
			
			width += cover[i];
		}
		
		if(width != layer.length()){
			throw new IllegalArgumentException("Grouping is not a cover of the layer");
		}
		
		int[] spans = new int[2 * cover.length];
		int aabbLen = 2 * layer.dim();
		double[] bounds = new double[aabbLen * cover.length];
		
		int index = 0;
		for(int i = 0; i < cover.length; i++){
			spans[2 * i] = index;
			spans[2 * i + 1] = cover[i];
			
			double[] aabb = layer.mbbOf(index, cover[i]);
			System.arraycopy(aabb, 0, bounds, i * aabbLen, aabbLen);
			
			index += cover[i];
		}
		
		return new RLayer(spans, bounds);
	}
	
	/**
	 * Construct a layer by combining spatial vectors
	 * @param cover  Array of number of nodes in each groups
	 * @param vectors  List of vectors
	 * @return  R-Layer
	 */
	public static RLayer coverOf(int[] cover, List<double[]> vectors) {
		if(vectors.isEmpty()){
			return new RLayer(cover, new double[0]);
		}
		
		int numDim = vectors.get(0).length;
		int[] spans = new int[2 * cover.length];
		double[] bounds = new double[2 * numDim * cover.length];
		
		int begin = 0;
		for(int i = 0; i < cover.length; i++){
			if(cover[i] < 1){
				throw new IllegalArgumentException("Invalid span " + cover[i] + " at #" + i);
			}
			
			double[] mbb = RLayer.mbbOf(vectors.subList(begin, begin + cover[i]));
			if(mbb.length != 2 * numDim){
				throw new IllegalArgumentException("Dimension mismatch");
			}
			
			System.arraycopy(mbb, 0, bounds, i * mbb.length, mbb.length);
			
			spans[2 * i] = begin;
			spans[2 * i + 1] = cover[i];
			
			begin += cover[i];
		}
		return new RLayer(spans, bounds);
	}
	
	/**
	 * Span array
	 */
	public final int[] spans;
	
	/**
	 * Bound array.
	 */
	public final double[] bounds;

	/**
	 * Constructor
	 * @param spans  Array of spans
	 * @param bounds  Array of bounds
	 */
	protected RLayer(int[] spans, double[] bounds) {
		this.spans = spans;
		this.bounds = bounds;
	}
	
	/**
	 * Get the number of dimensions of the axis-aligned bounded boxes
	 * @return  Number of dimensions
	 */
	public int dim() {
		return this.bounds.length / this.spans.length;
	}
	
	/**
	 * Get the number of nodes of this layer
	 * @return  Number of nodes
	 */
	public int length() {
		return this.spans.length / 2;
	}
	
	/**
	 * Compute the minimum axis-aligned bounded box of a range of aabbs.
	 * @param layer  R-Layer
	 * @param index  Starting index of aabb
	 * @param span  Number of aabb to be included
	 * @return  Mbb of the range of aabbs
	 */
	public double[] mbbOf(int index, int span) {
		int mbbLen = 2 * this.dim();
		double[] mbb = Arrays.copyOfRange(this.bounds, index * mbbLen, (index + 1) * mbbLen);
		
		for(int i = 1; i < span; i++){
			int begin = (index + i) * mbbLen;
			for(int j = 0; j < mbbLen; j += 2){
				if(mbb[j] > this.bounds[begin + j]){
					mbb[j] = this.bounds[begin + j];
				}
				
				if(mbb[j + 1] < this.bounds[begin + j + 1]){
					mbb[j + 1] = this.bounds[begin + j + 1];
				}
			}
		}
		return mbb;
	}
	
	/**
	 * Compute the minimum axis-aligned bounded box of a list of vectors.
	 * @param vectors  List of vectors
	 * @return  Mbb of a list of vectors
	 */
	private static double[] mbbOf(List<double[]> vectors) {
		double[] first = vectors.get(0);
		
		int numDim = first.length;
		double[] mbb = new double[2 * numDim]; 
		
		for(int i = 0; i < mbb.length; i++){
			mbb[i] = first[i / 2];
		}
		
		for(int i = 1; i < vectors.size(); i++){
			double[] vector = vectors.get(i);
			if(vector.length != numDim){
				throw new IllegalArgumentException("Dimension mismatch.");
			}
			
			for(int j = 0; j < vector.length; j++){
				double x = vector[j];
				if(mbb[2 * j] > x){
					mbb[2 * j] = x;
				}
				
				if(mbb[2 * j + 1] < x){
					mbb[2 * j + 1] = x;
				}
			}
		}
		return mbb;
	}
}
