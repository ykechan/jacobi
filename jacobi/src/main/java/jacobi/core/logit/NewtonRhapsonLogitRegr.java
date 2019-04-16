package jacobi.core.logit;

import jacobi.api.Matrix;

/**
 * Implementation of Logistic Regression using Newton-Rhapson's method.
 * 
 * <p>
 * Consider the log-likelihood of the Logistic Regression model
 * 
 * F(B) = Sum{ y<sub>k<sub>ln(p) + (1 - y<sub>k</sub>)ln(q) } 
 * 
 * where p(B, X) is the Sigmoid function and q(B, X) = 1 - p(B, X)
 * </p>
 * 
 * <p>
 * Since dp/dB<sub>i</sub> =  x<sub>i</sub>*pq,
 *       dq/dB<sub>i</sub> = -x<sub>i</sub>*pq</sup>,
 *       
 * dF/dB<sub>i</sub> = Sum{ yx<sub>i<sub>q - (1 - y)x<sub>i<sub>p }, with summation index omitted
 *                   = Sum{ x<sub>i</sub>[y(1 - p) - (1 - y)p ] }
 *                   = Sum{ x<sub>i</sub>(y - p) }
 *                   
 * dF/dB<sub>ij</sub> = Sum{ -x<sub>i</sub><sup>2</sup>p(1 - p) }
 * </p>
 * 
 * <p>
 * The maxima of log-likelihood can be found by finding the solution of G = 0 
 * where G = grad(F) = [dF/dB<sub>i</sub>]<sup>T</sup> is the gradient.
 * 
 * This implementation uses Newton-Rhapson's method to find the solution.
 * </p>
 * 
 * <p>
 * Approximate G(B + d) = G(B) + H(B) * d where H = [ dF/dB<sub>ij</sub> ] is the Hessian matrix.
 * 
 * For G(B + d) = 0, d = -H(B)<sup>-1</sup> * G(B).
 * 
 * Thus reiterate B<sub>n + 1<sub> = B<sub>n<sub> - H<sup>-1</sup> * G until converges.
 * </p>
 * 
 * <p>
 * Computing inverse may be expensive. This can be viewed as solving a system of linear equation
 * H * x = G. 
 * </p>
 * @author Y.K. Chan
 *
 */
public class NewtonRhapsonLogitRegr implements LogisticRegr {

    @Override
    public Matrix compute(Matrix input, Matrix outcome, double[] weights) {
        // TODO Auto-generated method stub
        return null;
    }

}
