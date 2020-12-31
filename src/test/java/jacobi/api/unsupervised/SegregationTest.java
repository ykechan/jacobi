package jacobi.api.unsupervised;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.test.util.Jacobi;

public class SegregationTest {
	
	@Test
	public void shouldBeAbleToGetAltRows() {
		Matrix matrix = Matrices.wrap(
			new double[]{1.0, 6.0},
			new double[]{2.0, 7.0},
			new double[]{3.0, 8.0},
			new double[]{4.0, 9.0},
			new double[]{5.0, 10.0}
		);
		
		List<Matrix> alt = matrix.ext(Unsupervised.class).segregate(Arrays.asList(
			new int[]{0, 2, 4},
			new int[]{1, 3}
		));
		
		Jacobi.assertEquals(Matrices.wrap(
			new double[]{1.0, 6.0},
			new double[]{3.0, 8.0},
			new double[]{5.0, 10.0}
		), alt.get(0));
		
		Jacobi.assertEquals(Matrices.wrap(
			new double[]{2.0, 7.0},
			new double[]{4.0, 9.0}
		), alt.get(1));
	}
	
	@Test
	public void shouldBeAbleToSegregationSubMatrix() {
		Matrix matrix = Matrices.wrap(
			new double[]{1.0, 6.0},
			new double[]{2.0, 7.0},
			new double[]{3.0, 8.0},
			new double[]{4.0, 9.0},
			new double[]{5.0, 10.0}
		);
			
		List<Matrix> alt = matrix.ext(Unsupervised.class).segregate(Arrays.asList(
			new int[]{0, 4}
		));
		
		Assert.assertEquals(1, alt.size());
		Jacobi.assertEquals(Matrices.wrap(
				new double[]{1.0, 6.0},
				new double[]{5.0, 10.0}
		), alt.get(0));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldFailWhenInputMatrixIsNull() {
		new Segregation().compute(null, Collections.emptyList());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldFailWhenListOfIndicesIsNull() {
		Matrix matrix = Matrices.wrap(
				new double[]{1.0, 6.0},
				new double[]{2.0, 7.0},
				new double[]{3.0, 8.0},
				new double[]{4.0, 9.0},
				new double[]{5.0, 10.0}
		);
				
		matrix.ext(Unsupervised.class).segregate(null);
	}
	
	@Test
	public void shouldReturnEmptyWhenListOfIndicesIsEmpty() {
		Matrix matrix = Matrices.wrap(
				new double[]{1.0, 6.0},
				new double[]{2.0, 7.0},
				new double[]{3.0, 8.0},
				new double[]{4.0, 9.0},
				new double[]{5.0, 10.0}
		);
				
		List<Matrix> list = matrix.ext(Unsupervised.class).segregate(Collections.emptyList());
		Assert.assertNotNull(list);
		Assert.assertTrue(list.isEmpty());
	}

}
