package jacobi.core.hmm;

import java.util.List;

import jacobi.api.Matrix;
import jacobi.core.op.Mul;
import jacobi.core.prop.Transpose;
import jacobi.core.util.Throw;

/**
 * Implementation of the Viterbi algorithm.
 * 
 * @author Y.K. Chan
 *
 */
public class Viterbi {
	
	public int[] compute(Matrix transit, Matrix emit, double[] init, int[] obs) {
		Throw.when()
			.isNull(() -> transit, () -> "No transition probability matrix.")
			.isFalse(
				() -> transit.getRowCount() == transit.getColCount(), 
				() -> "Transition probability matrix must be a square matrix."
			)
			.isNull(() -> emit, () -> "No emission matrix")
			.isFalse(
				() -> transit.getColCount() == emit.getRowCount(), 
				() -> "Dimension mismatch. Expected {0} states, found {1} in emission matrix."
					.replace("{0}", String.valueOf(transit.getColCount()))
					.replace("{1}", String.valueOf(emit.getRowCount()))
			)
			.isNull(() -> init, () -> "No initial probabilies")
			.isNull(() -> obs, () -> "No observation") 
			;
		
		if(obs.length < 1){
			return new int[0];
		}
		
		// given an observation, the probability it is from a state previously 
		Matrix givenFrom = this.transpose.compute(this.mul.compute(transit, emit));
		// ...
		return null;
	}
	
	protected List<int[]> dynProg() {
		return null;
	}
	
	protected int[] backtrack(List<int[]> dirTable, int last) {
		int[] seq = new int[dirTable.size()];
		seq[seq.length - 1] = last;
		
		for(int i = 1; i < seq.length; i++) {
			int k = seq.length - 1 - i;
			int[] dir = dirTable.get(k + 1);
			
			seq[k] = dir[seq[k + 1]];
		}
		return seq;
	}

	private Mul mul;
	private Transpose transpose;
}
