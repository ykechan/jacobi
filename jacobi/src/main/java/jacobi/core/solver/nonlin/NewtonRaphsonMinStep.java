package jacobi.core.solver.nonlin;

import java.util.Arrays;
import java.util.function.Function;

import jacobi.api.Matrix;
import jacobi.core.decomp.chol.CholeskyDecomp;
import jacobi.core.impl.ColumnVector;
import jacobi.core.solver.Substitution;

/**
 * Implementation of an iteration in Newton-Raphson's Method.
 * 
 * <p>
 * Given a multivariate vector function F: R<sup>n</sup> -&gt; R,<br> 
 * let G(x) = [dF/dx<sub>i</sub>] be the gradient, and<br>
 *     H(x) = [d<sup>2</sup>F/dx<sub>i</sub>dx<sub>j</sub>] be the Hessian matrix.<br>
 * 
 * Minimizing F is equivalent to find x s.t. G(x) = 0 and H(x) is positive-definite.<br>
 * 
 * Approximate G by the 1st-order Taylor expansion,<br>
 * G(x + dx) = G(x) + H(x) * dx<br>
 * 
 * For G(x + dx) = 0, dx = -H<sup>-1</sup>(x) * G(x)<br> 
 * 
 * The Newton-Raphson's Method thus iterates the procedure still a solution is found.<br>
 * </p>
 * 
 * <p>
 * Instead of computing the inverse of the Hessian matrix, the following system can be considered:<br>
 * 
 * H(x) * dx = -G(x)<br>
 * 
 * For a minimization problem, H should be positive-definite. Thus the Cholesky decomposition
 * H = L * L<sup>t</sup> can be found, which ease the effort of solving the system of linear equations.<br>
 * 
 * Also the case when H is not positive-definite can be detected, which indicates the gradient
 * is going to an wrong direction. In this case a fall-base function is called.
 * </p>
 * 
 * @author Y.K. Chan
 *
 */
public class NewtonRaphsonMinStep implements NonLinearOptimizingStep {
    
    public NewtonRaphsonMinStep(NonLinearOptimizingStep base) {
        this(new CholeskyDecomp(),
            m -> new Substitution(Substitution.Mode.BACKWARD, m),
            m -> new Substitution(Substitution.Mode.FORWARD, m),
            base
        );
    }
    
    public NewtonRaphsonMinStep(CholeskyDecomp chol, 
            Function<Matrix, Substitution> backSub,
            Function<Matrix, Substitution> fwdSub,
            NonLinearOptimizingStep base) {
        this.chol = chol;
        this.backSub = backSub;
        this.fwdSub = fwdSub;
        this.base = base;
    }

    @Override
    public double[] delta(VectorFunction func, double[] init) {
        ColumnVector gradient = func.grad(init);
        Matrix hessian = func.hess(init);        
        return this.chol.compute(hessian)
                .map(this::mirror)
                .map(l -> this.doubleSub(l, gradient))
                .map(g -> this.negate(g))
                .orElse(null);
    }        
    
    protected Matrix mirror(Matrix lower) {
        Matrix upper = lower;
        for(int i = 0; i < upper.getRowCount(); i++) {
            int index = i;
            upper.getAndSet(i, r -> {
                for(int j = index + 1; j < r.length; j++) {
                    r[j] = lower.get(j, index);
                }
            });
        }
        return upper;
    }
    
    protected ColumnVector doubleSub(Matrix lowerUpper, ColumnVector y) {
        this.fwdSub.apply(lowerUpper).compute(y);
        this.backSub.apply(lowerUpper).compute(y);
        return y;
    }
    
    protected double[] negate(ColumnVector vector) {
        double[] v = vector.getVector();
        for(int i = 0; i < v.length; i++) {
            v[i] *= -1.0;
        }
        return v;
    }

    private CholeskyDecomp chol;
    private Function<Matrix, Substitution> backSub, fwdSub;
    private NonLinearOptimizingStep base;
}
