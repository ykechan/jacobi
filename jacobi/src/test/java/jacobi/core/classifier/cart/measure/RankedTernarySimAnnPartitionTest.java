package jacobi.core.classifier.cart.measure;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import jacobi.api.classifier.Instance;

public class RankedTernarySimAnnPartitionTest {
	
	@Test
	public void shouldBeAbleToReturnNoSplitWeightNaNOnPure() {
		
	}
	
	@Test
	public void shouldBeAbleToClusterGroupedOutcome() {
		List<Instance> clusters = new RankedTernarySimAnnPartition(Impurity.ENTROPY)
			.clusterize(Arrays.asList(
				new Instance(0, 1, 2.0),
				new Instance(1, 1, 3.0),
				new Instance(2, 1, 4.0),
				new Instance(3, 0, 5.3),
				new Instance(4, 0, 5.7),
				new Instance(5, 2, 1.0),
				new Instance(6, 2, 1.1),
				new Instance(7, 2, 1.2)
			)); 
		
		Assert.assertEquals(3, clusters.size());
		Assert.assertEquals(3, clusters.get(0).feature);
		Assert.assertEquals(1, clusters.get(0).outcome);
		Assert.assertEquals(9.0, clusters.get(0).weight, 1e-12);
		Assert.assertEquals(2, clusters.get(1).feature);
		Assert.assertEquals(0, clusters.get(1).outcome);
		Assert.assertEquals(11.0, clusters.get(1).weight, 1e-12);
		Assert.assertEquals(3, clusters.get(2).feature);
		Assert.assertEquals(2, clusters.get(2).outcome);
		Assert.assertEquals(3.3, clusters.get(2).weight, 1e-12);
	}
	
	@Test
	public void shouldBeAbleToClusterAltOutcome() {
		List<Instance> alt = Arrays.asList(
			new Instance(7, 0, 2.0),
			new Instance(6, 1, 3.0),
			new Instance(5, 2, 4.0),
			new Instance(4, 3, 5.3),
			new Instance(3, 4, 5.7),
			new Instance(2, 5, 1.0),
			new Instance(1, 6, 1.1),
			new Instance(0, 7, 1.2)
		); 
		
		List<Instance> clusters = new RankedTernarySimAnnPartition(Impurity.ENTROPY)
				.clusterize(alt);
		
		Assert.assertEquals(alt.size(), clusters.size());
		for(int i = 0; i < alt.size(); i++) {
			Assert.assertEquals(1, clusters.get(i).feature);
			Assert.assertEquals(alt.get(i).outcome, clusters.get(i).outcome);
			Assert.assertEquals(alt.get(i).weight, clusters.get(i).weight, 1e-12);
		}
	}
	
	@Test
	public void shouldBeAbleToClusterReAppearingOutcome() {
		List<Instance> clusters = new RankedTernarySimAnnPartition(Impurity.ENTROPY)
			.clusterize(Arrays.asList(
				new Instance(0, 1, 2.0),
				new Instance(1, 1, 3.0),
				new Instance(2, 1, 4.0),
				new Instance(3, 0, 5.3),
				new Instance(4, 0, 5.7),
				new Instance(5, 1, 1.0),
				new Instance(6, 1, 1.1),
				new Instance(7, 2, 1.2)
			)); 
		
		Assert.assertEquals(4, clusters.size());
		Assert.assertEquals(3, clusters.get(0).feature);
		Assert.assertEquals(1, clusters.get(0).outcome);
		Assert.assertEquals(9.0, clusters.get(0).weight, 1e-12);
		Assert.assertEquals(2, clusters.get(1).feature);
		Assert.assertEquals(0, clusters.get(1).outcome);
		Assert.assertEquals(11.0, clusters.get(1).weight, 1e-12);
		Assert.assertEquals(2, clusters.get(2).feature);
		Assert.assertEquals(1, clusters.get(2).outcome);
		Assert.assertEquals(2.1, clusters.get(2).weight, 1e-12);
		Assert.assertEquals(1, clusters.get(3).feature);
		Assert.assertEquals(2, clusters.get(3).outcome);
		Assert.assertEquals(1.2, clusters.get(3).weight, 1e-12);
	}

}
