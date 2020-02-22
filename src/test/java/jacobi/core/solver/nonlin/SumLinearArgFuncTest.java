package jacobi.core.solver.nonlin;

import java.util.Arrays;

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
        
        VectorFunction id = new SumLinearArgFunc<Void>(this.coeffs) {

            @Override
            protected double valueAt(Void inter, int index, double x) {
                return x;
            }

            @Override
            protected double slopeAt(Void inter, int index, double x) {
                return 1.0;
            }

            @Override
            protected double convexityAt(Void inter, int index, double x) {
                return 0.0;
            }

            @Override
            protected Void prepare(double[] pos, double[] args) {
                return null;
            }
            
        };
        this.gradient = id.grad(this.input.getRow(0));
        this.hessian = id.hess(this.input.getRow(0));
    }
    
    @Test
    @JacobiImport("test g(x) = sin(x)")
    @JacobiEquals(expected = 100, actual = 100)
    @JacobiEquals(expected = 101, actual = 101)
    public void testGxIsTheSinFunction() {
        VectorFunction sinFunc = new SumLinearArgFunc<Void>(this.coeffs) {

            @Override
            protected double valueAt(Void inter, int index, double x) {
                return Math.sin(x);
            }

            @Override
            protected double slopeAt(Void inter, int index, double x) {
                return Math.cos(x);
            }

            @Override
            protected double convexityAt(Void inter, int index, double x) {
                return -Math.sin(x);
            }

            @Override
            protected Void prepare(double[] pos, double[] args) {
                return null;
            }
            
        };
        this.gradient = sinFunc.grad(this.input.getRow(0));
        this.hessian = sinFunc.hess(this.input.getRow(0));
    }    
    
}
