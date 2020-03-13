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

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * An spatial object indexed in a R-Tree.
 * 
 * <p>A r-object can be an internal node or leaf. An internal node contains
 * a list of r-objects under it, and a leaf contains no nodes and returns 
 * the spatial object indexed.</p>
 * 
 * <p>A r-object also supplies a minimum bounding box (mbb) representing
 * the largest extending area of the indexed object(s). If the node is a
 * leaf and the spatial object is a point, the mbb is degenerate.</p>
 * 
 * @author Y.K. Chan
 * @param <T>  Type of the spatial object indexed
 */
public interface RObject<T> extends Supplier<Optional<T>> {
	
	/**
	 * Get the minimum bounding box
	 * @return  A aabb that is the minimum bounding box
	 */
	public Aabb minBoundBox();
	
	/**
	 * Get the list of child nodes.
	 * @return  List of child nodes, or empty if this is a leaf
	 */
	public List<RObject<T>> nodeList();

}
