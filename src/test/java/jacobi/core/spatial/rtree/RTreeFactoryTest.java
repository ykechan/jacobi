package jacobi.core.spatial.rtree;

import java.awt.Color;
import java.io.IOException;
import java.util.Comparator;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import jacobi.api.Matrix;
import jacobi.core.spatial.sort.Fractal2D;
import jacobi.core.spatial.sort.FractalSort2D;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.util.JacobiJUnit4ClassRunner;
import jacobi.test.util.JacobiSvg;

@JacobiImport("/jacobi/test/data/RTreeFactoryTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class RTreeFactoryTest {
	
	@JacobiInject(0)
	public Matrix input;
	
	@Test
	@JacobiImport("Random Scatters")
	public void shouldBeAbleToBuildXAlignTreeOnRandomScatters() throws IOException {
		RNode<Integer> root = this.sortBy(0).createNode(this.input);
		int depth = this.verify(null, root);
		JacobiSvg svg = this.render(new JacobiSvg(), root, 0, 0, depth - 2);
		this.render(svg, this.input, 0, 1).exportTo(null);
	}
	
	@Test
	@JacobiImport("Random Scatters")
	public void shouldBeAbleToBuildYAlignTreeOnRandomScatters() throws IOException {
		RNode<Integer> root = this.sortBy(1).createNode(this.input);
		int depth = this.verify(null, root);
		JacobiSvg svg = this.render(new JacobiSvg(), root, 0, 0, depth - 1);
		this.render(svg, this.input, 0, 1).exportTo(null);
	}
	
	@Test
	@JacobiImport("Random Scatters")
	public void shouldBeAbleToBuildHilbertSortTreeOnRandomScatters() throws IOException {
		RNode<Integer> root = new RTreeFactory(
				new FractalSort2D(0, 1, Fractal2D.HILBERT),
				new RAdaptivePacker(() -> 1.0),
				2, 3
			)
			.createNode(this.input);
		int depth = this.verify(null, root);
		JacobiSvg svg = this.render(new JacobiSvg(), root, 0, 0, depth - 1);
		this.render(svg, this.input, 0, 1).exportTo(null);
	}
	
	protected RTreeFactory sortBy(int x){
		return new RTreeFactory(
			(ls) -> IntStream.range(0, ls.size())
				.boxed()
				.sorted(Comparator.comparingDouble(i -> ls.get(i)[x]))
				.mapToInt(Integer::intValue)
				.toArray()
			,
			(ls, min) -> Math.min(min, ls.size()),
			3, 12
		);
	}
	
	protected JacobiSvg render(JacobiSvg svg, RObject<?> node, int depth, int index) {
		return this.render(svg, node, depth, index, -1);
	}
	
	protected JacobiSvg render(JacobiSvg svg, RObject<?> node, int depth, int index, int target) {
		String tab = "";
		for(int i = 0; i < depth; i++) {
			tab += "    ";
		}
		
		Aabb aabb = node.minBoundBox();
		if(target < 0 || depth == target) {
			svg.rect(aabb.min(0), aabb.min(1), 
				aabb.max(0) - aabb.min(0), 
				aabb.max(1) - aabb.min(1),
				Color.GREEN).text(tab + "#" + depth + "," + index, aabb.min(0), aabb.min(1), Color.GRAY);
		}
		
		int k = 0;
		for(RObject<?> n : node.nodes()) {
			this.render(svg, n, depth + 1, k++, target);
		}
		return svg;
	}
	
	protected JacobiSvg render(JacobiSvg svg, Matrix data, int x, int y) {
		for(int i = 0; i < data.getRowCount(); i++) {
			double[] p = data.getRow(i);
			svg.dot(p[x], p[y], Color.RED)
				.text(p[x] + "," + p[y], p[x], p[y], Color.BLUE);
		}
		return svg;
	}
	
	protected int verify(RObject<Integer> parent, RObject<Integer> node) {
		Aabb mbb = node.minBoundBox();
		for(int i = 0; i < mbb.dim(); i++) {
			// mbb must be valid
			Assert.assertFalse(mbb.min(i) > mbb.max(i));
		}
		
		if(parent != null) {
			Assert.assertEquals(mbb.dim(), parent.minBoundBox().dim());
			for(int i = 0; i < mbb.dim(); i++) {
				// mbb must be within parent
				Assert.assertFalse(mbb.min(i) < parent.minBoundBox().min(i));
				Assert.assertFalse(mbb.max(i) > parent.minBoundBox().max(i));
			}
		}
		
		if(node.nodes().isEmpty()){
			double[] vector = this.input.getRow(node.get()
				.orElseThrow(() -> new IllegalStateException("Leaf " + node + " has no payload.")));
			
			if(parent != null) {
				Assert.assertEquals(vector.length, parent.minBoundBox().dim());
				for(int i = 0; i < vector.length; i++) {
					Assert.assertFalse(vector[i] < parent.minBoundBox().min(i));
					Assert.assertFalse(vector[i] > parent.minBoundBox().max(i));
				}
			}
			
			return 0;
		}
			
		RObject<Integer> prev = null;
		int depth = 0;
		for(RObject<Integer> n : node.nodes()) {
			Assert.assertTrue(prev == null || prev.nodes().isEmpty() == n.nodes().isEmpty());
			depth = Math.max(this.verify(node, n), depth);
			prev = n;
		}
		return depth + 1;
	}

}
