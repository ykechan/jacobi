/* 
 * The MIT License
 *
 * Copyright 2020 Y.K. Chan
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
package jacobi.api.hmm;

import java.util.Arrays;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.core.util.Throw;

/**
 * Data object for a Hidden Markov Model.
 * 
 * @author Y.K. Chan
 *
 */
public class MarkovModel {
	
	/**
	 * Factory method for initializing a default Markov Model
	 * @param init  Initial state probabilities
	 * @param numOut  Number of possible outcomes
	 * @return  An instance of Markov Model
	 */
	public static MarkovModel of(double[] init, int numOut) {
		Throw.when()
			.isTrue(() -> init == null || init.length < 1, () -> "No initial probabilites")
			.isTrue(() -> numOut < 1, () -> "Invalid number of outcome " + numOut);
		
		Matrix transit = Matrices.zeros(init.length);
		Matrix emit = Matrices.zeros(init.length, numOut);
		
		for(int i = 0; i < transit.getRowCount(); i++){
			transit.getAndSet(i, r -> Arrays.fill(r, 1.0 / transit.getColCount()));
			emit.getAndSet(i, r -> Arrays.fill(r, 1.0 / emit.getColCount()));
		}
		
		return MarkovModel.of(transit, emit, init);
	}
	
	/**
	 * Factory method for constructing a Markov Model with the given parameters.
	 * @param transit  Transition probability matrix
	 * @param emit  Emission probability matrix
	 * @param init  Initial state probabilities
	 * @return  An instance of Markov Model
	 */
	public static MarkovModel of(Matrix transit, Matrix emit, double[] init) {
		return new MarkovModel(transit, emit, init).validate();
	}
	
	/**
	 * Transition probability matrix
	 */
	public final Matrix transit;
	
	/**
	 * Emission probability matrix
	 */
	public final Matrix emits;
	
	/**
	 * Initial state probabilities
	 */
	public final double[] init;

	/**
	 * Constructor
	 * @param transit  Transition probability matrix
	 * @param emits  Emission probability matrix
	 * @param init  Initial state probabilities
	 */
	protected MarkovModel(Matrix transit, Matrix emits, double[] init) {
		this.transit = transit;
		this.emits = emits;
		this.init = init;
	}
	
	/**
	 * Validate the dimensionality of this Markov Model
	 * @return  This
	 * @throws  IllegalArgumentException if dimension mismatch
	 */
	public MarkovModel validate() {
		Throw.when()
			.isNull(() -> transit, () -> "No transition probability matrix.")
			.isFalse(
				() -> transit.getRowCount() == transit.getColCount(), 
				() -> "Transition probability matrix must be a square matrix."
			)
			.isNull(() -> emits, () -> "No emission matrix")
			.isFalse(
				() -> transit.getColCount() == emits.getRowCount(), 
				() -> "Dimension mismatch. Expected {0} states, found {1} in emission matrix."
					.replace("{0}", String.valueOf(transit.getColCount()))
					.replace("{1}", String.valueOf(emits.getRowCount()))
			)
			.isNull(() -> init, () -> "No initial probabilies");
	
		return this;
	}
	
}
