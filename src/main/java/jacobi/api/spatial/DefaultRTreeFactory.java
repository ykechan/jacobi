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

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.DoubleSupplier;
import java.util.function.Function;

import jacobi.api.Matrix;
import jacobi.api.annotations.Pure;
import jacobi.core.spatial.rtree.RCoverHeuristicPacker;
import jacobi.core.spatial.rtree.RDistHeuristicPacker;
import jacobi.core.spatial.rtree.RInlineTreeFactory;
import jacobi.core.spatial.rtree.RLayer;
import jacobi.core.spatial.sort.DefaultSpatialSort;
import jacobi.core.spatial.sort.SpatialSort;

/**
 * Factory class for creating spatial index.
 * 
 * @author Y.K. Chan
 */
@Pure
public class DefaultRTreeFactory {

	/**
	 * Default minimum number of vectors in leaf nodes
	 */
	public static final int DEFAULT_LEAF_MIN = 16;

	/**
	 * Default maximum number of vectors in leaf nodes
	 */
	public static final int DEFAULT_LEAF_MAX = 24;

	/**
	 * Default minimum number of nodes in internal nodes
	 */
	public static final int DEFAULT_NODE_MIN = 8;

	/**
	 * Default maximum number of nodes in internal nodes
	 */
	public static final int DEFAULT_NODE_MAX = 16;

	/**
	 * Default proportion of variance that is considered significant
	 */
	public static final double DEFAULT_R_SQUARE = 0.75;
	
	/**
	 * Default sampling size for computing statistics
	 */
	public static final int DEFAULT_SAMPLE_SIZE = 16;
	
	/**
	 * Default flag for serialize the matrix data to a single array for better access locality
	 */
	public static final boolean DEFAULT_INLINE_DATA = true;
	
	/**
	 * Constructor
	 * @param  data  Spatial data as row vectors in a matrix
	 */
	public DefaultRTreeFactory(Matrix data) {
		this.leafMin = DEFAULT_LEAF_MIN;
		this.leafMax = DEFAULT_LEAF_MAX;
		this.nodeMin = DEFAULT_NODE_MIN;
		this.nodeMax = DEFAULT_NODE_MAX;
		this.rSquare = DEFAULT_R_SQUARE;
		this.sampleSize = DEFAULT_SAMPLE_SIZE;
		this.inline = DEFAULT_INLINE_DATA;
		this.randFn = () -> ThreadLocalRandom.current().nextDouble();
		this.data = data;
	}

	/**
	 * Set the minimum number of vectors in leaf nodes
	 * @param leafMin  minimum number of vectors in leaf nodes
	 * @return this
	 */
	public DefaultRTreeFactory setLeafMin(int leafMin) {
		this.leafMin = leafMin;
		return this;
	}

	/**
	 * Set the maximum number of vectors in leaf nodes
	 * @param leafMax  maximum number of vectors in leaf nodes
	 * @return this
	 */
	public DefaultRTreeFactory setLeafMax(int leafMax) {
		this.leafMax = leafMax;
		return this;
	}

	/**
	 * Set the minimum number of nodes in internal nodes
	 * @param nodeMin  minimum number of nodes in internal nodes
	 * @return this
	 */
	public DefaultRTreeFactory setNodeMin(int nodeMin) {
		this.nodeMin = nodeMin;
		return this;
	}

	/**
	 * Set the maximum number of nodes in internal nodes
	 * @param nodeMax  maximum number of nodes in internal nodes
	 * @return this
	 */
	public DefaultRTreeFactory setNodeMax(int nodeMax) {
		this.nodeMax = nodeMax;
		return this;
	}

	/**
	 * Set the proportion of variance that is considered significant
	 * @param rSquare  proportion of variance that is considered significant
	 * @return this
	 */
	public DefaultRTreeFactory setRSquare(double rSquare) {
		this.rSquare = rSquare;
		return this;
	}

	/**
	 * Set random function that gives a random value in [0, 1)
	 * @param randFn  Random function
	 * @return this
	 */
	public DefaultRTreeFactory setRandFn(DoubleSupplier randFn) {
		this.randFn = randFn;
		return this;
	}
	
	/**
	 * Set the spatial sorting implementation
	 * @param spatialSort  Spatial sort
	 * @return this
	 */
	public DefaultRTreeFactory setSpatialSort(SpatialSort spatialSort) {
		this.spatialSort = spatialSort;
		return this;
	}

	/**
	 * Create spatial index on a set of vectors
	 * 
	 * @param vectors  Input vectors
	 * @return Spatial index of given input vectors
	 */
	public SpatialIndex<Integer> build() {
		Function<List<double[]>, RLayer> sPacker = new RDistHeuristicPacker(this.leafMin, this.leafMax);
		Function<RLayer, RLayer> rPacker = new RCoverHeuristicPacker(this.nodeMin, this.nodeMax);
		SpatialSort sSort = this.spatialSort == null
			? new DefaultSpatialSort(this.randFn, this.sampleSize, this.rSquare)
			: this.spatialSort;
		
		RInlineTreeFactory rFactory = new RInlineTreeFactory(sSort, rPacker, sPacker, inline);
		return rFactory.create(this.data);
	}

	private int leafMin, leafMax;
	private int nodeMin, nodeMax;
	private double rSquare;
	private int sampleSize;
	private boolean inline;
	private DoubleSupplier randFn;
	private SpatialSort spatialSort;
	private Matrix data;
}
