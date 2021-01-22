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
package jacobi.api.unsupervised;

import java.util.AbstractList;
import java.util.Collections;
import java.util.List;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.api.annotations.Pure;
import jacobi.core.impl.ImmutableMatrix;
import jacobi.core.util.Throw;

/**
 * Segregate a matrix into different clusters.
 * 
 * @author Y.K. Chan
 */
@Pure
public class Segregation {
	
	/**
	 * Get the default singleton instance of this class
	 * @return  Default instance
	 */
	public static Segregation getInstance() {
		return INSTANCE;
	}
	
	/**
	 * Given the row indices of members of each clusters, obtain the clusters as collection of 
	 * member vectors.  
	 * @param input  Input matrix
	 * @param seqs  Row indices of members of each clusters
	 * @return  Immutable list of clusters as matrices
	 */
	public List<Matrix> compute(Matrix input, List<int[]> seqs) {
		Throw.when()
			.isNull(() -> input, () -> "No matrix to segregate")
			.isNull(() -> seqs, () -> "No index sequences");
		
		if(seqs.isEmpty()){
			return Collections.emptyList();
		}
		
		return new AbstractList<Matrix>() {

			@Override
			public Matrix get(int index) {
				int[] seq = seqs.get(index);
				return toMatrix(input, seq, 0, seq.length);
			}

			@Override
			public int size() {
				return seqs.size();
			}
			
		};
	}
	
	/**
	 * Wrap a sub-matrix into a matrix given the sequence of row indices
	 * @param input  Input matrix
	 * @param seq  Sequence of row indices
	 * @return  Sub-matrix
	 */
	public Matrix toMatrix(Matrix input, int[] seq, int begin, int end) {
		int len = end - begin;
		if(len < 1){
			return Matrices.zeros(0);
		}
		
		int numCols = input.getColCount();
		return new ImmutableMatrix(){

			@Override
			public int getRowCount() {
				return len;
			}

			@Override
			public int getColCount() {
				return numCols;
			}

			@Override
			public double[] getRow(int index) {
				int i = seq[begin + index];
				return input.getRow(i);
			}
			
		};
	}

	private static final Segregation INSTANCE = new Segregation();
}
