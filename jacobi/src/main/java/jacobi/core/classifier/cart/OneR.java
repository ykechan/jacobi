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

public class OneR implements Rule {
	
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
			System.out.println("Measure " + feat.getIndex());
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
	
	public interface Merger<T> extends Function<List<DecisionNode<T>>, DecisionNode<T>> {
		
	}
	
}
