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
package jacobi.api.classifier.cart;

import jacobi.core.classifier.cart.measure.Impurity;

/**
 * Data class for parameters on training a decision tree.
 * 
 * @author Y.K. Chan
 */
public class DecisionTreeParams {
	
	/**
	 * Default parameters that is suitable for most common tasks.
	 */
	public static final DecisionTreeParams DEFAULT = new DecisionTreeParams(
		Impurity.ENTROPY, Integer.MAX_VALUE
	);
	
	/**
	 * Impurity measurement function.
	 */
	public final Impurity impurityMeasure;
	
	/**
	 * Maximum height of the resultant tree.
	 */
	public final int maxHeight;

	/**
	 * Constructor
	 * @param impurityMeasure  Impurity measurement function
	 * @param maxHeight  Maximum height of the resultant tree
	 */
	public DecisionTreeParams(Impurity impurityMeasure, int maxHeight) {
		this.impurityMeasure = impurityMeasure;
		this.maxHeight = maxHeight;
	}	
	
}
