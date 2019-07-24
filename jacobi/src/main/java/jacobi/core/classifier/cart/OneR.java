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

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;
import java.util.stream.Collectors;

import jacobi.api.Matrix;
import jacobi.core.classifier.cart.data.Column;
import jacobi.core.classifier.cart.data.DataTable;
import jacobi.core.classifier.cart.data.Instance;
import jacobi.core.classifier.cart.data.Sequence;
import jacobi.core.classifier.cart.node.BinaryNumericSplit;
import jacobi.core.classifier.cart.node.DecisionNode;
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
	public OneR(Rule zeroR, Partition<?> partition) {
		this.zeroR = zeroR;
		this.partition = partition;
	}

	@Override
	public <T> Weighted<DecisionNode<T>> make(DataTable<T> dataTable, 
			Set<Column<?>> features, 
			Sequence seq) {
		Weighted<?> min = null;
		Column<?> target = null;
		for(Column<?> feat : features) {
			Weighted<?> split = this.partition.measure(dataTable, feat, seq);
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
		
		List<Sequence> subseq = seq.groupBy(this.splitFunc(dataTable, target, min.item));
		List<DecisionNode<T>> nodes = subseq.stream()
				.map(s -> this.zeroR.make(dataTable, subfeats, s))
				.map(w -> w.item)
				.collect(Collectors.toList());
		
		return new Weighted<>(this.<T>mergeFunc(target, min.item).apply(nodes), min.weight);
	}
	
	/**
	 * Get the merging function according to a feature column and given generic split info
	 * @param col  Feature column
	 * @param splitInfo  Split info
	 * @return  Merging function
	 */
	public <T> Merger<T> mergeFunc(Column<?> col, Object splitInfo) {
		if(!col.isNumeric()){
			return ls ->  new NominalSplit<>(col, null, ls);
		}
		
		if(splitInfo instanceof Double){
			double thres = (Double) splitInfo;
			return ls -> new BinaryNumericSplit<>(col, thres, ls.get(0), ls.get(1));
		}
		
		throw new UnsupportedOperationException("Unable to merge by " + splitInfo);
	}
	
	/**
	 * Get the splitting function from a decision node created by this class
	 * @param dataTab  Data table
	 * @param node  Decision node created
	 * @return  Splitting function
	 */
	public IntUnaryOperator splitFunc(DataTable<?> dataTab, DecisionNode<?> node) {
		if(node instanceof NominalSplit){ 
			return this.splitFunc(dataTab, node.split(), null);
		}

		if(node instanceof BinaryNumericSplit){
			return this.splitFunc(dataTab, node.split(), 
				((BinaryNumericSplit<?>) node).getThreshold());
		}
		
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Get the splitting function from a target feature column and split info
	 * @param dataTab  Data table
	 * @param col  Feature column
	 * @param splitInfo Split info
	 * @return  Splitting function
	 */
	protected IntUnaryOperator splitFunc(DataTable<?> dataTab, Column<?> col, Object splitInfo) {
		
		if(!col.isNumeric()){
			List<Instance> instances = dataTab.getInstances(col);
			return i -> instances.get(i).feature;
		}
		
		if(splitInfo instanceof Double) {
			double thres = (Double) splitInfo;
			Matrix mat = dataTab.getMatrix();
			return i -> mat.get(i, col.getIndex()) < thres ? 0 : 1;
		}
		
		if(splitInfo instanceof double[]){
			double[] bounds = (double[]) splitInfo;
			Matrix mat = dataTab.getMatrix();
			return i -> Math.abs(Arrays.binarySearch(bounds, mat.get(i, col.getIndex())));
		}

		throw new UnsupportedOperationException("Unable to split by " + splitInfo);
	}			
	
	private Rule zeroR;
	private Partition<?> partition;
	
	/**
	 * 
	 * @author Y.K. Chan
	 * @param <T>  Type of outcome
	 */
	public interface Merger<T> extends Function<List<DecisionNode<T>>, DecisionNode<T>> {
		
	}
	
}
