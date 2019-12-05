package jacobi.core.stats.select;

import org.junit.Test;
import org.junit.runner.RunWith;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.JacobiJUnit4ClassRunner;

@JacobiImport("/jacobi/test/data/MedianOfMediansTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class MedianOfMediansTest {
	
	@JacobiInject(0)
	public Matrix input;
	
	@JacobiResult(1)
	public Matrix output;
	
	@Test
	@JacobiImport("test median of groups")
	@JacobiEquals(expected = 1, actual = 1)
	public void shouldBeAbleToSwapMedianOfGroupsIntoFront() {
		MedianOfMedians selector = new MedianOfMedians(new ExtremaSelect());
		input.getApplySet(0, r -> { selector.medianToFront(r, 0, r.length); return r; });
		input.getApplySet(1, r -> { selector.medianToFront(r, 0, r.length); return r; });
		input.getApplySet(2, r -> { selector.medianToFront(r, 0, r.length); return r; });
		
		this.output = Matrices.wrap(new double[][] {
			new double[] {input.get(0, 0), input.get(0, 5), input.get(0, 10)},
			new double[] {input.get(1, 0), input.get(1, 5), input.get(1, 10)},
			new double[] {input.get(2, 0), input.get(2, 5), input.get(2, 10)}
		});
	}
	
	@Test
	@JacobiImport("test median of groups in 14")
	@JacobiEquals(expected = 1, actual = 1)
	public void shouldBeAbleToSwapMedianOfGroupsIntoFrontWith4ElementsInLastGroup() {
		MedianOfMedians selector = new MedianOfMedians(new ExtremaSelect());
		input.getApplySet(0, r -> { selector.medianToFront(r, 0, r.length); return r; });
		input.getApplySet(1, r -> { selector.medianToFront(r, 0, r.length); return r; });
		input.getApplySet(2, r -> { selector.medianToFront(r, 0, r.length); return r; });
		
		this.output = Matrices.wrap(new double[][] {
			new double[] {input.get(0, 0), input.get(0, 5), input.get(0, 10)},
			new double[] {input.get(1, 0), input.get(1, 5), input.get(1, 10)},
			new double[] {input.get(2, 0), input.get(2, 5), input.get(2, 10)}
		});
	}
	
	@Test
	public void shouldBeAbleToGroupAllMediansIntoFirstGroup() {
		
	}
	
	protected Select getInstance() {
		//return new MedianOfMedians(new Extrema);
		//return new MedianOfMedians();
		return null;
	}

}
