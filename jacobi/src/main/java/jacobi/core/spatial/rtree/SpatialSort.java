/* 
 * The MIT License
 *
 * Copyright 2019 Y.K. Chan
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
import java.util.function.UnaryOperator;

/**
 * Common interface of sorting a list of spatial objects.
 * 
 * <p>A spatial object here is a point in high-dimensional space.</p>
 * 
 * <p>The ordering of the objects depends on the algorithm.</p>
 * 
 * @author Y.K. Chan
 *
 */
public interface SpatialSort extends UnaryOperator<List<double[]>> {
	
	@Override
	public default List<double[]> apply(List<double[]> vectors) {
		return Arrays.asList(this.apply(vectors.toArray(new double[0][]), 0, vectors.size()));
	}

	/**
	 * Apply sorting to an array of spatial objects. 
	 * The ordering of the input array maybe mutated.
	 * @param vectors  Input array
	 * @param begin  Begin index of interest
	 * @param end  End index of interest
	 * @return  An sorted array of spatial objects.
	 */
	public double[][] apply(double[][] vectors, int begin, int end);

}
