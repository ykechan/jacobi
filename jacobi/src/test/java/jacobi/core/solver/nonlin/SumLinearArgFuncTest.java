package jacobi.core.solver.nonlin;

import jacobi.api.Matrix;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.JacobiJUnit4ClassRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@JacobiImport("/jacobi/test/data/SumLinearArgFuncTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class SumLinearArgFuncTest {
    
    @JacobiInject(0)
    public Matrix coeffs;
    
    @JacobiInject(1)
    public Matrix input;
    
    @JacobiResult(100)
    public Matrix gradient;
    
    @JacobiResult(101)
    public Matrix hessian;
    
    @Test
    @JacobiImport("test g(x) = x")
    @JacobiEquals(expected = 100, actual = 100)
    @JacobiEquals(expected = 101, actual = 101)
    public void testGxIsTheIdentityFunction() {
        VectorFunction func = new SumLinearArgFunc(this.coeffs, i -> this.identity());
        this.gradient = func.grad(this.input.getRow(0));
        this.hessian = func.hess(this.input.getRow(0));
    }
    
    @Test
    @JacobiImport("test g(x) = sin(x)")
    @JacobiEquals(expected = 100, actual = 100)
    @JacobiEquals(expected = 101, actual = 101)
    public void testGxIsTheSinFunction() {
        VectorFunction func = new SumLinearArgFunc(this.coeffs, i -> this.sin());
        this.gradient = func.grad(this.input.getRow(0));
        this.hessian = func.hess(this.input.getRow(0));
    }
    
    protected ScalarFunction identity() {
        return new ScalarFunction() {

            @Override
            public double valueAt(double x) {
                return x;
            }

            @Override
            public double slopeAt(double x) {
                return 1.0;
            }

            @Override
            public double convexityAt(double x) {
                return 0;
            }
            
        };
    }
    
    protected ScalarFunction sin() {
        return new ScalarFunction() {

            @Override
            public double valueAt(double x) {
                return Math.sin(x);
            }

            @Override
            public double slopeAt(double x) {
                return Math.cos(x);
            }

            @Override
            public double convexityAt(double x) {
                return -Math.sin(x);
            }
            
        };
    }

}
