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
 * Data class for training parameters of Support Vector Machines.
 * 
 * @author Y.K. Chan
 *
 */
public class SupportVectorMachineParams {
	
	public static final double DEFAULT_LEARNING_RATE = 0.05;
	
	public static final double DEFAULT_ERROR_TOL = 1e-6;
	
	public static final int DEFAULT_MIN_EPOCH = 4;
	
	public static final int DEFAULT_MAX_EPOCH = 256;
	
	public static final double DEFAULT_DECAY_RATE = 0.9;
	
	/**
	 * Factory method using given regulation coefficient with other parameters set as default values
	 * @param lambda  Regulation cofficient
	 * @return  Training parameters
	 */
	public static SupportVectorMachineParams ofDefault(double lambda) {
		return new SupportVectorMachineParams()
			.setRegulationCoeff(lambda)
			.setErrorTolerance(DEFAULT_ERROR_TOL)
			.setLearningRate(DEFAULT_LEARNING_RATE)
			.setDecayRate(DEFAULT_DECAY_RATE)
			.setMinEpoch(DEFAULT_MIN_EPOCH)
			.setMaxEpoch(DEFAULT_MAX_EPOCH);
	}
	
	private double regulationCoeff;
	
	private double learningRate;
	
	private double decayRate;
	
	private double errorTolerance;
	
	private int minEpoch;
	
	private int maxEpoch;

	public double getRegulationCoeff() {
		return regulationCoeff;
	}

	public SupportVectorMachineParams setRegulationCoeff(double regulationCoeff) {
		this.regulationCoeff = regulationCoeff;
		return this;
	}

	public double getLearningRate() {
		return learningRate;
	}

	public SupportVectorMachineParams setLearningRate(double learningRate) {
		this.learningRate = learningRate;
		return this;
	}
	
	public double getDecayRate() {
		return decayRate;
	}

	public SupportVectorMachineParams setDecayRate(double decayRate) {
		this.decayRate = decayRate;
		return this;
	}

	public double getErrorTolerance() {
		return errorTolerance;
	}

	public SupportVectorMachineParams setErrorTolerance(double errorTolerance) {
		this.errorTolerance = errorTolerance;
		return this;
	}

	public int getMinEpoch() {
		return minEpoch;
	}

	public SupportVectorMachineParams setMinEpoch(int minEpoch) {
		this.minEpoch = minEpoch;
		return this;
	}

	public int getMaxEpoch() {
		return maxEpoch;
	}

	public SupportVectorMachineParams setMaxEpoch(int maxEpoch) {
		this.maxEpoch = maxEpoch;
		return this;
	}
	
}
