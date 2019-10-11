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
 */package jacobi.api.graph;

import java.util.Optional;

import jacobi.api.Matrix;
import jacobi.api.annotations.Facade;
import jacobi.api.annotations.Pure;
import jacobi.core.facade.FacadeProxy;
import jacobi.core.graph.AdjMatrix;

/**
 * Facade interface for Graph algorithms.
 * 
 * @author Y.K. Chan
 */
@Facade(AdjList.class)
public interface Adjacency {
	
	/**
	 * Topological sort
	 * @return  Topological ordering, or empty if any cycle is found
	 */
	public Optional<int[]> topsort();
	
	/**
	 * Initialize a general matrix into adjacency matrix
	 * 
	 * @author Y.K. Chan
	 */
	@Pure
	public static class Init {
		
		/**
		 * Initialize adjacency matrix for Graph algorithms
		 * @param adjMat  Adjacency matrix
		 * @return  Facade for Graph algorithms
		 */
		public Adjacency init(Matrix adjMat) {
			return FacadeProxy.of(Adjacency.class, AdjMatrix.of(adjMat));
		}
		
	}

}
