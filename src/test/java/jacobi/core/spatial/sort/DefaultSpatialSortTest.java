package jacobi.core.spatial.sort;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.core.impl.ColumnVector;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.util.JacobiJUnit4ClassRunner;

@JacobiImport("/jacobi/test/data/DefaultSpatialSortTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class DefaultSpatialSortTest {
	
	@JacobiInject(0)
	public Matrix input;
	
	@JacobiInject(10)
	public Matrix output;
	
	@Test
	public void shouldReturnEmptyMatrixWhenVectorsIsEmpty() {
		Matrix stats = DefaultSpatialSort.getInstance()
				.sampleMedianAndVar(Collections.emptyList());
		
		Assert.assertEquals(0, stats.getRowCount());
		Assert.assertEquals(0, stats.getColCount());
	}
	
	@Test
	public void shouldReturnStatsMatrixWithZeroVarsWhenOnlyOneVector() {
		Matrix stats = DefaultSpatialSort.getInstance()
				.sampleMedianAndVar(Collections.singletonList(new double[]{1.0, 2.0, 3.0, 4.0, 5.0}));
		
		Assert.assertArrayEquals(new double[]{1.0, 2.0, 3.0, 4.0, 5.0},  stats.getRow(0), 1e-12);
		Assert.assertArrayEquals(new double[]{0.0, 0.0, 0.0, 0.0, 0.0},  stats.getRow(1), 1e-12);
	}
	
	@Test
	public void shouldReturnStatsMatrixWithMidPointAndSpanForVarsWhenOnlyTwoVectors() {
		List<double[]> vectors = Arrays.asList(
			new double[]{10.0, 20.0, 30.0},
			new double[]{30.0, 10.0, 20.0}
		);
		
		Matrix stats = DefaultSpatialSort.getInstance().sampleMedianAndVar(vectors);
		Assert.assertArrayEquals(new double[]{20.0, 15.0, 25.0}, stats.getRow(0), 1e-12);
		Assert.assertArrayEquals(new double[]{20.0, 10.0, 10.0}, stats.getRow(1), 1e-12);
	}
	
	@Test
	@JacobiImport("data rand 256x12")
	public void shouldBeAbleToSortDataRand256x12() {
		List<double[]> data = this.toList(this.input);
		Random rand = new Random(Double.doubleToLongBits(Math.PI));
		SpatialSort ssort = new DefaultSpatialSort(rand::nextDouble, 13, 0.5);
		
		int[] seq = ssort.sort(data);
		for(int s : seq){
			System.out.println(s);
		}
	}
	
	@Test
	@JacobiImport("test 11-D with 3 large dim")
	public void shouldBeAbleToSort11DWith3LargeDim() {
		AtomicBoolean invoked = new AtomicBoolean(false);
		
		Random rand = new Random(Double.doubleToLongBits(Math.PI));
		SpatialSort ssort = new DefaultSpatialSort(rand::nextDouble, 
			DefaultSpatialSort.DEFAULT_SAMPLING_SIZE, 
			DefaultSpatialSort.DEFAULT_R_SQUARE
		) {

			@Override
			protected SpatialSort getSortingFunction(int[] dim) {
				int[] temp = Arrays.copyOf(dim, dim.length);
				Arrays.sort(temp);
				
				Assert.assertEquals(3, temp.length);
				Assert.assertArrayEquals(new int[]{0, 5, 9}, temp);
				
				SpatialSort s = super.getSortingFunction(dim);
				Assert.assertTrue(s instanceof HilbertSort3D);
				
				invoked.set(true);
				return s;
			}
			
		};
		
		int[] seq = ssort.sort(this.toList(this.input));
		Assert.assertTrue(invoked.get());
		
		double total = 0.0;
		for(int i = 1; i < seq.length; i++){
			double[] u = this.input.getRow(seq[i - 1]);
			double[] v = this.input.getRow(seq[i]);
			
			double dist = 0.0;
			for(int j = 0; j < u.length; j++){
				double dx = v[j] - u[j];
				dist += dx * dx;
			}
			
			total += dist;
		}
		
		Assert.assertFalse(total - this.output.get(0, 0) > 0.1);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionWhenGetSortingFunctionWithNoDimIsGiven() {
		DefaultSpatialSort ssort = new DefaultSpatialSort(() -> 0.1, 
			DefaultSpatialSort.DEFAULT_SAMPLING_SIZE, 
			DefaultSpatialSort.DEFAULT_R_SQUARE
		);
		
		ssort.getSortingFunction(new int[0]);
	}
	
	@Test
	public void shouldReturnScalarSortWhenGetSortingFunctionWith1DimGiven() {
		DefaultSpatialSort ssort = new DefaultSpatialSort(() -> 0.1, 
			DefaultSpatialSort.DEFAULT_SAMPLING_SIZE, 
			DefaultSpatialSort.DEFAULT_R_SQUARE
		);
		
		Assert.assertNotNull(ssort.getSortingFunction(new int[]{1}));
	}
	
	@Test
	public void shouldReturnImplWhenGetSortingFunctionWith2or3DimsGiven() {
		DefaultSpatialSort ssort = new DefaultSpatialSort(() -> 0.1, 
			DefaultSpatialSort.DEFAULT_SAMPLING_SIZE, 
			DefaultSpatialSort.DEFAULT_R_SQUARE
		);
		
		Assert.assertNotNull(ssort.getSortingFunction(new int[]{1, 2}));
		Assert.assertNotNull(ssort.getSortingFunction(new int[]{1, 2, 3}));
	}
	
	@Test
	public void shouldBeAbleToSortColumnVector() {
		double[] vector = new Random(Double.doubleToLongBits(Math.sqrt(2.0))).doubles()
			.map(v -> 1000 * v - 500.0)
			.limit(1024).toArray();
		
		int[] seq = DefaultSpatialSort.getInstance().sort(this.toList(new ColumnVector(vector)));
		
		for(int i = 1; i < seq.length; i++){
			Assert.assertFalse(vector[seq[i - 1]] > vector[seq[i]]);
		}
		
		int[] alt = DefaultSpatialSort.getInstance().getSortingFunction(new int[]{0})
				.sort(this.toList(new ColumnVector(vector)));
		
		Assert.assertArrayEquals(seq, alt);
	}
	
	@Test
	public void shouldBeAbleToSortEmptyList() {
		int[] result = DefaultSpatialSort.getInstance().sort(Collections.emptyList());
		Assert.assertNotNull(result);
		Assert.assertEquals(0, result.length);
	}
	
	@Test
	public void shouldBeAbleToSortEmptyVectors() {
		int[] result = DefaultSpatialSort.getInstance().sort(Arrays.asList(
			new double[0],
			new double[0],
			new double[0],
			new double[0]
		));
		Assert.assertNotNull(result);
		Assert.assertArrayEquals(new int[]{0, 1, 2, 3}, result);
	}
	
	@Test
	public void shouldBeAbleToSort3DVectorByDefaultImpl() {
		int[] result = DefaultSpatialSort.getInstance().sort(Arrays.asList(
			new double[]{1.0, 2.0, 3.0}
		));
		Assert.assertNotNull(result);
		Assert.assertArrayEquals(new int[]{0}, result);
	}
	
	@Test
	public void shouldBeAbleToSampleBetweenRange() {
		for(int n = 10; n < 256; n++){
			int a = DefaultSpatialSort.getInstance().sample(n);
			Assert.assertTrue(a >= 0);
			Assert.assertTrue(a < n);
		}
	}
	
	protected List<double[]> toList(Matrix matrix) {
		return new AbstractList<double[]>(){

			@Override
			public double[] get(int index) {
				return matrix.getRow(index);
			}

			@Override
			public int size() {
				return matrix.getRowCount();
			}
			
		};
	}
}
 