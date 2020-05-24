package jacobi.core.hmm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.api.hmm.MarkovModel;
import jacobi.core.prop.Transpose;
import jacobi.core.util.Throw;

/**
 * Implementation of the Viterbi algorithm.
 * 
 * <p>
 * Given a hidden markov model, let 
 * X: R<sup>MxM</sup> be the transition matrix,
 * Y: R<sup>MxN</sup> be the emission matrix,
 * &pi;: R<sup>M</sup> be the initial probabilities, and also given
 * {y[t] &isin; [0, N) | t = 0, 1, ...} be a sequence of observation.
 * </p>
 * 
 * <p>The Viterbi algorithm finds the most probable sequence of states {x[t]} that
 * gives the sequence of observation {y[t]}. It is noteworthy to state it is the most probable
 * path that is interesting, not the most probable state. The probability that the system
 * is in a particular state at time t, i.e. P(x[t] = i | Y) is the sum of all paths leading
 * to that state at time t. However, there maybe a more probable path leading to another state
 * but while other paths are less probable, the super-position is more probable.</p>
 * 
 * <p>
 * Thus for each transition only the most probable path is considered. 
 * P(x[t] = i | Y) = max{ P(x[t-1] = j | Y) * X[j, i] * Y[i, y[t]] }
 *                 = Y[i, y[t]] * max{ P(x[t-1] = j | Y) * X[j, i] }
 * 
 * Since the probability of a single path is vanishingly small, the log of the probabilities
 * is considered instead:
 * 
 * lnP(x[t] = i | Y) = ln(Y[i, y[t]]) + max{ lnP(x[t - 1] = j | Y) + ln(X[j, i]) } 
 * </p>
 * 
 * @author Y.K. Chan
 *
 */
public class Viterbi {
	
	/**
	 * Constructor.
	 */
	public Viterbi() {
		this(new Transpose());
	}

	/**
	 * Constructor.
	 * @param transpose  Implementation of computing Matrix transpose
	 */
	protected Viterbi(Transpose transpose) {
		this.transpose = transpose;
	}

	/**
	 * Compute the most probable sequence of hidden states given a series of observations 
	 * according to the given Markov Model
	 * @param mm  Input Markov Model
	 * @param obs  Input series of observations
	 * @return  Sequence of hidden states
	 */
	public int[] compute(MarkovModel mm, int[] obs) {
		mm.validate();
		
		Matrix txFrom = this.logT(mm.transit);
		Matrix emitBy = this.logT(mm.emits);
		
		double[] lnInit = Arrays.stream(mm.init).map(Math::log).toArray();
		
		if(obs.length < 1){
			return new int[0];
		}

		return this.dynProg(txFrom, emitBy, lnInit, obs);
	}
	
	/**
	 * Perform dynamic programming on the series of observation.
	 * @param txFrom  Transpose of transition probabilities
	 * @param emitBy  Transpose of emission probabilities
	 * @param init  Probability of initial states
	 * @param obs  Input series of observation
	 * @return  Sequence of hidden states
	 */
	protected int[] dynProg(Matrix txFrom, Matrix emitBy, double[] init, int[] obs) {
		List<short[]> table = new ArrayList<>(obs.length);
		table.add(new short[0]);
	
		double[] prev = new double[init.length];
		double[] first = emitBy.getRow(obs[0]);
		
		for(int i = 0; i < prev.length; i++){
			prev[i] = init[i] + first[i];
		}
		
		for(int t = 1; t < obs.length; t++){
			Track track = this.forward(txFrom, emitBy.getRow(obs[t]), prev);
			table.add(track.from);
			prev = track.values;
		}
		
		// backtrack
		int max = 0;
		for(int i = 1; i < prev.length; i++){
			if(prev[max] < prev[i]) {
				max = i;
			}
		}
		return this.backtrack(table, max);
	}
	
	/**
	 * Backtrack from a table of source indices
	 * @param table  Table of source indices
	 * @param last  Starting index
	 * @return  Sequence of the index of the path
	 */
	protected int[] backtrack(List<short[]> table, int last) {
		int[] seq = new int[table.size()];
		seq[seq.length - 1] = last;
		for(int t = seq.length - 1; t > 0; t--){
			seq[t - 1] = table.get(t)[ seq[t] ];
		}
		return seq;
	}
	
	/**
	 * Compute the maximum probabilities that the most probable path to the next step 
	 * ends on each state
	 * @param txFrom  Transpose of the transition probabilities
	 * @param by  Emission probabilities of each state according to current observation
	 * @param curr  Probabilities of the path at the current next step in each state  
	 * @return  Probabilities of each state in the next step and the source state
	 */
	protected Track forward(Matrix txFrom, double[] by, double[] curr) {
		short[] from = new short[curr.length];
		double[] next = new double[curr.length];
		for(int i = 0; i < next.length; i++) {
			double[] row = txFrom.getRow(i);
			
			double maxLike = 0.0;
			int argmax = -1;
			
			for(int j = 0; j < row.length; j++){
				double lnLike = row[j] + curr[j];
				if(argmax < 0 || lnLike > maxLike){
					argmax = j;
					maxLike = lnLike;
				}
			}
			
			next[i] = by[i] + maxLike;
			from[i] = (short) argmax;
		}
		return new Track(next, from);
	}
	
	/**
	 * Compute the transpose of a matrix and transform each element to its log value.
	 * @param input  Input matrix
	 * @return  Ln of the transposed matrix
	 */
	protected Matrix logT(Matrix input) {
		return Matrices.wrap(this.transpose.compute(input, r -> {
			for(int i = 0; i < r.length; i++){
				if(r[i] < 0.0){
					throw new UnsupportedOperationException("Negative probability " + r[i] + " not supported.");
				}
				
				if(r[i] == 0.0){
					r[i] = NEG_INF;
					continue;
				}
				
				r[i] = Math.log(r[i]);
			}
			return r;
		}).toArray(new double[0][]));
	}
	
	private Transpose transpose;
	
	/**
	 * Track information for a single step
	 * @author Y.K. Chan
	 *
	 */
	protected static class Track {
		
		/**
		 * Probability values
		 */
		public final double[] values;
		
		/**
		 * Source of the transition
		 */
		public final short[] from;

		/**
		 * Constructor.
		 * @param values  Probability for each states
		 * @param from  Source of transition for each states
		 */
		public Track(double[] values, short[] from) {
			this.values = values;
			this.from = from;
		}
		
	}
	
	/**
	 * Constant value for representing -ve infinity arising from ln(0)
	 */
	protected static final double NEG_INF = -128.0;
}
