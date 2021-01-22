/*
 * The MIT License
 *
 * Copyright 2021 Y.K. Chan
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
package jacobi.core.clustering;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import jacobi.api.Matrix;
import jacobi.api.unsupervised.Segregation;

/**
 * Interface of describing a cluster and defining the metric distance between a cluster and a vector.
 * 
 * <p></p>
 * 
 * @author Y.K. Chan
 * @param <T>  Type of cluster descriptor
 */
public interface ClusterMetric<T> {
	/**
	 * Expectation value of the cluster descriptors given the cluster vectors with index sequences
	 * @param matrix  Input matrix
	 * @param seqs  Index sequences for each clusters
	 * @return  Cluster descriptors
	 */
	public default List<T> expects(Matrix matrix, List<int[]> seqs) {
		return Segregation.getInstance().compute(matrix, seqs)
			.stream().sequential()
			.map(this::expects)
			.collect(Collectors.toList());
	}
	
	/**
	 * Expectation value of the cluster descriptor given the cluster vectors
	 * @param matrix  Input matrix
	 * @return  Cluster descriptor
	 */
	public T expects(Matrix matrix);
	
	/**
	 * Compute the distance between a cluster and a vector
	 * @param cluster  Input cluster descriptor
	 * @param vector  Input vector
	 * @return  Distance between a cluster and a vector
	 */
	public double distanceBetween(T cluster, double[] vector);

}
