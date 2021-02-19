package jacobi.core.clustering;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.api.spatial.SpatialIndex;
import jacobi.core.clustering.MeanShift.Context;
import jacobi.core.clustering.MeanShift.Tuple;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.util.Jacobi;
import jacobi.test.util.JacobiJUnit4ClassRunner;
import jacobi.test.util.JacobiSvg;

@JacobiImport("/jacobi/test/data/MeanShiftTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class MeanShiftTest {
	
	@JacobiInject(0)
	public Matrix data;
	
	@JacobiInject(1)
	public Matrix props;
	
	@JacobiInject(100)
	public Matrix oracle;
	
	@JacobiInject(-1)
	public Map<Integer, Matrix> all;
	
	@Test
	@JacobiImport("test rand gauss 30x2")
	public void shouldBeAbleToTraceRandGauss30x2Step0(){
		MeanShift impl = this.mock(ParzenWindow.FLAT);
		int startAt = this.indexOf(this.props.getRow(0));
		
		Context context = impl.initContext(this.data);
		Tuple start = new Tuple(startAt, this.data.getRow(startAt));
		
		int ans = impl.trace(impl.createIndex(this.data), context, start).index;
		Assert.assertEquals(startAt, ans);
	}
	
	@Test
	@JacobiImport("test rand gauss 30x2 (2)")
	public void shouldBeAbleToStopTraceWhenDensityIsLowerThenThreshold() {
		MeanShift impl = this.mock(ParzenWindow.FLAT);
		int startAt = this.indexOf(this.props.getRow(0));
		
		Context context = impl.initContext(this.data);
		Tuple start = new Tuple(startAt, this.data.getRow(startAt));
		
		int ans = impl.trace(impl.createIndex(this.data), context, start).index;
		Assert.assertEquals(-1, ans);
	}
	
	@Test
	@JacobiImport("test rand gauss 30x2 (3)")
	public void shouldBeAbleToTraceRandGauss30x2Step3() {
		MeanShift impl = this.mock(ParzenWindow.FLAT);
		int startAt = this.indexOf(this.props.getRow(0));
		
		Context context = impl.initContext(this.data);
		Tuple start = new Tuple(startAt, this.data.getRow(startAt));
		
		int ans = impl.trace(impl.createIndex(this.data), context, start).index;
		Assert.assertEquals(startAt, ans);
	}
	
	@Test
	@JacobiImport("test rand gauss 30x2 (ALL)")
	public void shouldBeAbleToTraceAllPointsInRandGauss30x2() throws IOException {
		AtomicInteger invoked = new AtomicInteger(0);
		MeanShift impl = new MeanShift(ParzenWindow.FLAT, 1.0, 0.2){

			@Override
			protected Tuple trace(SpatialIndex<Tuple> sIndex, Context context, Tuple start) {
				Tuple dest = super.trace(sIndex, context, start);
				
				Matrix oracle = all.get(100 + start.index);
				if(oracle != null){
					Assert.assertArrayEquals("#" + start.index, oracle.getRow(0), dest.vector, 1e-12);
					invoked.incrementAndGet();
				}
				
				return dest;
			}

			@Override
			protected Neighbourhood shift(SpatialIndex<Tuple> sIndex, double[] mean, Context context, int start) {
				Neighbourhood next = super.shift(sIndex, mean, context, start);
				return next;
			}
			
			
			
		};
		List<int[]> clusters = impl.compute(this.data);
		
		System.out.println("Done " + invoked.get());
		Color[] palette = new Color[]{
			Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE, Color.MAGENTA, Color.YELLOW
		}; 
		
		JacobiSvg svg = new JacobiSvg();
		for(int i = 0; i < this.data.getRowCount(); i++){
			double[] v = this.data.getRow(i);
			svg.dot(v[0], v[1], Color.BLACK);
		}
		int k = 0;
		for(int[] seq : clusters){
			Color c = palette[k++ % palette.length];
			
			for(int s : seq){
				double[] v = this.data.getRow(s);
				svg.dot(v[0], v[1], c);
			}
		}
		svg.exportTo(null);
	}
	
	@Test
	@JacobiImport("test radiation trace (1)")
	public void shouldBeAbleToTracePoint1InTestRadiation() {
		MeanShift impl = this.mock(ParzenWindow.FLAT);
		
		int startAt = this.indexOf(this.props.getRow(0));
		Tuple start = new Tuple(startAt, this.data.getRow(startAt));
		
		SpatialIndex<Tuple> sIndex = this.directQuery(this.data);
		
		Context context = impl.initContext(this.data);
		int[] before = Arrays.copyOf(context.memberships, context.memberships.length);
		
		Tuple tr = impl.trace(sIndex, context, start);
		int ans = tr.index;
		Assert.assertEquals(startAt, ans);
		this.assertCollapsed(before, start, context, sIndex);
	}
	
	@Test
	@JacobiImport("test radiation trace (2)")
	public void shouldBeAbleToTracePoint2InTestRadiation() {
		MeanShift impl = this.mock(ParzenWindow.FLAT);
		
		int startAt = this.indexOf(this.props.getRow(0));
		Tuple start = new Tuple(startAt, this.data.getRow(startAt));
		
		SpatialIndex<Tuple> sIndex = this.directQuery(this.data);
		
		Context context = impl.initContext(this.data);
		int[] before = Arrays.copyOf(context.memberships, context.memberships.length);
		
		Tuple tr = impl.trace(sIndex, context, start);
		int ans = tr.index;
		Assert.assertEquals(startAt, ans);
		this.assertCollapsed(before, start, context, sIndex);
		
		for(Map.Entry<Integer, Tuple> entry : context.cache.entrySet()){
			System.out.println("key=" + entry.getKey() 
				+ ", #" + entry.getValue().index 
				+ ":" + Arrays.toString(entry.getValue().vector));
		}
	}
	
	@Test
	@JacobiImport("test radiation trace (3)")
	public void shouldBeAbleToTracePoint3InTestRadiation() {
		MeanShift impl = this.mock(ParzenWindow.FLAT);
		
		int startAt = this.indexOf(this.props.getRow(0));
		Tuple start = new Tuple(startAt, this.data.getRow(startAt));
		
		SpatialIndex<Tuple> sIndex = this.directQuery(this.data);
		
		Context context = impl.initContext(this.data);
		int[] before = Arrays.copyOf(context.memberships, context.memberships.length);
		
		Tuple tr = impl.trace(sIndex, context, start);
		int ans = tr.index;
		Assert.assertEquals(startAt, ans);
		this.assertCollapsed(before, start, context, sIndex);
		
		for(Map.Entry<Integer, Tuple> entry : context.cache.entrySet()){
			System.out.println("key=" + entry.getKey() 
				+ ", #" + entry.getValue().index 
				+ ":" + Arrays.toString(entry.getValue().vector));
		}
	}
	
	@Test
	@JacobiImport("test radiation trace (ALL)")
	public void shouldBeAbleToTraceAllPointsInTestRadiation() {
		Context[] temp = new Context[1];
		
		double[] props = this.props == null ? new double[]{1.0, 0.0} : this.props.getRow(1);
		MeanShift impl = new MeanShift(ParzenWindow.FLAT, props[0], props[1], 3){

			@Override
			protected SpatialIndex<Tuple> createIndex(Matrix matrix) {
				return directQuery(matrix);
			}

			@Override
			protected Context initContext(Matrix matrix) {
				Context context = super.initContext(matrix);
				temp[0] = context;
				return context;
			}
			
		};
		
		List<int[]> result = impl.compute(this.data);
		for(int[] seq : result){
			Assert.assertTrue(seq.length > 0);
			Arrays.sort(seq);
			boolean lower = seq[0] == 0;
			long error = Arrays.stream(seq).filter(s -> (s < 70) != lower).count();
			Assert.assertTrue(error < 3);
		}
		
		Context context = temp[0];
		Assert.assertEquals(this.oracle.getRowCount(), context.destinations.size());
		
		double[][] array = context.destinations.stream()
			.map(t -> t.vector).sorted(Comparator.comparingDouble(a -> a[0]))
			.toArray(n -> new double[n][]);
		
		
		for(int i = 0; i < this.oracle.getRowCount(); i++){
			double[] target = this.oracle.getRow(i);
			double[] dest = array[i];
			
			Assert.assertTrue(EuclideanCluster.getInstance().distanceBetween(target, dest) < props[1]);
		}
	}
	
	@Test
	public void shouldBeAbleToCollapseMembershipOf6Elements() {
		MeanShift impl = this.mock(ParzenWindow.FLAT);
		List<int[]> seqs = impl.collapse(new int[]{
			1, 2, 2, 5, 5, 5
		});
		
		Assert.assertEquals(2, seqs.size());
		Arrays.sort(seqs.get(0));
		Arrays.sort(seqs.get(1));
		
		Assert.assertArrayEquals(new int[]{0, 1, 2}, seqs.get(0));
		Assert.assertArrayEquals(new int[]{3, 4, 5}, seqs.get(1));
		
		seqs = impl.collapse(new int[]{
			1, 3, 2, 4, 5, 5
		});
		
		Assert.assertEquals(1, seqs.size());
		Arrays.sort(seqs.get(0));
		Assert.assertArrayEquals(new int[]{0, 1, 3, 4, 5}, seqs.get(0));
		
		seqs = impl.collapse(new int[]{
			4, 4, 4, 4, 4, 4
		});
		
		Assert.assertEquals(1, seqs.size());
		Arrays.sort(seqs.get(0));
		Assert.assertArrayEquals(new int[]{0, 1, 2, 3, 4, 5}, seqs.get(0));
	}
	
	protected void assertCollapsed(int[] before, Tuple start, Context context, SpatialIndex<Tuple> sIndex) {
		double[] props = this.props == null ? new double[]{1.0, 0.0} : this.props.getRow(1);
		
		Set<Integer> passby = new TreeSet<>();
		context.cache.put(0, start);
		for(Tuple dest : context.cache.values()){
			Iterator<Tuple> iter = sIndex.queryRange(dest.vector, props[1]);
			while(iter.hasNext()){
				passby.add(iter.next().index);
			}
		}
		
		for(int i = 0; i < context.memberships.length; i++){
			int m = context.memberships[i];
			if(m < 0){
				Assert.assertFalse(passby.contains(i));
			}
			
			if(m == before[i]){
				continue;
			}
		
			Assert.assertTrue(m >= 0);
			Assert.assertTrue("#" + i + " was collapsed but not detected", passby.contains(i));
		}
	}
	
	protected MeanShift mock(ParzenWindow window) {
		double[] props = this.props == null ? new double[]{1.0, 0.0} : this.props.getRow(1);
		return new MeanShift(window, props[0], props[1], 3) {
			
			@Override
			protected Neighbourhood shift(SpatialIndex<Tuple> sIndex, double[] mean, Context context, int start) {
				Neighbourhood n = super.shift(sIndex, mean, context, start);
				int k = this.step++;
				
				Matrix oracle = all.get(offset + k);
				if(n.elements.length == 0){
					Assert.assertNull(oracle);
					return n;
				}
				
				Assert.assertNotNull("Step " + k, oracle);
				Assert.assertArrayEquals(oracle.getRow(0), n.mean, 1e-8);
				return n;
			}

			@Override
			protected SpatialIndex<Tuple> createIndex(Matrix matrix) {
				return directQuery(matrix);
			}
			
			private int step = 0;
			private int offset = 10;
		};
	}
	
	protected SpatialIndex<Tuple> directQuery(Matrix matrix) {		
		return new SpatialIndex<Tuple>(){

			@Override
			public List<Tuple> queryKNN(double[] query, int kMax) {
				throw new UnsupportedOperationException();
			}

			@Override
			public Iterator<Tuple> queryRange(double[] query, double dist) {
				
				
				ClusterMetric<double[]> metric = EuclideanCluster.getInstance();
				double qDist = dist * dist;
				
				List<Tuple> tuples = new ArrayList<>();
				for(int i = 0; i < matrix.getRowCount(); i++){
					double pDist = metric.distanceBetween(query, matrix.getRow(i));
					if(pDist > qDist){
						continue;
					}
					
					tuples.add(new Tuple(i, matrix.getRow(i)));
				}
				
				return tuples.iterator();
			}
			
		};
	}
	
	protected int indexOf(double[] vector) {
		for(int i = 0; i < this.data.getRowCount(); i++){
			double[] row = this.data.getRow(i);
			double dx = 0.0;
			
			for(int j = 0; j < row.length; j++){
				dx = Math.max(dx, Math.abs(row[j] - vector[j]));
			}
			
			if(dx < 1e-8){
				return i;
			}
		}
		return -1;
	}

}
