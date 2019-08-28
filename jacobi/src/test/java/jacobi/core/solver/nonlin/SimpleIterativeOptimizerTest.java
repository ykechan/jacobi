package jacobi.core.solver.nonlin;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import jacobi.api.Matrix;
import jacobi.core.impl.ColumnVector;
import jacobi.core.util.Real;
import jacobi.core.util.Weighted;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.util.JacobiJUnit4ClassRunner;

@JacobiImport("/jacobi/test/data/SimpleIterativeOptimizerTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class SimpleIterativeOptimizerTest {
    
    @JacobiInject(0)
    public Matrix start;
    
    @JacobiInject(1)
    public Matrix deltas;
    
    @JacobiInject(2)
    public Matrix steps;
    
    @JacobiInject(3)
    public Matrix finish;
    
    @Test
    @JacobiImport("test pre-defined deltas")
    public void testPreDefinedDeltas() {
        Weighted<ColumnVector> ans = new SimpleIterativeOptimizer(() -> new IterativeOptimizerStep() {

            @Override
            public double[] delta(VectorFunction func, double[] curr) {
                if(this.step > 0) {
                    Assert.assertArrayEquals(steps.getRow(step - 1), curr, Real.TOLERANCE);
                }
                return deltas.getRow(step++);
            }
            
            private int step = 0;
        }).optimize(
            this.mock(this.finish.getRow(0)), 
            () -> this.start.getRow(0), 
            1024, 
            Real.TOLERANCE
        );
        
    }
    
    protected VectorFunction mock(double[] ans) {
        return new VectorFunction() {

            @Override
            public double at(double[] pos) {
                return arrayEquals(pos, ans) ? -1.0 : 1.0;
            }

            @Override
            public ColumnVector grad(double[] pos) {
                ColumnVector g = new ColumnVector(pos.length);
                if(arrayEquals(pos, ans)) {
                    return g;
                }
                Arrays.fill(g.getVector(), 100.0);
                return g;
            }

            @Override
            public Matrix hess(double[] pos) {
                throw new UnsupportedOperationException();
            }

        };
    }

    protected static boolean arrayEquals(double[] u, double[] v) {
        for(int i = 0; i < u.length; i++) {
            if(!Real.isNegl(u[i] - v[i])) {
                return false;
            }
        }
        return true;
    }
}
