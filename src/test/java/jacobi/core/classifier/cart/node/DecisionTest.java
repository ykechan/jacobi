package jacobi.core.classifier.cart.node;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class DecisionTest {
	
	@Test
	public void shouldBeAbleToGetBackDecisionItem() {
		Assert.assertEquals(42, new Decision<>(42).decide().intValue());
		Assert.assertEquals("This is a text", new Decision<>("This is a text").decide());
				
	}
	
	@Test
	public void shouldDecisionDoesNotHaveSplit() {
		Assert.assertNull(new Decision<>(42).split());
		Assert.assertNull(new Decision<>("ABC").split());
		Assert.assertNull(new Decision<>(new Object()).split());
		
		Assert.assertFalse(new Decision<>(42).decide(7).isPresent());
		Assert.assertFalse(new Decision<>("String.").decide(new double[] {1, 2, 3}).isPresent());
	}
	
	@Test
	public void shouldBeAbleToUseDecisionInHashSet() {
		Set<Decision<String>> set = new HashSet<>();
		set.add(new Decision<>("John Wayne"));
		set.add(new Decision<>("Bruce Wayne"));
		set.add(new Decision<>("John Locke"));
		set.add(new Decision<>("Johanne Sebastian Bach"));
		
		Assert.assertTrue(set.contains(new Decision<>("Bruce Wayne")));
		Assert.assertTrue(set.contains(new Decision<>("Johanne Sebastian Bach")));
		Assert.assertTrue(set.contains(set.iterator().next()));
		Assert.assertFalse(set.contains(new Decision<>(42)));
		Assert.assertFalse(set.contains(new Decision<>("John Wayne Gacy")));
	}

}
