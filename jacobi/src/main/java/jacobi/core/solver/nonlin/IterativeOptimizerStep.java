package jacobi.core.solver.nonlin;

/**
 * Common interface for an iteration in a numeric iterative optimizer.
 * 
 * <p></p>
 * 
 * @author Y.K. Chan
 *
 */
public interface IterativeOptimizerStep {
    
    /**
     * Given the current position, find the delta vector to move to a more optimized
     * position.
     * @param func  Vector function to be optimized
     * @param curr  Current position
     * @return  Delta vector to move to new position
     */
    public double[] delta(VectorFunction func, double[] curr);

}
