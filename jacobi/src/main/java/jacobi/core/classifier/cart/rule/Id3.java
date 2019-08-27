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
package jacobi.core.classifier.cart.rule;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.IntUnaryOperator;
import java.util.stream.Collectors;

import jacobi.api.classifier.Column;
import jacobi.api.classifier.DataTable;
import jacobi.api.classifier.cart.DecisionNode;
import jacobi.core.classifier.cart.Sequence;
import jacobi.core.classifier.cart.measure.Impurity;
import jacobi.core.classifier.cart.measure.NominalPartition;

/**
 * Implementation of the Iterative Dichotomizer (ID3) algorithm.
 * 
 * <p>ID3 is a greedy algorithm that picks the best feature to split, and apply recursively 
 * on the subsets of data until the data is pure, i.e. all instances are with a single outcome,
 * or features are exhausted. </p>
 * 
 * <p>This implementation repeated invoked 1-R and inspects the result.</p>
 * 
 * @author Y.K. Chan
 *
 */
public class Id3 implements Rule {

	/**
	 * Factory method of standard Id3 rule maker.
	 * @param impurity  Impurity measurement
	 * @return  Instance of Id3
	 */
	public static Id3 of(Impurity impurity) {
		return new Id3(
			new ZeroR(), 
			new OneR(NO_RULE, new NominalPartition(impurity)),
			(s, g) -> {}
		);
	}
	
	/**
	 * Constructor.
	 * @param zeroR Implementation of 0-R
	 * @param oneR  Implementation of 1-R
	 * @param beforeSplit  Listener on splitting the data set
	 */
	public Id3(Rule zeroR, OneR oneR, BiConsumer<Sequence, IntUnaryOperator> beforeSplit) {
		this.zeroR = zeroR;
		this.oneR = oneR;
		this.beforeSplit = beforeSplit;
	}

	@Override
	public <T> DecisionNode<T> make(DataTable<T> dataTab, Set<Column<?>> feats, Sequence seq) {
		
		DecisionNode<T> result = this.oneR.make(dataTab, feats, seq);
		if(result == null) {
			// no feature to split
			return this.zeroR.make(dataTab, feats, seq);
		}
		
		if(result.split() == null){
			// already is a leaf node
			return result;
		}
		
		Set<Column<?>> subfeat = feats.stream()
				.filter(f -> !f.equals(result.split()))
				.collect(Collectors.toSet());
		
		List<Sequence> subseqs = this.split(dataTab, seq, result);
		List<DecisionNode<T>> subNodes = this.make(dataTab, subfeat, subseqs);
		
		return this.oneR
				.mergeFunc(result)
				.apply(subNodes);
	}
	
	/**
	 * Learn decision nodes for a list of subset of the data
	 * @param dataTab  Input data table
	 * @param feats  Set of feature columns
	 * @param seqs  List of sub-sequences
	 * @return  List of decision node with impurity measurement
	 */
	protected <T> List<DecisionNode<T>> make(DataTable<T> dataTab, 
			Set<Column<?>> feats, 
			List<Sequence> seqs) {
		List<DecisionNode<T>> nodes = new ArrayList<>(seqs.size());
		
		for(Sequence seq : seqs) {
			nodes.add(this.make(dataTab, feats, seq));
		}
		
		return nodes;
	}
	
	/**
	 * Split the subset of data by a decision node
	 * @param view  Subset of data
	 * @param subfeat  Set of features
	 * @param node  Decision node
	 * @return  Split list of subset
	 */
	protected <T> List<Sequence> split(DataTable<T> dataTab,
			Sequence subseq,
			DecisionNode<T> node) {
		IntUnaryOperator splitFn = this.oneR.splitFunc(dataTab, node);
		this.beforeSplit.accept(subseq, splitFn);
		
		return subseq.groupBy(splitFn);
	}
	
	private Rule zeroR;
	private OneR oneR;
	private BiConsumer<Sequence, IntUnaryOperator> beforeSplit;	
	
	/**
	 * Rule that returns null as decision.
	 */
	protected static final Rule NO_RULE = new Rule() {

		@Override
		public <T> DecisionNode<T> make(DataTable<T> dataTab, 
				Set<Column<?>> features, 
				Sequence seq) {
			
			return null;
		}
		
	};	
	
}
