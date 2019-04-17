package jacobi.core.logit;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.DoubleFunction;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.api.ext.Decomp;
import jacobi.core.op.Mul;
import jacobi.core.util.Pair;

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
 * dF/dB<sub>ij</sub> = Sum{ -x<sub>i</sub>x<sub>j</sub>p(1 - p) }
 * </p>
 * 
 * <p>
 * The maxima of log-likelihood can be found by finding the solution of G = 0 
 * where G = grad(F) = [dF/dB<sub>i</sub>]<sup>T</sup> is the gradient.
 * 
 * This implementation uses Newton-Raphson's method to find the solution.
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
public class NewtonRaphsonLogitRegr implements LogisticRegr { 

    @Override
    public double[] compute(Matrix input, double[] outcome, double[] weights) { 
        return null;
    }
    
    protected Optional<double[]> delta(Matrix hessian, double[] grad) {
        Pair decomp = hessian.ext(Decomp.class).chol2().orElse(null);
        if(decomp == null) {
            return Optional.empty();
        }
        return null;
    }
    
    protected Matrix hessian(Matrix input, double[] prob) {
        Matrix hess = Matrices.zeros(1 + input.getColCount());
        for(int k = 0; k < input.getRowCount(); k++) {
            double[] vector = input.getRow(k);
            double weight = prob[k] * (1.0 - prob[k]);
            
            hess.getAndSet(0, row -> row[0] += weight);
            for(int i = 1; i < hess.getRowCount(); i++) {
                int n = i;
                double coeff = weight * vector[i - 1];
                hess.getAndSet(i, row -> {
                    row[0] += coeff;
                    for(int j = 1; j < n; j++) {
                        row[j] += coeff * vector[j - 1];
                    }
                });
            }
        } 
        return hess;
    }
    
    protected double[] gradient(Matrix input, double[] outcome, double[] prob) {
        double[] grad = new double[1 + input.getColCount()];
        for(int i = 0; i < input.getRowCount(); i++) {
            double[] vector = input.getRow(i);
            double res = outcome[i] - prob[i];
            grad[0] += res;
            for(int j = 1; j < grad.length; j++) {
                grad[j] += vector[j - 1] * res; 
            }
        }
        return grad;
    }
    
    protected double[] estimates(Matrix input, double[] coeff) {
        double[] err = new double[input.getRowCount()];
        for(int i = 0; i < input.getRowCount(); i++) {
            err[i] = this.sigmoid(this.affine(coeff, input.getRow(i)));
        }
        return err;
    }
    
    protected double affine(double[] coeff, double[] vector) {
        double res = coeff[0];
        for(int i = 0; i < vector.length; i++) {
            res += coeff[i + 1] * vector[i];
        }
        return res;
    }
    
    protected double sigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }

}
