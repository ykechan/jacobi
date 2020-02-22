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

/**
 * Distance function used in R-Tree.
 * 
 * @author Y.K. Chan
 * @param <T>  Type of spatial objects
 */
public interface RDistance<T> {
	
	/**
	 * Test if the distance between a query point and a target is smaller than a limit 
	 * @param target  Target spatial object
	 * @param p  Query point
	 * @param dist  Limit of distance
	 * @return  True if the distance between a query point and a target is smaller than a limit, 
	 * 			false otherwise
	 */
	public default boolean isCloser(T target, double[] qp, double dist) {
		return this.between(target, qp) <= dist;
	}
	
	/**
	 * Get the distance between a query point and a target
	 * @param target  Target spatial object
	 * @param p  Query point
	 * @return  Distance betwen a query point and a target
	 */
	public double between(T target, double[] qp);

}
