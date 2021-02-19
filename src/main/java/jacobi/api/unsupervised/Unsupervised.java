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

import java.util.List;

import jacobi.api.Matrix;
import jacobi.api.annotations.Facade;
import jacobi.api.annotations.Implementation;
import jacobi.core.clustering.Dbscan;
import jacobi.core.facade.FacadeProxy;

/**
 * Facade interface for unsupervised learning
 * 
 * @author Y.K. Chan
 */
@Facade
public interface Unsupervised {
	
	/**
	 * Proxy to extension of unsupervised learning
	 * 
	 * @author Y.K. Chan
	 *
	 */
	public static class Factory {
		
		/**
		 * Create proxy to unsupervised learning interface
		 * @param data  Input data
		 * @return  Unsupervised learning interface
		 */
		public Unsupervised proxy(Matrix data) {
			return FacadeProxy.of(Unsupervised.class, data);
		}
		
	}
	
	/**
	 * Given the row indices of members of each clusters, obtain the clusters as collection of 
	 * member vectors.
	 * @param seqs  Row indices of members of each clusters
	 * @return  Immutable list of clusters as matrices
	 */
	@Implementation(Segregation.class)
	public List<Matrix> segregate(List<int[]> seqs);
	
	/**
	 * K-means clustering
	 * @param k  Number of clusters
	 * @return  Row indices of members of each clusters
	 */
	@Implementation(KMeans.class)
	public List<int[]> kMeans(int k);
	
	/**
	 * K-means clustering that determines the optimal number of clusters
	 * @param k  Number of clusters
	 * @param kMin  Minimum number of clusters
	 * @param kMax  Maximum number of clusters
	 * @return  Row indices of members of each clusters
	 */
	@Implementation(KMeans.class)
	public List<int[]> kMeans(int kMin, int kMax);
	
	/**
	 * Clustering using Gaussian mixture model. If number of data is insufficient,
	 * the co-variance matrix is deflated to a diagonal matrix.
	 * @param k  Number of clusters
	 * @return  Row indices of members of each clusters
	 */
	@Implementation(GaussMixModel.class)
	public List<int[]> gmm(int k);
	
	
	/**
	 * Clustering using Gaussian mixture model.
	 * @param k  Number of clusters
	 * @param full  True for consider full co-variance matrix, deflate to diagonal matrix otherwise
	 * @return  Row indices of members of each clusters
	 */
	@Implementation(GaussMixModel.class)
	public List<int[]> gmm(int k, boolean full);
	
	/**
	 * Clustering using DBSCAN algorithm
	 * @param minPts  Minimum number of points in a cluster
	 * @param epsilon  Reaching distance of a cluster
	 * @return  Row indices of members of each clusters
	 */
	@Implementation(Dbscan.Proxy.class)
	public List<int[]> dbscan(int minPts, double epsilon);

	/**
	 * Configurer for clustering using the Mean-Shift algorithm.
	 * @return  Mean-shift clustering model
	 */
	@Implementation(MeanShifts.Proxy.class)
	public MeanShifts meanShift();
}
