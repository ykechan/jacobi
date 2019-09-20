package jacobi.core.classifier.ensemble;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleSupplier;
import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;

import jacobi.api.classifier.Classifier;
import jacobi.api.classifier.ClassifierLearner;
import jacobi.api.classifier.Column;
import jacobi.api.classifier.DataTable;
import jacobi.api.classifier.ensemble.BaggingParams;
import jacobi.core.classifier.cart.ArraySequence;
import jacobi.core.classifier.cart.Sequence;

public class BaggingLearner<T, C extends Classifier<T>, P> 
	implements ClassifierLearner<T, AggregatedClassifier<T, C>, BaggingParams<P>> {
	
	public BaggingLearner(ClassifierLearner<T, C, P> learner, DoubleSupplier rand) {
		this.learner = learner;
		this.rand = rand;
	}

	@Override
	public AggregatedClassifier<T, C> learn(DataTable<T> dataTab, BaggingParams<P> params) {
		List<Classifier<T>> classifiers = new ArrayList<>();
				
		for(int i = 0; i < params.numOfModels; i++) {
			
		}
		return null;
	}
	
	protected DataTable<T> subset(DataTable<T> dataTab, double subspaceRate, double samplingRate) {
		int sampleSize = (int) Math.ceil(samplingRate * dataTab.size());
		
		return null;
	}
	
	protected List<Column<?>> subspace(List<Column<?>> columns, double rate) {
		if(rate > 1.0) {
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
	
	protected Sequence randomSample(IntUnaryOperator randFn, int len) {		
		return new ArraySequence(IntStream.range(0, len).map(randFn).toArray(), 0, len);
	}
	
	private ClassifierLearner<T, C, P> learner;
	private DoubleSupplier rand;
}
