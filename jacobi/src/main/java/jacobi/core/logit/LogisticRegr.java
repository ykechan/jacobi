package jacobi.core.logit;

import jacobi.api.Matrix;

/**
 * Common interface for implementations of Logistic Regression.
 * 
 * <p>
 * Given a data set {X<sub>k</sub>, y<sub>k</sub>} where X<sub>k</sub> are vectors 
 * and y<sub>k</sub> in {1, 0} for all k, 
 * the Logistic Regression is to fit the Sigmoid function p(X) = 1 / (1 + e<sup>-&lt;B, X&gt;</sup>) 
 * as the probability of given 1 or 0 which minimizes the error. 
 * </p>
 * 
 * <p>
 * A common practice is to obtain a maximum likelihood estimation of the model parameters
 * that fits the data. Assume the data are i.i.d. Bourneulli trials, the likelihood is
 * L(B) = Prod{ p(B, X<sub>k</sub>)<sup>y<sub>k</sub></sup> 
 *            * (1 - p(B, X<sub>k</sub>))<sup>1 - y<sub>k</sub></sup> }
 *            
 * For easier analysis, the log-likelihood is considered instead
 * 
 * F(B) = lnL(B) = Sum{ y<sub>k<sub>ln(p) + (1 - y<sub>k</sub>)ln(1 - p) }
 * </p>
 * 
 * <p>To maximize F, a solution of grad(F) = 0 should be found.</p>
 * 
 * <p>Since there is no closed form solution, different implementations use different approach
 * to obtain the solution.
 * </p>
 * 
 * @author Y.K. Chan
 *
 */
public interface LogisticRegr {
    
    public double[] compute(Matrix input, double[] outcome, double[] weights);

}
