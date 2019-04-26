package jacobi.core.solver.nonlin;

/**
 * Representation of a real single variable scalar-value differentiable function F: R -&gt; R.
 * 
 * @author Y.K. Chan
 *
 */
public interface ScalarFunction {
    
    public double valueAt(double x);
    
    public double slopeAt(double x);
    
    public double convexityAt(double x);

}
