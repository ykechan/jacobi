package jacobi.core.spatial.rtree;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.JacobiJUnit4ClassRunner;

@JacobiImport("/jacobi/test/data/RDistHeuristicPackerTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class RDistHeuristicPackerTest {
	
	@JacobiInject(0)
	public Matrix input;
	
	@JacobiResult(100)
	public Matrix output;
	
	@Test
	@JacobiImport("test 10 rand 2-D")
	@JacobiEquals(expected = 100, actual = 100)
	public void shouldBeAbleToPack10Rand2DPoints() {
		RDistHeuristicPacker packer = new RDistHeuristicPacker(1, this.input.getRowCount());
		int index = packer.packFront(this.toList(this.input), 0);
		
		this.output = Matrices.scalar(index);
	}
	
	@Test
	@JacobiImport("test 32 sorted rand 2-D")
	@JacobiEquals(expected = 100, actual = 100)
	public void shouldBeAbleToPack32SortedRand2DPoints() {
		RDistHeuristicPacker packer = new RDistHeuristicPacker(1, 10);
		RLayer nodes = packer.apply(this.toList(this.input));
		
		this.output = Matrices.wrap(new double[][]{
			Arrays.stream(nodes.cuts).mapToDouble(v -> v).toArray()
		});
	}
	
	@Test
	@JacobiImport("test 30 sorted rand 2-D")
	@JacobiEquals(expected = 100, actual = 100)
	public void shouldBeAbleToPack30SortedRand2DPoints() {
		RDistHeuristicPacker packer = new RDistHeuristicPacker(3, 10);
		RLayer nodes = packer.apply(this.toList(this.input));
		
		this.output = Matrices.wrap(new double[][]{
			Arrays.stream(nodes.cuts).mapToDouble(v -> v).toArray()
		});
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
 