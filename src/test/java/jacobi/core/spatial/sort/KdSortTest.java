package jacobi.core.spatial.sort;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import jacobi.api.Matrix;
import jacobi.core.impl.ImmutableMatrix;
import jacobi.core.spatial.sort.KdSort.Div;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.JacobiJUnit4ClassRunner;

/**
 * 
 * @author Y.K. Chan
 *
 */
@JacobiImport("/jacobi/test/data/KdSortTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class KdSortTest {
	
	@JacobiInject(0)
	public Matrix input;
	
	@JacobiResult(1)
	public Matrix output;
	
	@JacobiInject(100)
	public Matrix stats;
	
	@Test
	@JacobiImport("Rand Cont' 30x10")
	public void shouldBeAbleToSelectDimensionsInLargeVariance() {
		int[] dims = new KdSort(d -> null, m -> this.stats, 1.0)
				.selectDims(this.stats.getRow(1), 8);
		Assert.assertArrayEquals(new int[] {7, 4, 5, 6, 3, 2, 1, 0}, dims);
	}
	
	@Test
	@JacobiImport("Rand Cont' 30x10")
	@JacobiEquals(expected = 1, actual = 1)
	public void shouldBeAbleToSortADiv() {
		List<double[]> vectors = this.toList(this.input);
		
		int[] seq = IntStream.range(0, this.input.getRowCount()).toArray();
		new KdSort(d -> null, m -> this.stats, 1.0)
			.sortDiv(vectors, new Div(seq, 0, seq.length));
		
		this.output = this.toMatrix(vectors, seq);
	}
	
	@Test
	@JacobiImport("Rand Dist & Cont' 40x8")
	public void shouldBeAbleToDivideContAndDistData() {
	}
	
	@Test
	@JacobiImport("Rand Corr. Data 40x8")
	public void shouldBeAbleToDivideCorrelatedData() {
		List<double[]> vectors = this.toList(this.input);
		
		int[] seq = IntStream.range(0, this.input.getRowCount()).toArray();
		new KdSort(d -> null, m -> this.stats, 1.0)
			.sortDiv(vectors, new Div(seq, 0, seq.length));
		
		this.output = this.toMatrix(vectors, seq);
	}
	
	protected List<double[]> toList(Matrix matrix) {
		return new AbstractList<double[]>() {

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
	
	protected Matrix toMatrix(List<double[]> vectors, int[] order) {
		return this.toMatrix(new AbstractList<double[]>() {

			@Override
			public double[] get(int index) {
				return vectors.get(order[index]);
			}

			@Override
			public int size() {
				return order.length;
			}
			
		});
	}
	
	protected Matrix toMatrix(List<double[]> vectors) {
		return new ImmutableMatrix() {

			@Override
			public int getRowCount() {
				return vectors.size();
			}

			@Override
			public int getColCount() {
				return vectors.isEmpty() ? 0 : vectors.get(0).length;
			}

			@Override
			public double[] getRow(int index) {
				return vectors.get(index);
			}
			
		};
	}

}
