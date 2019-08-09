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
package jacobi.core.classifier.cart;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.IntUnaryOperator;
import java.util.stream.Collectors;

import jacobi.core.classifier.cart.OneR.Merger;
import jacobi.core.classifier.cart.data.Column;
import jacobi.core.classifier.cart.data.DataTable;
import jacobi.core.classifier.cart.data.Sequence;
import jacobi.core.classifier.cart.node.DecisionNode;
import jacobi.core.util.Weighted;

/**
 * Implementation of the Iterative Dichotomizer (ID3) algorithm.
 * 
 * <p>ID3 is a greedy algorithm that picks the best feature to split, and apply recursively 
 * on the subsets of data until the data is pure, i.e. all instances are with a single outcome,
 * or features are exhausted. </p>
 * 
 * <p>This implementation repeated invoked 1-R and inspects the result. Numeric features
 * are not supported and ignored.</p>
 * 
 * @author Y.K. Chan
 *
 */
public class Id3 implements Rule {
	
	/**
	 * Constructor
	 * @param oneR  1-R implementation
	 * @param beforeSplit  Listener on splitting dataset
	 */
	public Id3(OneR oneR, BiConsumer<Sequence, IntUnaryOperator> beforeSplit) {
		this.oneR = oneR;
		this.beforeSplit = beforeSplit;
	}

	@Override
	public <T> Weighted<DecisionNode<T>> make(DataTable<T> dataTable, Set<Column<?>> features, Sequence seq) {
		return this.make(new DataView<>(
			dataTable,
			features.stream()
				.filter(c -> !c.isNumeric())
				.collect(Collectors.toSet()),
			seq
		));
	}
	
	/**
	 * Create decision branch on a subset of data
	 * @param view  Subset of data table
	 * @return  Decision branch with associated impurity
	 */
	protected <T> Weighted<DecisionNode<T>> make(DataView<T> view) {
		Weighted<DecisionNode<T>> result = this.oneR.make(view.dataTab, view.features, view.subseq);
		if(result.item.split() == null) {
			return result;
		}
		
		Set<Column<?>> subfeat = view.features.stream()
				.filter(f -> !f.equals(result.item.split()))
				.collect(Collectors.toSet());
				
		List<DataView<T>> views = this.split(view, subfeat, result.item);
		Merger<T> merger = this.oneR.mergeFunc(result.item);
		
		List<DecisionNode<T>> nodes = new ArrayList<>();
		
		double dot = 0.0;
		double norm = 0.0;
		
		for(DataView<T> subView : views) {
			Weighted<DecisionNode<T>> subNode = this.make(subView);
			double partWeight = this.totalWeight(view.dataTab, view.subseq);
			dot += partWeight * subNode.weight;
			norm += partWeight;
			nodes.add(subNode.item);
		}
		
		return new Weighted<>(merger.apply(nodes), norm == 0.0 ? 0.0 : dot / norm);
	}
	
	/**
	 * Compute the total weight of a subset of data
	 * @param dataTab  Full data
	 * @param seq  Subset sequence of data
	 * @return  Sum of weights of the subset
	 */
	protected double totalWeight(DataTable<?> dataTab, Sequence seq) {
		
		return seq.apply(dataTab.getInstances(dataTab.getOutcomeColumn()))
			.stream()
			.mapToDouble(i -> i.weight)
			.sum();
	}
	
	/**
	 * Split the subset of data by a decision node
	 * @param view  Subset of data
	 * @param subfeat  Set of features
	 * @param node  Decision node
	 * @return  Split list of subset
	 */
	protected <T> List<DataView<T>> split(DataView<T> view, 
			Set<Column<?>> subfeat, 
			DecisionNode<T> node) {
		IntUnaryOperator splitFn = this.oneR.splitFunc(view.dataTab, node);
		this.beforeSplit.accept(view.subseq, splitFn);
		
		return view.subseq.groupBy(splitFn)
			.stream()
			.map(s -> new DataView<>(view.dataTab, subfeat, s))
			.collect(Collectors.toList());
	}
	
	private OneR oneR;
	private BiConsumer<Sequence, IntUnaryOperator> beforeSplit;
	
	/**
	 * Data object representing a subset of data table
	 * 
	 * @author Y.K. Chan
	 * @param <T>  Type of outcome
	 */
	protected static class DataView<T> {
		
		/**
		 * Data table
		 */
		public final DataTable<T> dataTab;
		
		/**
		 * Set of feature to consider
		 */
		public final Set<Column<?>> features;
		
		/**
		 * Sub-Sequence of instances
		 */
		public final Sequence subseq;

		public DataView(DataTable<T> dataTab, Set<Column<?>> features, Sequence subseq) {
			this.dataTab = dataTab;
			this.features = features;
			this.subseq = subseq;
		}
		
	}	
	
	protected static final Rule NO_RULE = new Rule() {

		@Override
		public <T> Weighted<DecisionNode<T>> make(DataTable<T> dataTab, 
				Set<Column<?>> features, 
				Sequence seq) {
			
			return new Weighted<>(null, Double.MAX_VALUE);
		}
		
	};	
	
}
