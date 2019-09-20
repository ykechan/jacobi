package jacobi.core.classifier.cart.rule;

import java.util.List;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.api.classifier.Column;
import jacobi.api.classifier.DataTable;
import jacobi.api.classifier.Instance;
import jacobi.api.classifier.cart.DecisionNode;
import jacobi.core.classifier.cart.Sequence;

public class ChiSquarePruner implements Pruner {

	@Override
	public <T> DecisionNode<T> prune(DecisionNode<T> node, DataTable<T> dataTab, Sequence seq) {
		return null;
	}
	
	protected double statistics(Matrix contMat) {
		double[] predictDist = new double[contMat.getRowCount()];
		double[] actualDist = new double[contMat.getRowCount()];
		
		for(int i = 0; i < contMat.getRowCount(); i++){
			double[] row = contMat.getRow(i);
			for(int j = 0; j < row.length; j++){
				actualDist[j] += row[j];
				predictDist[i] += row[j];
			}			
		}
		
		double chi = 0.0;
		for(int i = 0; i < contMat.getRowCount(); i++){
			double[] row = contMat.getRow(i);
			double p = predictDist[i];
			
			for(int j = 0; j < row.length; j++) {
				double expect = p * actualDist[j] / (p + actualDist[j]);
				chi += (row[j] - expect) * (row[j] - expect) / expect;
			}
		}
		return chi;
	}
	
	protected <T> Matrix contingencyMatrix(DecisionNode<T> node, 
			DataTable<T> dataTab, 
			Sequence seq) {
		
		Column<T> outCol = dataTab.getOutcomeColumn();
		
		Matrix contMat = Matrices.zeros(outCol.cardinality());
		
		Matrix dataMat = dataTab.getMatrix();
		List<Instance> instances = dataTab.getInstances(outCol);
		
		for(int i = 0; i < seq.length(); i++){
			int index = seq.indexAt(i);
			Instance inst = instances.get(index);
			
			int predict = outCol.getItems().indexOf(node.apply(dataMat.getRow(index)));
			double[] row = contMat.getRow(predict);
			row[inst.outcome] += inst.weight;
			contMat.setRow(predict, row);
		}
		return contMat;
	}

}
