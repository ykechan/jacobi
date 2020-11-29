package jacobi.core.spatial.sort;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import jacobi.api.Matrix;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.util.JacobiJUnit4ClassRunner;

@JacobiImport("/jacobi/test/data/DefaultSpatialSortTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class DefaultSpatialSortTest {
	
	@JacobiInject(0)
	public Matrix input;
	
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
			System.out.println(Arrays.toString(data.get(s))
					.replace("[", "")
					.replace("]", "")
					.replace(", ", "\t"));
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
