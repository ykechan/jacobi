package jacobi.core.solver.nonlin;

/**
 * Representation of a real single variable scalar-value differentiable function f: R -&gt; R.
 * 
 * @author Y.K. Chan
 *
 */
public interface ScalarFunction {
    
	/**
	 * Find the value of the function at x, i.e. f(x).
	 * @param x  Value of x
	 * @return  Value of the function at x, i.e. f(x)
	 */
    public double valueAt(double x);
    
    /**
     * Find the slope of the function at x, i.e. f'(x)
     * @param x  Value of x
	 * @return  Value of the function at x, i.e. f'(x)
     */
    public double slopeAt(double x);
    
    /**
     * Find the convexity of the function at x, i.e. f''(x)
     * @param x  Value of x
     * @return  Value of the function at x, i.e. f''(x)
     */
    public double convexityAt(double x);

}
