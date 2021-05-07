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
package jacobi.api.classifier.ensemble;

/**
 * Data class for parameters of training a Bootstrapping Aggregation model.
 * 
 * @author Y.K. Chan
 * @param <P>  Type of parameter for training aggregated model.
 */
public class BaggingParams<P> {
	
	/**
	 * Default probability of sampling an instance.
	 */
	public static final double DEFAULT_SAMPLING_RATE = 0.75;
	
	/**
	 * Default window of convergence
	 */
	public static final int DEFAULT_CONV_DELTA = 10;
	
	/**
	 * Default threshold of convergence
	 */
	public static final double DEFAULT_CONV_TOL = 0.001;
	
	/**
	 * Factory method for creating Bootstrapping parameters with default sampling rate
	 * @param span  Dimension span, i.e. number of features
	 * @return  Bootstrapping parameters
	 */
	public static BootstrapRate defaultRate(int span) {
		return new BootstrapRate(1 / Math.sqrt(span), DEFAULT_SAMPLING_RATE);
	}		

	/**
	 * Sampling parameters used in Bootstrapping
	 */
	public final BootstrapRate rate;
	
	/**
	 * Parameters of stopping criteria
	 */
	public final StoppingCriteria stop;
	
	/**
	 * Parameters for training models
	 */
	public final P subParams;
	
	/**
	 * Constructor.
	 * @param rate  Sampling parameters used in Bootstrapping
	 * @param stop  Parameters of stopping criteria
	 * @param subParams  Parameters for training models
	 */
	public BaggingParams(BootstrapRate rate, StoppingCriteria stop, P subParams) {
		this.rate = rate;
		this.stop = stop;
		this.subParams = subParams;
	}

	/**
	 * Data class for sampling parameters used in Bootstrapping.
	 * 
	 * @author Y.K. Chan
	 *
	 */
	public static class BootstrapRate {	
		
		/**
		 * Probability of selecting a feature and an instance respectively.
		 */
		public final double dim, data;

		/**
		 * Constructor.
		 * @param dim  Probability of selecting a feature
		 * @param data  Probability of selecting an instance
		 */
		public BootstrapRate(double dim, double data) {
			this.dim = dim;
			this.data = data;
		}
		
	}
	
	/**
	 * Data class for Parameters of stopping criteria
	 * 
	 * @author Y.K. Chan
	 *
	 */
	public static class StoppingCriteria {
		
		/**
		 * Maximum number of models and number of models recently trained to consider convergence. 
		 */
		public final int limit, width;
		
		/**
		 * Tolerance for considering convergence
		 */
		public final double convTol;

		/**
		 * Constructor.
		 * @param limit  Maximum number of models
		 * @param width  Number of models recently trained to consider convergence
		 * @param convTol  Tolerance for considering convergence
		 */
		public StoppingCriteria(int limit, int width, double convTol) {
			this.limit = limit;
			this.width = width;
			this.convTol = convTol;
		}
		
	}
	
}
