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
import java.util.Optional;

/**
 * An internal node in R-Tree.
 * 
 * @author Y.K. Chan
 * @param <T>  Type of indexed spatial object
 */
public class RNode<T> implements RObject<T> {
	
	/**
	 * Factory method.
	 * @param nodes  List of child nodes
	 * @return  An internal node that contains the given child nodes
	 */
	public static <T> RNode<T> of(List<RObject<T>> nodes) {
		if(nodes == null || nodes.isEmpty()){
			throw new IllegalArgumentException("A R-Node cannot be empty.");
		}
		
		Aabb[] aabbs = nodes.subList(1, nodes.size()).stream()
			.map(RObject::minBoundBox)
			.toArray(n -> new Aabb[n]);
		
		return new RNode<T>(
			nodes.get(0).minBoundBox().join(Arrays.asList(aabbs)), 
			nodes
		);
	}
	
	/**
	 * Constructor.
	 * @param mbb  Minimum bounding box
	 * @param nodeList  List of child nodes
	 */
	protected RNode(Aabb mbb, List<RObject<T>> nodeList) {
		this.mbb = mbb;
		this.nodeList = nodeList;
	}

	@Override
	public Optional<T> get() {
		return Optional.empty();
	}

	@Override
	public Aabb minBoundBox() {
		return this.mbb;
	}

	@Override
	public List<RObject<T>> nodeList() {
		return this.nodeList;
	}
	
	private Aabb mbb;
	private List<RObject<T>> nodeList;
}
