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
package jacobi.api.ext;

import jacobi.api.annotations.Facade;
import jacobi.api.annotations.Implementation;
import jacobi.api.annotations.Pure;
import jacobi.api.spatial.SpatialIndex;
import jacobi.api.spatial.SpatialIndexBuilder;

/**
 * Build spatial index on a matrix acting act a collection or row vectors as spatial objects
 * 
 * @author Y.K. Chan
 */
@Pure
@Facade
public interface Spatial {
	
	/**
	 * Create a builder of spatial index. Use build to directly obtain a spatial index
	 * without specifying any settings. 
	 * @return  Builder of spatial index
	 */
	@Implementation(SpatialIndexBuilder.Factory.class)
	public SpatialIndexBuilder builder();
	
	/**
	 * Build a spatial index with default settings.
	 * @return  Spatial index
	 */
	@Implementation(SpatialIndexBuilder.Default.class)
	public SpatialIndex<Integer> build();

}
