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
package jacobi.core.classifier.ensemble;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.DoubleSupplier;
import java.util.function.IntUnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import jacobi.api.Matrix;
import jacobi.api.classifier.Classifier;
import jacobi.api.classifier.ClassifierLearner;
import jacobi.api.classifier.Column;
import jacobi.api.classifier.DataTable;
import jacobi.api.classifier.Instance;
import jacobi.api.classifier.ensemble.BaggingParams;
import jacobi.core.classifier.cart.ArraySequence;
import jacobi.core.classifier.cart.Sequence;
import jacobi.core.impl.ImmutableMatrix;
import jacobi.core.util.Weighted;

/**
 * Learn a classifer by combining multiple classifiers and select the majority of result.
 * 
 * @author Y.K. Chan
 *
 * @param <T>  Type of outcome
 * @param <C>  Type of inner classifier model
 * @param <P>  Type of parameters to learn the inner classifier model
 */
public class BaggingLearner<T, C extends Classifier<T>, P> 
	implements ClassifierLearner<T, AggregatedClassifier<T, C>, BaggingParams<P>> {
	
	/**
	 * Constructor.
	 * @param learner  Learner of inner classifiers
	 * @param rand  Random function
	 */
	protected BaggingLearner(ClassifierLearner<T, C, P> learner, DoubleSupplier rand) {
		this.learner = learner;
		this.rand = rand;
	}

	@Override
	public AggregatedClassifier<T, C> learn(DataTable<T> dataTab, BaggingParams<P> params) {
		
		List<C> models = new ArrayList<>();		
		int size = (int) Math.ceil(params.samplingRate * dataTab.size());
		
		for(int i = 0; i < params.stoppingLimit; i++) {
			DataTable<T> subData = this.subset(dataTab, 
				this.subspace(dataTab.getColumns(), params.dimSpan), 
				this.randomSample(n -> (int) (n * this.rand.getAsDouble()), size)
			);
			
			C model = this.learner.learn(subData, params.subParams);
			models.add(model);
		}
		
		return new AggregatedClassifier<>(models
			.stream()
			.map(c -> new Weighted<>(c, 1.0))
			.collect(Collectors.toList())
		);
	}
	
	protected double delta(DataTable<T> dataTab, List<C> classifiers, int lag) {
		Matrix matrix = dataTab.getMatrix();
		
		double change = 0.0;
		for(int i = 0; i < matrix.getRowCount(); i++){
			Map<T, Integer> before = new HashMap<>();
			Map<T, Integer> after = null;
			
			double[] features = matrix.getRow(i);			
			for(int j = 0; j < classifiers.size(); j++){				
				T ans = classifiers.get(j).apply(features);
				before.put(ans, before.getOrDefault(ans, 0) + 1);
				
				if(j + lag > classifiers.size()){
					after.put(ans, after.getOrDefault(ans, 0) + 1);
				}
			}
		}
		return change;
	}	
	
	/**
	 * Create a view of the subset of data given a set of data.
	 * @param dataTab  Set of data
	 * @param columns  Sub-List of feature columns
	 * @param samples  Indices of sampled instances
	 * @return  Data Table of the subset
	 */
	protected DataTable<T> subset(DataTable<T> dataTab, List<Column<?>> columns, Sequence samples) {
		Matrix subMat = this.subMatrix(dataTab.getMatrix(), samples);
		return new DataTable<T>() {

			@Override
			public List<Column<?>> getColumns() {
				return columns;
			}

			@Override
			public Column<T> getOutcomeColumn() {
				return dataTab.getOutcomeColumn();
			}

			@Override
			public Matrix getMatrix() {
				return subMat;
			}

			@Override
			public List<Instance> getInstances(Column<?> column) {
				return samples.apply(dataTab.getInstances(column));
			}
			
		};
	}
	
	/**
	 * A matrix with only the sampled rows of the original matrix
	 * @param matrix  Input matrix
	 * @param samples  Index of sampled rows
	 * @return  Matrix with only the sampled rows
	 */
	protected Matrix subMatrix(Matrix matrix, Sequence samples) {
		return new ImmutableMatrix() {

			@Override
			public int getRowCount() {
				return samples.length();
			}

			@Override
			public int getColCount() {
				return matrix.getColCount();
			}

			@Override
			public double[] getRow(int index) {
				return matrix.getRow(samples.indexAt(index));
			}
			
		};
	}
	
	/**
	 * Random sampling of feature columns without replacement
	 * @param columns  List of all feature columns
	 * @param rate  Probability of select a feature column
	 * @return  Subset of the feature column 
	 */
	protected List<Column<?>> subspace(List<Column<?>> columns, double rate) {
		if(rate >= 1.0) {
			return columns;
		}
		
		List<Column<?>> subfeats = new ArrayList<>();
		for(Column<?> col : columns) {
			if(this.rand.getAsDouble() > rate) {
				continue;
			}
			
			subfeats.add(col);
		}
		return subfeats;
	}
	
	/**
	 * Random sampling of sequence indices with replacement
	 * @param randFn  Sampling function
	 * @param len  Number of the samples
	 * @return  Sampling of sequence indices with replacement
	 */
	protected Sequence randomSample(IntUnaryOperator randFn, int len) {	
		return new ArraySequence(IntStream.range(0, len).map(randFn).toArray(), 0, len);
	}
	
	private ClassifierLearner<T, C, P> learner;
	private DoubleSupplier rand;
}
