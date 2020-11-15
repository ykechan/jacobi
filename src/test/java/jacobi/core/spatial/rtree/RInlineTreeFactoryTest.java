package jacobi.core.spatial.rtree;

import java.awt.Color;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import jacobi.api.Matrix;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.util.JacobiJUnit4ClassRunner;
import jacobi.test.util.JacobiSvg;

@JacobiImport("/jacobi/test/data/RInlineTreeFactoryTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class RInlineTreeFactoryTest {
	
	@JacobiInject(0)
	public Matrix input;
	
	@Test
	@JacobiImport("test rand 2-D scatter 128")
	public void shouldBeAbleToSerializeRand2D128DataSortingByX() {
		RInlineTreeFactory factory = new RInlineTreeFactory(
			ls -> this.sortBy(ls, 0),
			this.nodePacker(5),
			this.vectorPacker(10)
		);
		
		RLayer base = factory.serialize(this.input, false);
		Assert.assertEquals(this.input.getRowCount(), base.cuts.length);
		for(int i = 1; i < base.cuts.length; i++){
			double[] curr = this.input.getRow(base.cuts[i]);
			double[] prev = this.input.getRow(base.cuts[i - 1]);
			
			Assert.assertTrue(prev[0] <= curr[0]);
		}
		
		Assert.assertEquals(0, base.bounds.length);
	}
	
	@Test
	@JacobiImport("test rand 2-D scatter 128")
	public void shouldBeAbleToInlineRand2D128DataSortingByX() {
		RInlineTreeFactory factory = new RInlineTreeFactory(
			ls -> this.sortBy(ls, 0),
			this.nodePacker(5),
			this.vectorPacker(10)
		);
		
		RLayer base = factory.serialize(this.input, true);
		Assert.assertEquals(this.input.getRowCount(), base.cuts.length);
		for(int i = 1; i < base.cuts.length; i++){
			double[] curr = this.input.getRow(base.cuts[i]);
			double[] prev = this.input.getRow(base.cuts[i - 1]);
			
			Assert.assertTrue(prev[0] <= curr[0]);
		}
		
		Assert.assertEquals(this.input.getRowCount() * this.input.getColCount(), 
				base.bounds.length);
		
		for(int i = 0; i < base.cuts.length; i++){
			double[] vector = this.input.getRow(base.cuts[i]);
			Assert.assertArrayEquals(vector, 
				Arrays.copyOfRange(base.bounds, i * this.input.getColCount(), (i + 1) * this.input.getColCount()), 
				1e-12);
		}
	}
	
	@Test
	@JacobiImport("test rand 2-D scatter 128")
	public void shouldBeAbleToBuildHierarchyRand2D128DataSortingByXAndExportSvg() throws IOException {
		RInlineTreeFactory factory = new RInlineTreeFactory(
			ls -> this.sortBy(ls, 0),
			this.nodePacker(5),
			this.vectorPacker(10)
		);
		
		List<RLayer> hierarchy = factory.buildHierarchy(this.input, true);
		
		Assert.assertEquals(3, hierarchy.size());
		
		RLayer root = hierarchy.get(0);
		RLayer nodes = hierarchy.get(1);
		RLayer base = hierarchy.get(2);
		
		Assert.assertEquals(2, nodes.dim());
		Assert.assertEquals(2, root.dim());
		
		Assert.assertEquals(128, base.cuts.length);
		Assert.assertEquals(13, nodes.length());
		Assert.assertEquals(3, root.length());
		
		JacobiSvg svg = new JacobiSvg();
		this.toSvg(svg, this.input, 0, 1);
		this.toSvg(svg, nodes, 0, 1, Color.BLUE);
		this.toSvg(svg, root, 0, 1, Color.RED);
		svg.exportTo(null);
	}
	
	@Test
	@JacobiImport("test rand 2-D scatter 128")
	public void shouldBeAbleToBuildHierarchyRand2D128DataSortingByXAndMBBsAreOrderByX() throws IOException {
		RInlineTreeFactory factory = new RInlineTreeFactory(
			ls -> this.sortBy(ls, 0),
			this.nodePacker(5),
			this.vectorPacker(10)
		);
		
		List<RLayer> hierarchy = factory.buildHierarchy(this.input, true);
		
		Assert.assertEquals(3, hierarchy.size());
		
		RLayer root = hierarchy.get(0);
		RLayer nodes = hierarchy.get(1);
		
		int stride = 2 * nodes.dim();
		
		for(int i = stride; i < nodes.bounds.length; i += stride){
			Assert.assertTrue(nodes.bounds[i - stride + 1] <= nodes.bounds[i]);
		}
		
		for(int i = stride; i < root.bounds.length; i += stride){
			Assert.assertTrue(root.bounds[i - stride + 1] <= root.bounds[i]);
		}
	}
	
	@Test
	@JacobiImport("test rand 2-D scatter 128")
	public void shouldBeAbleToBuildHierarchyRand2D128DataSortingByX() throws IOException {
		RInlineTreeFactory factory = new RInlineTreeFactory(
				ls -> this.sortBy(ls, 0),
				this.nodePacker(5),
				this.vectorPacker(10)
			);
			
		List<RLayer> hierarchy = factory.buildHierarchy(this.input, true);
			
		Assert.assertEquals(3, hierarchy.size());
			
		RLayer root = hierarchy.get(0);
		RLayer nodes = hierarchy.get(1);
		
		int stride = 2 * root.dim();
		
		for(int i = 0; i < root.bounds.length; i+=stride){
			double minX = root.bounds[i];
			double maxX = root.bounds[i + 1];
			double minY = root.bounds[i + 2];
			double maxY = root.bounds[i + 3];
			
			int begin = root.cuts[2 * (i / stride)];
			int span = root.cuts[2 * (i / stride) + 1];
			int end = begin + span;
			
			for(int j = begin; j < end; j++){
				double x0 = nodes.bounds[j * stride];
				double x1 = nodes.bounds[j * stride + 1];
				double y0 = nodes.bounds[j * stride + 2];
				double y1 = nodes.bounds[j * stride + 3];
				
				Assert.assertFalse(x0 < minX || x0 > maxX || x1 < minX || x1 > maxX);
				Assert.assertFalse(y0 < minY || y0 > maxY || y1 < minY || y1 > maxY);
			}
		}
		
		RLayer base = hierarchy.get(2);
		for(int i = 0; i < nodes.bounds.length; i+=stride){
			double minX = nodes.bounds[i];
			double maxX = nodes.bounds[i + 1];
			double minY = nodes.bounds[i + 2];
			double maxY = nodes.bounds[i + 3];
			
			int begin = nodes.cuts[2 * (i / stride)];
			int span = nodes.cuts[2 * (i / stride) + 1];
			int end = begin + span;
			
			for(int j = begin; j < end; j++){
				double x = base.bounds[2 * j];
				double y = base.bounds[2 * j + 1];
				
				Assert.assertFalse(x < minX || x > maxX);
				Assert.assertFalse(y < minY || y > maxY);
			}
		}
	}
	
	protected JacobiSvg toSvg(JacobiSvg svg, RLayer rLayer, 
			int dimX, int dimY, 
			Color color) {
		int numDim = rLayer.dim();
		for(int i = 0; i < rLayer.length(); i++){
			int base = 2 * i * numDim;
			
			double minX = rLayer.bounds[base + 2 * dimX];
			double maxX = rLayer.bounds[base + 2 * dimX + 1];
			
			double minY = rLayer.bounds[base + 2 * dimY];
			double maxY = rLayer.bounds[base + 2 * dimY + 1];
			
			svg.rect(minX, minY, maxX - minX, maxY - minY, color);
		}
		return svg;
	}
	
	protected JacobiSvg toSvg(JacobiSvg svg, Matrix matrix, int dimX, int dimY) {
		for(int i = 0; i < matrix.getRowCount(); i++){
			double[] vector = matrix.getRow(i);
			svg.dot(vector[dimX], vector[dimY], Color.GREEN);
		}
		return svg;
	}
	
	protected int[] sortBy(List<double[]> vectors, int index) {
		if(index < 0){
			return IntStream.range(0, vectors.size()).toArray();
		}
		
		return IntStream.range(0, vectors.size()).boxed()
			.sorted(Comparator.comparingDouble(i -> vectors.get(i)[index]))
			.mapToInt(Integer::intValue).toArray();
	}

	protected Function<RLayer, RLayer> nodePacker(int len) {
		return r -> {
			int[] spans = new int[(r.length() / len) + (r.length() % len == 0 ? 0 : 1)];
			int left = r.length();
			for(int i = 0; i < spans.length; i++){
				spans[i] = Math.min(len, left);
				left -= spans[i];
			}
			return RLayer.coverOf(spans, r);
		};
	}
	
	protected Function<List<double[]>, RLayer> vectorPacker(int len) {
		return r -> {
			int[] spans = new int[(r.size() / len) + (r.size() % len == 0 ? 0 : 1)];
			int left = r.size();
			for(int i = 0; i < spans.length; i++){
				spans[i] = Math.min(len, left);
				left -= spans[i];
			}
			return RLayer.coverOf(spans, r);
		};
	}
}
