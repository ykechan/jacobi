package jacobi.core.signal.fft;

import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.core.signal.ComplexVector;
import jacobi.core.util.Pair;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.Jacobi;
import jacobi.test.util.JacobiJUnit4ClassRunner;

@JacobiImport("/jacobi/test/data/ShortTimeFourierTransformTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class ShortTimeFourierTransformTest {
	
	@JacobiInject(0)
	public Matrix inRe;
	
	@JacobiInject(1)
	public Matrix inIm;
	
	@JacobiInject(100)
	public Matrix oracleRe;
	
	@JacobiInject(101)
	public Matrix oracleIm;
	
	@JacobiResult(10)
	public Matrix outRe;
	
	@JacobiResult(11)
	public Matrix outIm;	
	
	@JacobiInject(-1)
    public Map<Integer, Matrix> results;
	
	@Test
	@JacobiImport("Test STFT-8 on Complex 13")
	@JacobiEquals(expected = 10, actual = 10)
	@JacobiEquals(expected = 11, actual = 11)
	public void testStft8OnComplex13() {
		List<Pair> result = this.mock().compute(this.inRe, this.inIm, 8);
		Assert.assertEquals(1, result.size());
		this.outRe = result.get(0).getLeft();
		this.outIm = result.get(0).getRight();		
	}
	
	@Test
	@JacobiImport("Test STFT-5 on Pure Real 4x10")
	public void testStft5OnPureReal4x10() {
		List<Pair> result = this.mock().compute(this.inRe, 
				Matrices.zeros(this.inRe.getRowCount(), this.inRe.getColCount()), 
				5);
		Assert.assertEquals(4, result.size());
		for(int i = 0; i < result.size(); i++) {
			Jacobi.assertEquals(this.results.get(10 + 2*i), result.get(i).getLeft());
			Jacobi.assertEquals(this.results.get(11 + 2*i), result.get(i).getRight());
		}
	}
	
	private ShortTimeFourierTransform mock() {
		return new ShortTimeFourierTransform(new DiscreteFourierTransform.Forward() {

			@Override
			public UnaryOperator<ComplexVector> toFunc(int len) {				
				return v -> {
					for(int i = 0; i < inRe.getRowCount(); i++) {
						if(this.areEqual(inRe.getRow(i), v.real)){
							return ComplexVector.of(oracleRe.getRow(i), oracleIm.getRow(i));
						}
					}
					throw new UnsupportedOperationException();
				};
			}
			
			protected boolean areEqual(double[] arr0, double[] arr1) {
				int n = Math.min(arr0.length, arr1.length);
				for(int i = 0; i < n; i++) {
					if(Math.abs(arr0[i] - arr1[i]) > 1e-8) {
						return false;
					}
				}
				return true;
			}
			
		});
	}

}
