package jacobi.core.spatial.rtree;

import java.awt.Color;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import jacobi.test.util.JacobiSvg;

public class RNodeTest {
	
	@Test
	public void shouldBeAbleToConstructRNodeOn4Corners() {
		RNode<String> node = RNode.of(Arrays.asList(
			this.leaf(new double[] {-9, -8}, "Lower, Lower"),
			this.leaf(new double[] {-6,  7}, "Lower, Upper"),
			this.leaf(new double[] { 6, -7}, "Upper, Lower"),
			this.leaf(new double[] { 8,  7}, "Upper, Upper")
		));
		
		Assert.assertFalse(node.get().isPresent());
		Assert.assertEquals(-9.0, node.minBoundBox().min(0), 1e-12);
		Assert.assertEquals(-8.0, node.minBoundBox().min(1), 1e-12);
		Assert.assertEquals( 8.0, node.minBoundBox().max(0), 1e-12);
		Assert.assertEquals( 7.0, node.minBoundBox().max(1), 1e-12);
		
		Assert.assertEquals(4, node.nodes().size());
		Assert.assertEquals("Lower, Lower", node.nodes().get(0).get().get());
		Assert.assertEquals("Lower, Upper", node.nodes().get(1).get().get());
		Assert.assertEquals("Upper, Lower", node.nodes().get(2).get().get());
		Assert.assertEquals("Upper, Upper", node.nodes().get(3).get().get());
	}
	
	@Test
	public void shouldBeAbleToConstruct2LevelRNode() throws IOException {
		RNode<String> ll = RNode.of(Arrays.asList(
			this.leaf(new double[] {0.46, 0.12}, "LL #0"),
			this.leaf(new double[] {0.91, 0.78}, "LL #1"),
			this.leaf(new double[] {0.23, 0.43}, "LL #2"),
			this.leaf(new double[] {0.57, 0.89}, "LL #3")
		));
		
		RNode<String> lu = RNode.of(Arrays.asList(
			this.leaf(new double[] {0.46, 10.12}, "LU #0"),
			this.leaf(new double[] {0.91, 10.78}, "LU #1"),
			this.leaf(new double[] {0.23, 10.43}, "LU #2"),
			this.leaf(new double[] {0.57, 10.89}, "LU #3")
		));
		
		RNode<String> ul = RNode.of(Arrays.asList(
			this.leaf(new double[] {10.46, 0.12}, "UL #0"),
			this.leaf(new double[] {10.91, 0.78}, "UL #1"),
			this.leaf(new double[] {10.23, 0.43}, "UL #2"),
			this.leaf(new double[] {10.57, 0.89}, "UL #3")
		));
		
		RNode<String> uu = RNode.of(Arrays.asList(
			this.leaf(new double[] {10.46, 10.12}, "UU #0"),
			this.leaf(new double[] {10.91, 10.78}, "UU #1"),
			this.leaf(new double[] {10.23, 10.43}, "UU #2"),
			this.leaf(new double[] {10.57, 10.89}, "UU #3")
		));
		
		RNode<String> node = RNode.of(Arrays.asList(ll, lu, ul, uu));
		Assert.assertEquals( 0.23, node.minBoundBox().min(0), 1e-12);
		Assert.assertEquals( 0.12, node.minBoundBox().min(1), 1e-12);
		Assert.assertEquals(10.91, node.minBoundBox().max(0), 1e-12);
		Assert.assertEquals(10.89, node.minBoundBox().max(1), 1e-12);
	}
	
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
	
	protected JacobiSvg render(RObject<?> node, int depth, JacobiSvg svg) {
		
		
		if(node.get().isPresent()){
			Object item = node.get().get();
			svg.dot(
				node.minBoundBox().min(0), 
				node.minBoundBox().min(1), 
				Color.RED)
				.text(item == null ? "" : item.toString(), 
				node.minBoundBox().min(0), 
				node.minBoundBox().min(1), 
				Color.BLUE);
			return svg;
		}
		
		svg.rect(
			node.minBoundBox().min(0), 
			node.minBoundBox().min(1), 
			node.minBoundBox().max(0) - node.minBoundBox().min(0), 
			node.minBoundBox().max(1) - node.minBoundBox().min(1),
			Color.GRAY);
		
		for(RObject<?> child : node.nodes()) {
			this.render(child, depth + 1, svg);
		}
		return svg;
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
}
