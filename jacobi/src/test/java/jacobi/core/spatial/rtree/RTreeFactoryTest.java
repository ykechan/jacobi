package jacobi.core.spatial.rtree;

import java.awt.Color;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.TreeMap;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
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
		this.verify(null, root);
		JacobiSvg svg = this.render(new JacobiSvg(), root, 0, 0);
		this.render(svg, this.input, 0, 1).exportTo(null);
	}
	
	@Test
	@JacobiImport("Random Scatters")
	public void shouldBeAbleToBuildYAlignTreeOnRandomScatters() throws IOException {
		RNode<Integer> root = this.sortBy(1).createNode(this.input);
		this.verify(null, root);
		JacobiSvg svg = this.render(new JacobiSvg(), root, 0, 0);
		this.render(svg, this.input, 0, 1).exportTo(null);
	}
	
	protected RTreeFactory sortBy(int x){
		return new RTreeFactory(
			(ls, min) -> Math.min(min, ls.size())
		);
	}
	
	protected JacobiSvg render(JacobiSvg svg, RObject<?> node, int depth, int index) {
		String tab = "";
		for(int i = 0; i < depth; i++) {
			tab += "    ";
		}
		
		Aabb aabb = node.minBoundBox();
		svg.rect(aabb.min(0), aabb.min(1), 
			aabb.max(0) - aabb.min(0), 
			aabb.max(1) - aabb.min(1),
			Color.GREEN).text(tab + "#" + depth + "," + index, aabb.min(0), aabb.min(1), Color.GRAY);
		
		int k = 0;
		for(RObject<?> n : node.nodes()) {
			this.render(svg, n, depth + 1, k++);
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
	
	protected void verify(RObject<Integer> parent, RObject<Integer> node) {
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
			
			return;
		}
		
		for(RObject<Integer> n : node.nodes()) {
			this.verify(node, n);
		}
	}

}
