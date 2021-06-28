package jacobi.core.classifier.svm;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import jacobi.api.Matrix;
import jacobi.api.classifier.Column;
import jacobi.api.classifier.svm.SupportVectorMachine;
import jacobi.api.classifier.svm.SupportVectorMachineParams;
import jacobi.api.classifier.svm.SupportVectorMachines;
import jacobi.api.ext.Learn;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.util.JacobiJUnit4ClassRunner;

@JacobiImport("/jacobi/test/data/SupportVectorMachineLearnerTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class SupportVectorMachineLearnerTest {
	
	@JacobiInject(0)
	public Matrix input;
	
	@JacobiInject(1)
	public Matrix props;
	
	@JacobiInject(10)
	public Matrix error;
	
	@Test
	@JacobiImport("test mixed gaussian")
	public void shouldBeAbleToLearnSVMFromMixedGauss() {
		double lambda = this.props.get(0, 0);
		SupportVectorMachines<Boolean> svms = this.input.ext(Learn.class)
			.classify(Arrays.asList(Column.numeric(1), Column.numeric(2)), Column.signed(0))
			.learnSVM(SupportVectorMachineParams.ofDefault(lambda));
		
		double error = this.error(svms.getSVM(true), Column.signed(0));
		
		Assert.assertTrue(error < this.error.get(0, 0));
	}
	
	@Test
	@JacobiImport("test linear inseparable")
	public void shouldBeAbleToLearnSVMFromLinearInseparable() {
		double lambda = this.props.get(0, 0);
		SupportVectorMachines<Boolean> svms = this.input.ext(Learn.class)
			.classify(Arrays.asList(Column.numeric(1), Column.numeric(2)), Column.signed(0))
			.learnSVM(SupportVectorMachineParams.ofDefault(lambda));
		
		double error = this.error(svms.getSVM(true), Column.signed(0));
		Assert.assertTrue(error < this.error.get(0, 0));
	}
	
	@Test
	@JacobiImport("test linear inseparable (2)")
	public void shouldBeAbleToLearnSVMFromLinearInseparable2() {
		SupportVectorMachines<Boolean> svms = this.input.ext(Learn.class)
			.classify(Arrays.asList(Column.numeric(1), Column.numeric(2)), Column.signed(0))
			.learnSVM(SupportVectorMachineParams.ofDefault(0.1));
		
		double error = this.error(svms.getSVM(true), Column.signed(0));
		Assert.assertTrue(error < this.error.get(0, 0));
	}

	protected double error(SupportVectorMachine svm, Column<Boolean> outCol) {
		double err = 0.0;
		for(int i = 0; i < this.input.getRowCount(); i++){
			double[] v = this.input.getRow(i);
			double w = svm.applyAsDouble(v);
			int sgn = outCol.valueOf(v[outCol.getIndex()]) ? 1 : -1;
			
			err += Math.max(0, 1 - sgn * w);
		}
		
		
		return err;
	}
	
}
