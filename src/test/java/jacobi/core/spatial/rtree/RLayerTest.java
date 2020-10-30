package jacobi.core.spatial.rtree;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.core.impl.ImmutableMatrix;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.JacobiJUnit4ClassRunner;

@JacobiImport("/jacobi/test/data/RLayerTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class RLayerTest {
	
	@JacobiInject(0)
	public Matrix input;
	
	@JacobiInject(1)
	public Matrix groupings;
	
	@JacobiInject(2)
	public Matrix spans;
	
	@JacobiResult(10)
	public Matrix output;
	
	@JacobiResult(11)
	public Matrix outSpans;
	
	@Test
	public void shouldBeAbleToGetNumberOfDimensionAndLength() {
		int[] span = {0, 1, 1, 1, 2, 1, 3, 1}; // 4 nodes
		double[] aabbs = {
			1.0, 2.0, 3.0, 4.0,
			
			10.0, 20.0, 30.0, 40.0,
			
			50.0, 60.0, 70.0, 80.0,
			
			100.0, 200.0, 100.0, 200.0,
		}; // 2-D AABBs
		
		RLayer layer = new RLayer(span, aabbs);
		Assert.assertEquals(2, layer.dim());
		Assert.assertEquals(4, layer.length());
	}
	
	@Test
	@JacobiImport("test 2-D sort by X")
	@JacobiEquals(expected = 10, actual = 10)
	public void shouldBeAbleToGroup2DSortByX() {
		RLayer bottom = this.flat(this.input);
		int[] cover = Arrays.stream(this.groupings.getRow(0)).mapToInt(v -> (int) v).toArray();
		
		RLayer layer = RLayer.coverOf(cover, bottom);
		System.out.println(Arrays.toString(layer.bounds));
		this.output = this.toMatrix(layer);
	}
	
	@Test
	@JacobiImport("test 3-D sort by Y desc")
	@JacobiEquals(expected = 10, actual = 10)
	public void shouldBeAbleToGroup3DSortByYDesc() {
		RLayer bottom = this.flat(this.input);
		int[] cover = Arrays.stream(this.groupings.getRow(0)).mapToInt(v -> (int) v).toArray();
		
		RLayer layer = RLayer.coverOf(cover, bottom);
		System.out.println(Arrays.toString(layer.bounds));
		this.output = this.toMatrix(layer);
	}
	
	@Test
	@JacobiImport("test 4-D sort by T")
	@JacobiEquals(expected = 10, actual = 10)
	public void shouldBeAbleToGroup4DSortByT() {
		RLayer bottom = this.flat(this.input);
		int[] cover = Arrays.stream(this.groupings.getRow(0)).mapToInt(v -> (int) v).toArray();
		
		RLayer layer = RLayer.coverOf(cover, bottom);
		System.out.println(Arrays.toString(layer.bounds));
		this.output = this.toMatrix(layer);
	}
	
	@Test
	@JacobiImport("test rand 3-D AABBs")
	@JacobiEquals(expected = 10, actual = 10)
	@JacobiEquals(expected = 11, actual = 11)
	public void shouldBeAbleToGroup3DRandAabbs() {
		int[] cover = Arrays.stream(this.groupings.getRow(0)).mapToInt(v -> (int) v).toArray();
		int[] spans = Arrays.stream(this.spans.toArray()).flatMap(r -> Arrays.stream(r).boxed())
				.mapToInt(v -> v.intValue()).toArray();
		
		int[] widths = new int[2 * spans.length];
		for(int i = 0; i < widths.length; i += 2){
			widths[i] = i == 0 ? 0 : widths[i - 2] + widths[i - 1];
			widths[i + 1] = spans[i / 2];
		}
		
		RLayer base = this.ofAabbs(this.input, widths);
		RLayer parent = RLayer.coverOf(cover, base);
		this.output = this.toMatrix(parent);
		this.outSpans = Matrices.wrap(new double[][]{
			Arrays.stream(parent.spans).mapToDouble(v -> v).toArray()
		});
	}
	
	protected RLayer ofAabbs(Matrix aabbs, int[] span) {
		double[] array = new double[aabbs.getRowCount() * aabbs.getColCount()];
		for(int i = 0; i < aabbs.getRowCount(); i++){
			double[] row = aabbs.getRow(i);
			System.arraycopy(row, 0, array, i * aabbs.getColCount(), row.length);
		}
		
		return new RLayer(span, array);
	}
	
	protected RLayer flat(Matrix vectors) {
		int[] spans = new int[2 * vectors.getRowCount()];
		double[] bounds = new double[2 * vectors.getColCount() * vectors.getRowCount()];
		
		for(int i = 0; i < vectors.getRowCount(); i++){
			spans[2 * i] = i;
			spans[2 * i + 1] = 1;
			
			double[] v = vectors.getRow(i);
			double[] aabb = new double[2 * v.length];
			for(int j = 0; j < aabb.length; j++){
				aabb[j] = v[j / 2];
			}
			
			System.arraycopy(aabb, 0, bounds, (2 * vectors.getColCount()) * i, aabb.length);
		}
		
		return new RLayer(spans, bounds);
	}
	
	protected Matrix toMatrix(RLayer layer) {
		return new ImmutableMatrix(){

			@Override
			public int getRowCount() {
				return layer.length();
			}

			@Override
			public int getColCount() {
				return 2 * layer.dim();
			}

			@Override
			public double[] getRow(int index) {
				int begin = index * this.getColCount();
				int end = begin + this.getColCount();
				return Arrays.copyOfRange(layer.bounds, begin, end);
			}
			
		};
	}

}
