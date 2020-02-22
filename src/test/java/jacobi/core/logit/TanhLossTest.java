package jacobi.core.logit;

import java.util.stream.IntStream;

import org.junit.Test;
import org.junit.runner.RunWith;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.core.solver.nonlin.VectorFunction;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.JacobiJUnit4ClassRunner;

/**
 * 
 * @author Y.K. Chan
 *
 */
@JacobiImport("/jacobi/test/data/TanhLossTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class TanhLossTest {
	
	@JacobiInject(0)
    public Matrix data;
    
    @JacobiInject(1)
    public Matrix outcome;
    
    @JacobiInject(2)
    public Matrix position;
    
    @JacobiResult(100)
    public Matrix value;
    
    @JacobiResult(101)
    public Matrix gradient;
    
    @JacobiResult(102)
    public Matrix hessian;
	
	@Test
	@JacobiImport("test 2 vars with bias")
	@JacobiEquals(expected = 100, actual = 100)
    @JacobiEquals(expected = 101, actual = 101)
    @JacobiEquals(expected = 102, actual = 102)
	public void test2VarsWithBias() {
		VectorFunction func = new TanhLoss(this.data, 
                IntStream.range(0, this.outcome.getRowCount())
                    .mapToDouble(i -> this.outcome.get(i, 0))
                    .toArray());
        
        this.value = Matrices.scalar(func.at(this.position.getRow(0)));
        this.gradient = func.grad(this.position.getRow(0));
        this.hessian = func.hess(this.position.getRow(0));
	}

}
