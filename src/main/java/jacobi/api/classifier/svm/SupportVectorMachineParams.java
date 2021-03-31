/*
 * The MIT License
 *
 * Copyright 2021 Y.K. Chan
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
package jacobi.api.classifier.svm;

import jacobi.core.util.Real;

/**
 * Data class for parameters to train a Support Vector Machine.
 * 
 * <p>
 * The most important parameter is the control parameter that is included in the Hinge-Loss function:
 * H(w, b) = C||w||<sup>2</sup> + &sum;max(0, y - &lt;w, x&gt; + b). In this library it follows the PEGASOS
 * formulation that the control parameter is the coefficient of the norm term. This is equivalent to setting
 * the reciprocal value in the formulated used in SMO. 
 * </p>
 * 
 * <p>
 * Other parameters concerns about the switching between using PEGASOS and SMO. By default, if the number of data
 * is less than smoMax, SMO is used. If the number of data is larger than smoMax, a solution is found using
 * PEGASOS first. The PEGASOS algorithm runs for minEpochs * number of data steps, and check if the support
 * vector found within smoMargin is less than the square root of the number of data, and check if the distribution
 * within the margin is similiar to the whole distribution enough,  and if so it would pass the data within smoMargin 
 * to SMO for calibration. If the number of support vector is too large, PEGASOS would continue until 
 * maxEpochs * number of data steps is run.
 * </p>
 * 
 * <p>To train SVM using PEGASOS only, set minEpoch = maxEpoch. To train SVM using SMO only,
 * set maxEpoch = 0, or smoMax sufficiently high.</p>
 * 
 * @author Y.K. Chan
 *
 */
public class SupportVectorMachineParams {
	
	/**
	 * Default minimum number of epochs to run in PEGASOS algorithm.
	 */
	public static final int DEFAULT_MIN_EPOCH = 16;
	
	/**
	 * Default maximum number of epochs to run in PEGASOS algorithm.
	 */
	public static final int DEFAULT_MAX_EPOCH = 24;
	
	/**
	 * Default maximum number of instances to use SMO 
	 */
	public static final int DEFAULT_SMO_MAX = 32;
	
	/**
	 * Default margin of passing support vectors to SMO
	 */
	public static final double DEFAULT_SMO_MARGIN = 1 / Real.GOLDEN_RATIO;
	
	/**
	 * Default similarity of distribution between within margin and whole data to use SMO
	 */
	public static final double DEFAULT_SMO_SIMILIARITY = 0.75;
	
	/**
	 * Factory method with all default values other than the control parameter
	 * @param control  Control parameter
	 * @return  Default parameters with control set
	 */
	public static SupportVectorMachineParams of(double control) {
		return new SupportVectorMachineParams()
			.setControl(control)
			.setSmoMax(DEFAULT_SMO_MAX).setSmoMargin(DEFAULT_SMO_MARGIN)
			.setMinEpochs(DEFAULT_MIN_EPOCH).setMaxEpochs(DEFAULT_MAX_EPOCH);
	}

	private double control;

	private int minEpochs;

	private int maxEpochs;

	private int smoMax;

	private double smoMargin;
	
	private double smoSimilarity;

	public double getControl() {
		return control;
	}

	public SupportVectorMachineParams setControl(double control) {
		this.control = control;
		return this;
	}

	public int getMinEpochs() {
		return minEpochs;
	}

	public SupportVectorMachineParams setMinEpochs(int minEpochs) {
		this.minEpochs = minEpochs;
		return this;
	}

	public int getMaxEpochs() {
		return maxEpochs;
	}

	public SupportVectorMachineParams setMaxEpochs(int maxEpochs) {
		this.maxEpochs = maxEpochs;
		return this;
	}

	public int getSmoMax() {
		return smoMax;
	}

	public SupportVectorMachineParams setSmoMax(int smoMax) {
		this.smoMax = smoMax;
		return this;
	}

	public double getSmoMargin() {
		return smoMargin;
	}

	public SupportVectorMachineParams setSmoMargin(double smoMargin) {
		this.smoMargin = smoMargin;
		return this;
	}

	public double getSmoSimilarity() {
		return smoSimilarity;
	}

	public SupportVectorMachineParams setSmoSimilarity(double smoSimilarity) {
		this.smoSimilarity = smoSimilarity;
		return this;
	}

}
