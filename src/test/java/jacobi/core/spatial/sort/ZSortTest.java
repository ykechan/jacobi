package jacobi.core.spatial.sort;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.JacobiJUnit4ClassRunner;

@JacobiImport("/jacobi/test/data/ZSortTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class ZSortTest {
	
	@JacobiInject(0)
	public Matrix input;
	
	@JacobiResult(1)
	public Matrix result;
	
	@JacobiInject(100)
	public Matrix stats;
	
	@Test
	@JacobiImport("test 12 dim vars")
	@JacobiEquals(expected = 1, actual = 1)
	public void shouldBeAbleToSelect4LargestDimsIn12DimsGivenVars() {
		int[] dims = new ZSort(null, null, 1.0).selectDims(this.input.getRow(0), 4);
		this.result = Matrices.wrap(new double[][]{ 
			Arrays.stream(dims).mapToDouble(v -> v).toArray() 
		});
	}
	
	@Test
	@JacobiImport("test 12 dim vars only 3 large")
	@JacobiEquals(expected = 1, actual = 1)
	public void shouldBeAbleToSelect3LargestDimsIn12DimsGivenVarsGivenRSquare() {
		int[] dims = new ZSort(null, null, 0.75).selectDims(this.input.getRow(0), 8);
		this.result = Matrices.wrap(new double[][]{ 
			Arrays.stream(dims).mapToDouble(v -> v).toArray() 
		});
	}
	
	@Test
	@JacobiImport("test Rand Cont' 30x10 max 8")
	public void shouldBeAbleToUseSortingFunction() {
		ZSort zSort = new ZSort(this::mockStats, this::singleDim, 0.0);
		int[] dims = zSort.selectDims(this.stats.getRow(1), 8);
		Assert.assertEquals(1, dims.length);
		Assert.assertEquals(7, dims[0]);
		
		int[] seq = zSort.sort(this.toList(this.input, null));
		for(int i = 1; i < seq.length; i++){
			double[] prev = this.input.getRow(seq[i - 1]);
			double[] curr = this.input.getRow(seq[i]);
			
			Assert.assertFalse(prev[7] > curr[7]);
		}
	}
	
	@Test
	@JacobiImport("test Rand Cont' 30x10 max 8")
	@JacobiEquals(expected = 1, actual = 1)
	public void shouldBeAbleToSortRandCont30x10Max8(){
		ZSort zSort = new ZSort(this::mockStats, dims -> null, 1.0);
		
		int[] seq = zSort.sort(this.toList(input, null));
		this.result = Matrices.wrap(this.toList(this.input, seq).toArray(new double[0][]));
	}
	
	@Test
	@JacobiImport("test Rand 40x8 max 3")
	@JacobiEquals(expected = 1, actual = 1)
	public void shouldBeAbleToSort1stRoundRand40x8Max3() {
		ZSort zSort = new ZSort(this::mockStats, dims -> null, 1.0) {

			@Override
			protected int[] sort(Context context, int begin, int end) {
				int[] span = super.sort(context, begin, end);
				Assert.assertEquals(8 + 1, span.length);
				return new int[0];
			}

			@Override
			protected int[] selectDims(double[] vars, int limit) {
				int[] dims = super.selectDims(vars, limit);
				return dims;
			}

		};
		zSort.setMaxDim(3);
		
		int[] seq = zSort.sort(this.toList(this.input, null));
		this.result = Matrices.wrap(this.toList(this.input, seq).toArray(new double[0][]));
	}

	protected Matrix mockStats(List<double[]> vectors){
		return this.stats;
	}
	
	protected SpatialSort singleDim(int[] dim) {
		if(dim.length > 1){
			return null;
		}
		
		int d = dim[0];
		return ls -> IntStream.range(0, ls.size())
			.boxed().sorted(Comparator.comparingDouble(i -> ls.get(i)[d]))
			.mapToInt(Integer::intValue)
			.toArray();
	}
	
	protected List<double[]> toList(Matrix matrix, int[] seq) {
		return new AbstractList<double[]>(){

			@Override
			public double[] get(int index) {
				int idx = seq == null ? index : seq[index];
				return matrix.getRow(idx);
			}

			@Override
			public int size() {
				return matrix.getRowCount();
			}
			
		};
	}

}
