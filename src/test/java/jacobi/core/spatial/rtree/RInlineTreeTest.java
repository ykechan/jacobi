package jacobi.core.spatial.rtree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.core.util.MinHeap;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.JacobiJUnit4ClassRunner;

@JacobiImport("/jacobi/test/data/RInlineTreeTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class RInlineTreeTest {
	
	@JacobiInject(0)
	public Matrix input;
	
	@JacobiInject(100)
	public Matrix query;
	
	@JacobiResult(200)
	public Matrix filters;
	
	@JacobiResult(300)
	public Matrix ans;
	
	@JacobiInject(-1)
	public Map<Integer, Matrix> all;
	
	private List<RLayer> rIndex;
	
	private RLayer rLeaves;
	
	@Test
	@JacobiImport("test 2-D tree from example")
	@JacobiEquals(expected = 200, actual = 200)
	public void shouldBeAbleToQueryRangeInExampleTree() {
		List<int[]> filters = new ArrayList<>();
		RInlineTree rTree = this.buildTree((idx, lf) -> new RInlineTree(idx, lf, this.input){

			@Override
			protected int[] queryFilter(RLayer rLayer, double[] query, double maxDist, int[] filter) {
				int[] next = super.queryFilter(rLayer, query, maxDist, filter);
				filters.add(next);
				return next;
			}
			
		});

		Iterator<Integer> iter = rTree.queryRange(this.query.getRow(0), this.queryDist());
		Assert.assertTrue(iter.hasNext());
		int ans0 = iter.next();
		Assert.assertTrue(iter.hasNext());
		int ans1 = iter.next();
		Assert.assertFalse(iter.hasNext());
		
		Assert.assertEquals('c', (char) ('a' + Math.min(ans0, ans1)));
		Assert.assertEquals('m', (char) ('a' + Math.max(ans0, ans1)));
		
		this.filters = this.toMatrix(filters);
	}
	
	@Test
	@JacobiImport("test rand 2-D (32)")
	@JacobiEquals(expected = 200, actual = 200)
	@JacobiEquals(expected = 300, actual = 300)
	public void shouldBeAbleToQueryRangeInRand2D32Tree() {
		List<int[]> filters = new ArrayList<>();
		RInlineTree rTree = this.buildTree((idx, lf) -> new RInlineTree(idx, lf, this.input){

			@Override
			protected int[] queryFilter(RLayer rLayer, double[] query, double maxDist, int[] filter) {
				int[] next = super.queryFilter(rLayer, query, maxDist, filter);
				filters.add(next);
				return next;
			}
			
		});

		Iterator<Integer> iter = rTree.queryRange(this.query.getRow(0), this.queryDist());
		Assert.assertTrue(iter.hasNext());
		int ans0 = iter.next();
		Assert.assertFalse(iter.hasNext());
		
		this.filters = this.toMatrix(filters);
		this.ans = Matrices.scalar(ans0);
	}
	
	@Test
	@JacobiImport("test rand 2-D sort by Y (32)")
	@JacobiEquals(expected = 200, actual = 200)
	@JacobiEquals(expected = 300, actual = 300)
	public void shouldBeAbleToQueryRangeInRand2DSortByY32Tree() {
		List<int[]> filters = new ArrayList<>();
		RInlineTree rTree = this.buildTree((idx, lf) -> new RInlineTree(idx, lf, this.input){

			@Override
			protected int[] queryFilter(RLayer rLayer, double[] query, double maxDist, int[] filter) {
				int[] next = super.queryFilter(rLayer, query, maxDist, filter);
				filters.add(next);
				return next;
			}
			
		});

		Iterator<Integer> iter = rTree.queryRange(this.query.getRow(0), this.queryDist());
		Assert.assertTrue(iter.hasNext());
		int ans0 = iter.next();
		Assert.assertTrue(iter.hasNext());
		int ans1 = iter.next();
		Assert.assertFalse(iter.hasNext());
		
		this.filters = this.toMatrix(filters);
		this.ans = Matrices.wrap(new double[][]{new double[]{
			Math.min(ans0, ans1),
			Math.max(ans0, ans1)
		}});
	}
	
	@Test
	@JacobiImport("test rand 4-D sort by X (128)")
	@JacobiEquals(expected = 200, actual = 200)
	public void shouldBeAbleToQueryRangeInRand4DSortByX128Tree() {
		List<int[]> filters = new ArrayList<>();
		RInlineTree rTree = this.buildTree((idx, lf) -> new RInlineTree(idx, lf, this.input){

			@Override
			protected int[] queryFilter(RLayer rLayer, double[] query, double maxDist, int[] filter) {
				int[] next = super.queryFilter(rLayer, query, maxDist, filter);
				filters.add(next);
				return next;
			}
			
		});

		Iterator<Integer> iter = rTree.queryRange(this.query.getRow(0), this.queryDist());
		Assert.assertFalse(iter.hasNext());
		
		this.filters = this.toMatrix(filters);
	}
	
	@Test
	@JacobiImport("test kNN rand 3-D sort by Z")
	public void shouldBeAbleToQueryAStarInRand3DSortByZ() {
		RInlineTree rTree = this.buildTree((idx, lf) -> new RInlineTree(idx, lf, this.input));
		double[] p = this.query.getRow(0);
		int k = (int) this.query.get(1, 0);
		
		MinHeap heap = rTree.queryAStar(p, k);
	}
	
	@Test
	@JacobiImport("test 3 centroids 3-D (100)")
	public void shouldBeAbleToQueryKnnIn3Centroids3D100() {
		RInlineTree rTree = this.buildTree((idx, lf) -> new RInlineTree(idx, lf, this.input));
		double[] p = this.query.getRow(0);
		int k = (int) this.query.get(1, 0);
		
		MinHeap heap = rTree.queryAStar(p, k);
		System.out.println(Arrays.toString(heap.flush()));
	}
	
	protected double queryDist() {
		double[] q0 = this.query.getRow(0);
		double[] q1 = this.query.getRow(1);
		double dist = 0.0;
		
		for(int i = 0; i < q0.length; i++){
			double dx = q0[i] - q1[i];
			dist += dx * dx;
		}
		
		return Math.sqrt(dist);
	}
	
	protected RInlineTree buildTree(BiFunction<List<RLayer>, RLayer, RInlineTree> factoryFn) {
		RLayer leaves = this.toRLayer(this.all.get(10), true);
		List<RLayer> layers = new ArrayList<>();
		
		for(int i = 11; i < 32; i++){
			Matrix matrix = this.all.get(i);
			if(matrix == null){
				this.rLeaves = leaves;
				Collections.reverse(layers);
				this.rIndex = Collections.unmodifiableList(layers);
				return factoryFn.apply(this.rIndex, this.rLeaves);
			}
			
			RLayer rLayer = this.toRLayer(matrix, false);
			layers.add(rLayer);
		}

		throw new UnsupportedOperationException("Tree too deep");
	}
	
	protected RLayer toRLayer(Matrix input, boolean isLeaf) {
		int dim = input.getColCount() - 1;
		if(dim % 2 != 0 && !isLeaf){
			throw new IllegalArgumentException();
		}
		
		int[] cuts = new int[input.getRowCount()];
		double[] bounds = new double[cuts.length * dim];
		
		for(int i = 0; i < input.getRowCount(); i++){
			double[] row = input.getRow(i);
			cuts[i] = (int) row[0];
			
			System.arraycopy(row, 1, bounds, i * dim, dim);
		}
		return new RLayer(cuts, bounds);
	}
	
	protected Matrix toMatrix(List<int[]> filters) {
		Matrix matrix = Matrices.zeros(filters.size(), filters.stream().mapToInt(a -> a.length).max().orElse(0));
		for(int i = 0; i < matrix.getRowCount(); i++){
			int[] filter = filters.get(i);
			matrix.getAndSet(i, r -> {
				for(int j = 0; j < filter.length; j++){
					r[j] = filter[j];
				}
			});
		}
		return matrix;
	}
	
}
