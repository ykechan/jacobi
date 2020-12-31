package jacobi.api.spatial;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.api.ext.Data;
import jacobi.api.ext.Spatial;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.JacobiJUnit4ClassRunner;

@RunWith(JacobiJUnit4ClassRunner.class)
@JacobiImport("/jacobi/test/data/SpatialTest.xlsx")
public class SpatialTest {
	
	@JacobiInject(0)
	public Matrix input;
	
	@JacobiInject(1)
	public Matrix query;
	
	@JacobiInject(10)
	public Matrix oracle;
	
	@JacobiResult(10)
	public Matrix ans;
	
	@Test
	@JacobiImport("test kNN 4 indicators")
	@JacobiEquals(expected = 10, actual = 10)
	public void shouldBeAbleToQueryKNNIn4Indicators() {
		SpatialIndex<Integer> sIndex = this.input
			.ext(Data.class)
			.select(0, 1, 2, 3).get()
			.ext(Spatial.class).build();
		List<Integer> kNN = sIndex.queryKNN(this.query.getRow(0), this.oracle.getColCount());
		
		this.ans = Matrices.wrap(new double[][]{
			kNN.stream().mapToDouble(v -> (double) v).toArray()
		});
	}
	
	@Test
	@JacobiImport("test kNN 4 indicators")
	public void shouldBeAbleToCrossValidationByKNN() {
		SpatialIndex<Boolean> sIndex = this.input
			.ext(Data.class)
			.select(0, 1, 2, 3).get()
			.ext(Spatial.class).build().map(i -> this.input.get(i, 4) > 0.0);
		
		int[] confusion = new int[4];
		int correct = 0;
		for(int i = 0; i < this.input.getRowCount(); i++){
			double[] q = Arrays.copyOf(this.input.getRow(i), 4);
			boolean ans = this.input.get(i, 4) > 0.0;
			List<Boolean> kNN = sIndex.queryKNN(q, this.oracle.getColCount());
			boolean guess = kNN.stream().skip(1).filter(b -> b).count() > 2;
			
			System.out.println("#" + i + ", ans = " + ans + ", guess = " + guess);
			
			int index = (ans ? 2 : 0) + (guess ? 1 : 0);
			if(ans == guess){
				correct++;
			}
			confusion[index]++;
		}
		
		System.out.println(Arrays.toString(confusion));
		System.out.println(correct);
	}
	
}
