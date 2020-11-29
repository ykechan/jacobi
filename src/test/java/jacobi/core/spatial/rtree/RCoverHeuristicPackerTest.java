package jacobi.core.spatial.rtree;

import org.junit.Test;
import org.junit.runner.RunWith;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.JacobiJUnit4ClassRunner;

@JacobiImport("/jacobi/test/data/RCoverHeuristicPackerTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class RCoverHeuristicPackerTest {
	
	@JacobiInject(0)
	public Matrix input;
	
	@JacobiResult(10)
	public Matrix output;
	
	@Test
	@JacobiImport("test cover 2-D AABBs")
	@JacobiEquals(expected = 10, actual = 10)
	public void shouldBeAbleToPackRand2DAABBs() {
		RLayer rLayer = this.ofAabbs(this.input);
		int span = new RCoverHeuristicPacker(1, 16).packFront(rLayer, 0);
		this.output = Matrices.scalar(span);
	}
	
	@Test
	@JacobiImport("test cover 2-D AABBs (2)")
	@JacobiEquals(expected = 10, actual = 10)
	public void shouldBeAbleToPackDesigned2DAABBs() {
		RLayer rLayer = this.ofAabbs(this.input);
		int span = new RCoverHeuristicPacker(3, 16).packFront(rLayer, 0);
		this.output = Matrices.scalar(span);
	}
	
	@Test
	@JacobiImport("test cover 2-D AABBs 2nd span")
	public void shouldBeAbleToPackDesigned2DAABBs2ndSpan() {
		RLayer rLayer = this.ofAabbs(this.input);
		int span = new RCoverHeuristicPacker(1, 16).packFront(rLayer, 0);
		this.output = Matrices.scalar(span);
	}
	
	protected RLayer ofAabbs(Matrix aabbs) {
		double[] array = new double[aabbs.getRowCount() * aabbs.getColCount()];
		for(int i = 0; i < aabbs.getRowCount(); i++){
			double[] row = aabbs.getRow(i);
			System.arraycopy(row, 0, array, i * aabbs.getColCount(), row.length);
		}
		
		return new RLayer(new int[aabbs.getRowCount()], array);
	}

}
