package jacobi.api.spatial;

import java.awt.Color;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.core.spatial.rtree.RInlineTree;
import jacobi.core.spatial.rtree.RLayer;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.JacobiJUnit4ClassRunner;
import jacobi.test.util.JacobiSvg;

@RunWith(JacobiJUnit4ClassRunner.class)
@JacobiImport("/jacobi/test/data/DefaultRTreeFactoryTest.xlsx")
public class DefaultRTreeFactoryTest {
	
	@JacobiInject(0)
	public Matrix input;
	
	@JacobiInject(1)
	public Matrix query;
	
	@JacobiResult(10)
	public Matrix ans;
	
	@Test
	@JacobiImport("test ring gaussian and tilted")
	@JacobiEquals(expected = 10, actual = 10)
	public void shouldBeAbleToBuildTreeOnRingGaussianAndTilted() throws IOException, IllegalAccessException {
		SpatialIndex<Integer> index = new DefaultRTreeFactory(this.input)
			.setLeafMin(8).setLeafMax(16).build();
		Assert.assertTrue(index instanceof RInlineTree);
		
		double[] q0 = this.query.getRow(0);
		double[] q1 = this.query.getRow(1);
		
		double dist = Math.sqrt((q0[0] - q1[0]) * (q0[0] - q1[0]) + (q0[1] - q1[1]) * (q0[1] - q1[1]));
		
		Iterator<Integer> iter = index.queryRange(q0, dist);
		List<Integer> ans = new ArrayList<>();
		iter.forEachRemaining(ans::add);
		
		this.ans = Matrices.wrap(new double[][]{ ans.stream().mapToDouble(v -> v).toArray() });
		
		JacobiSvg svg = this.toSvg(new JacobiSvg(), (RInlineTree) index);
		svg.exportTo(null);
	}
	
	protected JacobiSvg toSvg(JacobiSvg svg, RInlineTree rTree) throws IllegalAccessException {
		List<RLayer> internalNodes = this.getPrivate(rTree, List.class);
		RLayer leaves = this.getPrivate(rTree, RLayer.class);
		
		float[] start = new Color(30, 150, 0).getColorComponents(new float[3]);
		float[] mid = new Color(255, 150, 0).getColorComponents(new float[3]);
		float[] finish = new Color(255, 0, 0).getColorComponents(new float[3]);
		
		for(int i = 1; i < leaves.cuts.length; i++){
			double[] u = this.input.getRow(leaves.cuts[i - 1]);
			double[] v = this.input.getRow(leaves.cuts[i]);
			
			float grad = i / (float) leaves.cuts.length;
			float[] a = start;
			float[] b = mid;
			if(grad > 0.5) {
				a = mid;
				b = finish;
				grad -= 0.5;
			}
			
			svg.line(u[0], u[1], v[0], v[1], 
				new Color(
					a[0] + 2 * grad * (b[0] - a[0]),
					a[1] + 2 * grad * (b[1] - a[1]),
					a[2] + 2 * grad * (b[2] - a[2])
				)
			);
		}
		
		for(RLayer rLayer : internalNodes){
			int mbbLen = 2 * rLayer.dim();
			for(int i = 0; i < rLayer.length(); i++){
				int begin = mbbLen * i;
				double x = rLayer.bounds[begin];
				double y = rLayer.bounds[begin + 2];
				double w = rLayer.bounds[begin + 1] - x;
				double h = rLayer.bounds[begin + 3] - y;
				svg.rect(x, y, w, h, Color.BLUE);
			}
		}
		
		for(int i = 0; i < this.input.getRowCount(); i++){
			double[] row = this.input.getRow(i);
			
			svg.dot(row[0], row[1], Color.RED);
		}
		
		return svg;
	}

	protected <T> T getPrivate(RInlineTree rTree, Class<T> clazz) throws IllegalAccessException {
		for(Field field : RInlineTree.class.getDeclaredFields()){
			if(clazz.isAssignableFrom(field.getType())){
				field.setAccessible(true);
				return (T) field.get(rTree);
			}
		}
		return null;
	}
}
