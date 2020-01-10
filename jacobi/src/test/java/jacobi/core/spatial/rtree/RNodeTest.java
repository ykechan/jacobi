package jacobi.core.spatial.rtree;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

public class RNodeTest {
	
	@Test
	public void shouldBeAbleToConstructDegenerateRNode() {
		double[] q = {Math.E, Math.PI, 777.9, 8080.1};
		RNode<Void> node = RNode.of(Arrays.asList(this.leaf(q, null)));
		
		Assert.assertEquals(4, node.minBoundBox().dim());
		Assert.assertEquals(Math.E, node.minBoundBox().min(0), 1e-12);
		Assert.assertEquals(Math.E, node.minBoundBox().max(0), 1e-12);
		
		Assert.assertEquals(Math.PI, node.minBoundBox().min(1), 1e-12);
		Assert.assertEquals(Math.PI, node.minBoundBox().max(1), 1e-12);
		
		Assert.assertEquals(777.9, node.minBoundBox().min(2), 1e-12);
		Assert.assertEquals(777.9, node.minBoundBox().max(2), 1e-12);
		
		Assert.assertEquals(8080.1, node.minBoundBox().min(3), 1e-12);
		Assert.assertEquals(8080.1, node.minBoundBox().max(3), 1e-12);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldFailWhenNoNodeListGiven() {
		RNode.of(null);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldFailWhenNodeListGivenIsEmpty() {
		RNode.of(Collections.emptyList());
	}
	
	protected <T> RObject<T> leaf(double[] point, T item) {
		double[] q = Arrays.copyOf(point, point.length);
		return new RObject<T>() {

			@Override
			public Optional<T> get() {
				return Optional.of(item);
			}

			@Override
			public Aabb minBoundBox() {
				return Aabb.wrap(q);
			}

			@Override
			public List<RObject<T>> nodes() {
				return Collections.emptyList();
			}
			
		};
	}
	
	protected void toSvg(RNode<?> node, int x, int y, PrintStream out) {
		int width = 1920;
		int height = 1200;
		out.println("<svg>");
		out.println("</svg>");
	}

}
