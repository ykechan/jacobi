package jacobi.core.solver.nonlin;

import jacobi.api.Matrix;
import jacobi.core.impl.ColumnVector;

public interface VectorFunction {
    
    public ColumnVector grad(double[] pos);
    
    public Matrix hess(double[] pos);

}
