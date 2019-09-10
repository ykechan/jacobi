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

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;
import java.util.stream.Collectors;

import jacobi.api.Matrix;
import jacobi.api.classifier.Column;
import jacobi.api.classifier.DataTable;
import jacobi.api.classifier.Instance;
import jacobi.api.classifier.cart.DecisionNode;
import jacobi.core.classifier.cart.ArraySequence;
import jacobi.core.classifier.cart.Sequence;
import jacobi.core.classifier.cart.measure.Partition;
import jacobi.core.classifier.cart.node.BinaryNumericSplit;
import jacobi.core.classifier.cart.node.Decision;
import jacobi.core.classifier.cart.node.NominalSplit;
import jacobi.core.util.Weighted;

/**
 * Implementation of 1-R decision rule learning algorithm.
 * 
 * <p>1-R algorithm chooses the feature having minimum impurity of outcome distribution
 * after split, and decide for each distinct value of features its best guess according
 * to the instance.</p>
 * 
 * <p>Making the guess after feature split is delegated to a baseline implementation. 
 * If the baseline implementation is 0-R, this class would create a decision tree with
 * depth 1. Other baseline implementations can also be used.</p>
 * 
 * <p>If no feature is found, the class would fallback to it's baseline implementation.</p>
 * 
 * @author Y.K. Chan
 *
 */
public class OneR implements Rule {
	
	/**
	 * Constructor
	 * @param zeroR  Baseline implementation
	 * @param partition  Partition function
	 */
	public OneR(Rule zeroR, Partition partition) {
		this.zeroR = zeroR;
		this.partition = partition;
	}

	@Override
	public <T> DecisionNode<T> make(DataTable<T> dataTable, 
			Set<Column<?>> features, 
			Sequence seq) {
		Weighted<double[]> min = null;
		Column<?> target = null;
		
		for(Column<?> feat : features) {
			Weighted<double[]> split = this.partition.measure(dataTable, feat, seq);
			if(Double.isNaN(split.weight)) {
				return this.decidePure(dataTable, seq);
			}
			
			if(min == null || split.weight < min.weight) {
				min = split;
				target = feat;
			}
		}
		
		if(min == null) {
			return this.zeroR.make(dataTable, features, seq);
		}
		
		Set<Column<?>> subfeats = new TreeSet<>(features);
		subfeats.remove(target);
		
		List<? extends Sequence> subseq = seq.groupBy(this.splitFunc(dataTable, target, min.item));
		List<DecisionNode<T>> nodes = subseq.stream()
				.map(s -> this.zeroR.make(dataTable, subfeats, s))
				.collect(Collectors.toList());
		
		return this.<T>mergeFunc(target, min.item).apply(nodes);
	}
	
	/**
	 * Get the merging function according to a decision node. The decision node should be created
	 * by this implementation.
	 * @param node  Decision node
	 * @return  Merging function
	 */
	public <T> Merger<T> mergeFunc(DecisionNode<T> node) {
		if(node.split() == null){
			throw new IllegalArgumentException("Leaf node has no merge function");
		}
		
		if(node instanceof NominalSplit){
			return this.mergeFunc(node.split(), new double[0]);
		}
		
		if(node instanceof BinaryNumericSplit){
			return this.mergeFunc(node.split(), 
				new double[] {((BinaryNumericSplit<?>) node).getThreshold()}
			);
		}
		
		throw new UnsupportedOperationException("Un-identified decision node " + node);
	}
	
	/**
	 * Get the merging function according to a feature column and given split boundaries
	 * @param col  Feature column
	 * @param bounds  Boundaries of split
	 * @return  Merging function
	 */
	protected <T> Merger<T> mergeFunc(Column<?> col, double[] bounds) {
		this.validateColumnAndBounds(col, bounds);	
		
		switch(bounds.length) {
			case 0 :
				return ls -> new NominalSplit<>(col, null, ls);
				
			case 1 :
				return ls -> new BinaryNumericSplit<>(col, bounds[0], ls.get(0), ls.get(1));
				
			default :
				break;
		}
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Get the splitting function from a decision node created by this class
	 * @param dataTab  Data table
	 * @param node  Decision node created
	 * @return  Splitting function
	 */
	public IntUnaryOperator splitFunc(DataTable<?> dataTab, DecisionNode<?> node) {
		if(node instanceof NominalSplit){ 
			return this.splitFunc(dataTab, node.split(), new double[0]);
		}

		if(node instanceof BinaryNumericSplit){
			return this.splitFunc(dataTab, node.split(), 
				new double[] { ((BinaryNumericSplit<?>) node).getThreshold() }
			);
		}
		
		throw new UnsupportedOperationException("Unable to detect splitting function on " + node);
	}
	
	/**
	 * Get the splitting function from a target feature column and split info
	 * @param dataTab  Data table
	 * @param col  Feature column
	 * @param bounds Split info
	 * @return  Splitting function
	 */
	protected IntUnaryOperator splitFunc(DataTable<?> dataTab, Column<?> col, double[] bounds) {
		this.validateColumnAndBounds(col, bounds);
		
		switch(bounds.length) {
			case 0 : {
				List<Instance> instances = dataTab.getInstances(col);
				return i -> instances.get(i).feature;
			}
			case 1 : {
				Matrix mat = dataTab.getMatrix();
				return i -> mat.get(i, col.getIndex()) < bounds[0] ? 0 : 1;
			}
			default :
				break;
		}
		throw new UnsupportedOperationException();
	}	
	
	/**
	 * Validate column type and given boundaries, i.e. no boundaries for nominal column,
	 * and non-empty boundaries for numeric column
	 * @param col  Input column
	 * @param bounds  Boundaries
	 * @throws  IllegalArgumentException if no boundaries for numeric column, and 
	 *                                   non-empty boundaries for nominal column
	 */
	protected void validateColumnAndBounds(Column<?> col, double[] bounds) {		
		if(col.isNumeric() == (bounds.length > 0)){
			return;
		}
		throw new IllegalArgumentException(String.format(
			"Given %d boundaries for %s column #%d",
			bounds.length, col.isNumeric() ? "numeric" : "nominal", col.getIndex()
		));
	}
	
	/**
	 * Make decision on a pure data (sub)set, i.e. all instances having the same outcome.
	 * If no instance is found, decide on the first choice of outcome.
	 * @param dataTab  Input data set
	 * @param seq  Sequence of access
	 * @return  Decision leaf node
	 */
	protected <T> Decision<T> decidePure(DataTable<T> dataTab, Sequence seq) {
		
		return new Decision<>(dataTab.getOutcomeColumn().valueOf(
			seq.length() == 0
			? 0
			: dataTab.getInstances(dataTab.getOutcomeColumn())
				.get(seq.indexAt(0))
				.feature
		));
	}
	
	private Rule zeroR;
	private Partition partition;
	
	/**
	 * Short-hand for List&lt;DecisioNode&lt;T&gt;&gt; -&gt; DecisionNode&lt;T&gt; 
	 * 
	 * @author Y.K. Chan
	 * @param <T>  Type of outcome
	 */
	public interface Merger<T> extends Function<List<DecisionNode<T>>, DecisionNode<T>> {
		
	}
	
}
