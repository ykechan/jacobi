package jacobi.api.unsupervised;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import jacobi.api.Matrix;
import jacobi.api.ext.Stats;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.util.JacobiJUnit4ClassRunner;

@RunWith(JacobiJUnit4ClassRunner.class)
@JacobiImport("/jacobi/test/data/UnsupervisedTest.xlsx")
public class UnsupervisedTest {
	
	@JacobiInject(0)
	public Matrix data;
	
	@JacobiInject(1)
	public Matrix oracle;
	
	@Test
	@JacobiImport("iris")
	public void shouldBeAbleToClusterIrisDataByKMeans() {
		// TODO: not yet implemented
	}
	
	@Test
	@JacobiImport("wine")
	public void shouldBeAbleToClusterWineDataByGMM() {
		// TODO: not yet implemented
	}
	
	@Test
	@JacobiImport("balance-scale")
	public void shouldBeAbleToClusterBalanceScaleByFullGMM() {
		// TODO: not yet implemented
	}

}
