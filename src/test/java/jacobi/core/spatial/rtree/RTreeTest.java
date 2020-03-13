package jacobi.core.spatial.rtree;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import jacobi.api.Matrix;
import jacobi.core.util.Ranking;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.util.JacobiJUnit4ClassRunner;
import jacobi.test.util.JacobiSvg;

@JacobiImport("/jacobi/test/data/RTreeTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class RTreeTest {
	
	@JacobiInject(0)
	public Matrix input; 
	
	@JacobiInject(1)
	public Matrix query;
	
	@JacobiInject(100)
	public Matrix oracle;
	
	@Test
	@JacobiImport("Range Oracle Rand 80x2")
	public void shouldQueryRangeResultMatchesWithOracleInRandom2D() throws IOException {
		RTree<Integer> tree = this.factory().create(input, 
			RDefaultDistances.EUCLIDEAN_SQ.againstAabb(), 
			RDefaultDistances.EUCLIDEAN_SQ.againstPoint()
		);
		
		
		for(int i = 0; i < query.getRowCount(); i++) {
			double[] q = new double[] { query.get(i, 0), query.get(i, 1) };
			Iterator<Integer> iter = tree.queryRange(q, query.get(i, 2) * query.get(i, 2));
			
			Set<Integer> set = new TreeSet<>();
			while(iter.hasNext()) {
				set.add(iter.next());
			}
			System.out.println("Query " + i);
			for(int j = 0; j < this.oracle.getRowCount(); j++) {
				Assert.assertEquals(
					"Query #" + i + ", @" + j,
					this.oracle.get(j, i) > 0.0, set.contains(j));
			}
		}		
	}
	
	@Test
	public void shouldBeAbleToByPassNodeWithMBBOutOfReach() {
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
		
		RNode<String> parent = RNode.of(Arrays.asList(ll, lu, ul, uu));
		
		AtomicInteger verify = new AtomicInteger(0);
		
		RDistance<Aabb> bDist = (aabb, q) -> aabb == ll.minBoundBox() 
				|| aabb == parent.minBoundBox()
				|| (aabb.min(0) == aabb.max(0) && aabb.min(1) == aabb.max(1)) 
				? 0.0 : 1000.0;
		
		RDistance<double[]> qDist = (p, q) -> {
			verify.incrementAndGet();
			
			if(p[0] < 1.0 && p[1] < 1.0) {
				return 0.0;
			}
						
			throw new IllegalArgumentException("Unexpect to query against " 
					+ Arrays.toString(p));
		};
		
		RTree<String> tree = new RTree<>(parent, bDist, qDist);
		List<String> ans = this.toList(tree.queryRange(new double[2], 10.0));
		
		Assert.assertEquals(4, verify.get());
		Assert.assertArrayEquals(new String[] {
			"LL #0", "LL #1", "LL #2", "LL #3" 
		}, ans.stream().sorted().toArray(n -> new String[n]));
	}
	
	@Test
	public void shouldCheckAllPointDistWhenQueryIsInsideMBB() {
		RNode<String> node = RNode.of(Arrays.asList(
			this.leaf(new double[] {-100.0, -100.0}, "LL"),
			this.leaf(new double[] {-100.0,  100.0}, "LU"),
			this.leaf(new double[] { 100.0, -100.0}, "UL"),
			this.leaf(new double[] { 100.0,  100.0}, "UU")
		));
		
		AtomicInteger verify = new AtomicInteger(0);
		
		RTree<String> tree = new RTree<>(node, (b, p) -> 0.0, (q, p) -> verify.incrementAndGet());
		List<String> ans = this.toList(tree.queryRange(new double[2], 0.1));
		
		Assert.assertTrue(ans.isEmpty());
		Assert.assertEquals(4, verify.get());
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void shouldFailWhenCollapseANonDegenerateAabb() {
		RNode<String> node = RNode.of(Arrays.asList(
			this.leaf(new double[] {-100.0, -100.0}, "LL"),
			this.leaf(new double[] {-100.0,  100.0}, "LU"),
			this.leaf(new double[] { 100.0, -100.0}, "UL"),
			this.leaf(new double[] { 100.0,  100.0}, "UU")
		));
			
		RTree<String> tree = new RTree<>(node, (b, p) -> 0.0, (q, p) -> 1.0);
		tree.toArray(node.minBoundBox());
	}	
	
	protected RTreeFactory factory() {
		return new RTreeFactory(
			v -> IntStream.range(0, v.size()).boxed()
				.sorted(Comparator.comparingDouble(i -> v.get(i)[0]))
				.mapToInt(Integer::intValue).toArray(),
			(ls, min) -> Math.min(min, ls.size()), 
			4, 2
		);
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
			public List<RObject<T>> nodeList() {
				return Collections.emptyList();
			}
			
		};
	}
	
	protected <T> List<T> toList(Iterator<T> iter) {
		List<T> items = new ArrayList<>();
		while(iter.hasNext()){
			items.add(iter.next());
		}
		
		return items;
	}

}
