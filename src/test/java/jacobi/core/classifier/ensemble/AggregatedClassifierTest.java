package jacobi.core.classifier.ensemble;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import jacobi.api.classifier.Classifier;
import jacobi.core.classifier.cart.node.Decision;
import jacobi.core.util.Weighted;

public class AggregatedClassifierTest {
	
	@Test
	public void shouleBeAbleToSelectMajorityWhenAllAreEqual() {
		Classifier<Boolean> model = new AggregatedClassifier<>(Arrays.asList(
			new Weighted<>(new Decision<>(true), 1.0),
			new Weighted<>(new Decision<>(false), 1.0),
			new Weighted<>(new Decision<>(true), 1.0),
			new Weighted<>(new Decision<>(false), 1.0),
			new Weighted<>(new Decision<>(true), 1.0)
		));
		
		Assert.assertTrue(model.apply(new double[0]));
		Assert.assertTrue(model.apply(new double[] {1.0}));
		Assert.assertTrue(model.apply(new double[] {3.0, 0.0}));
	}
	
	@Test
	public void shouleBeAbleToSelectTheOverwhelmImportantModel() {
		Classifier<Boolean> model = new AggregatedClassifier<>(Arrays.asList(
			new Weighted<>(new Decision<>(true), 1.0),
			new Weighted<>(new Decision<>(false), 1.0),
			new Weighted<>(new Decision<>(true), 1.0),
			new Weighted<>(new Decision<>(false), 10.0),
			new Weighted<>(new Decision<>(true), 1.0)
		));
			
		Assert.assertFalse(model.apply(new double[0]));
		Assert.assertFalse(model.apply(new double[] {Math.PI}));
		Assert.assertFalse(model.apply(new double[] {3.0, 0.0}));
	}
	
	@Test
	public void shouleBeAbleToSelectOverrideOverwhelmImportantModelBySumOfLesserImportantModels() {
		
		Classifier<Boolean> model = new AggregatedClassifier<>(Arrays.asList(
			new Weighted<>(new Decision<>(true), 5.0),
			new Weighted<>(new Decision<>(false), 1.0),
			new Weighted<>(new Decision<>(true), 6.0),
			new Weighted<>(new Decision<>(false), 10.0),
			new Weighted<>(new Decision<>(true), 2.0)
		));
				
		Assert.assertTrue(model.apply(new double[0]));
		Assert.assertTrue(model.apply(new double[] {1.0, Math.E}));
		Assert.assertTrue(model.apply(new double[] {3.0, 0.0}));
	}

}
