package jacobi.api.spatial;

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
	
}
