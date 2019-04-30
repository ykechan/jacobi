package jacobi.core.solver.nonlin;

public interface NonLinearOptimizingStep {
    
    public double[] delta(VectorFunction func, double[] init);

}
