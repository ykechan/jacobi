package jacobi.api.classifier;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class InstanceTest {
	
	@Test
	public void shouldBeAbleToUseInstanceAsKeyInHashMap() {
		Map<Instance, Boolean> map = new HashMap<>();
		map.put(new Instance(0, 3, Math.PI), true);
		map.put(new Instance(1, 2, Math.E), false);
		
		Assert.assertTrue(map.get(new Instance(0, 3, Math.PI)));
		Assert.assertFalse(map.get(new Instance(1, 2, Math.E)));
	}
	
	@Test
	public void shouldInstanceNotEqualsToAString() {
		Assert.assertFalse(new Instance(2, 7, Math.E).equals("ABC"));
	}
	
	@Test
	public void shouldInstanceEqualsToItself() {
		Instance inst = new Instance(2, 7, Math.E);
		Assert.assertTrue(inst.equals(inst));
	}
	
	@Test
	public void shouldInstanceEqualsOnlyWhenAllPropsMatch() {
		Instance inst = new Instance(2, 7, Math.E);
		Assert.assertFalse(inst.equals(new Instance(1, 7, Math.E)));
		Assert.assertFalse(inst.equals(new Instance(2, 6, Math.E)));
		Assert.assertFalse(inst.equals(new Instance(2, 7, Math.PI)));
		Assert.assertTrue(inst.equals(new Instance(2, 7, Math.E)));
	}

}
