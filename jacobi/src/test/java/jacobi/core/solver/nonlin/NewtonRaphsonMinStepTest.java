package jacobi.core.solver.nonlin;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.core.impl.ColumnVector;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.JacobiJUnit4ClassRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@JacobiImport("/jacobi/test/data/NewtonRaphsonMinStepTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class NewtonRaphsonMinStepTest {
    
    @JacobiInject(0)
    public Matrix input;
    
    @JacobiInject(1)
    public Matrix params;
    
    @JacobiResult(100)
    public Matrix output;
    
    @Test
    @JacobiImport("test f(x) = x^2 + y^2")
    @JacobiEquals(expected = 100, actual = 100)    
    public void testShouldMoveTowardsOriginForStandardHalfSphere() {
        double[] dx = new NewtonRaphsonMinStep(null)
                .delta(this.halfSphere(1, 0, 1, 0), this.input.getRow(0));
        this.output = Matrices.wrap(new double[][] {dx});
    }
    
    @Test
    @JacobiImport("test std normal dist 2D")
    public void testStdNormalDist2D() {
    }
    
    // f(x) = (px - q)^2 + (uy - v)^2
    protected VectorFunction halfSphere(double p, double q, double u, double v) {
        return new VectorFunction() {

            @Override
            public ColumnVector grad(double[] pos) {
                // f'(x) = 2p(px - q)
                return new ColumnVector(new double[] {
                    2*p*(p*pos[0] - q),
                    2*u*(u*pos[0] - v)
                });
            }

            @Override
            public Matrix hess(double[] pos) {
                return Matrices.diag(new double[] {
                    2 * p * p,
                    2 * u * u
                });
            }

            @Override
            public double at(double[] pos) {
                return (p * pos[0] - q) * (p * pos[0] - q)
                     + (u * pos[1] - v) * (u * pos[1] - v);
            }
            
        };
    }
    

}
