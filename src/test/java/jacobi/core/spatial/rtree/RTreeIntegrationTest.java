package jacobi.core.spatial.rtree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.Test;
import org.junit.runner.RunWith;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.core.impl.ColumnVector;
import jacobi.core.spatial.sort.SpatialSort;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.JacobiJUnit4ClassRunner;

@JacobiImport("/jacobi/test/data/RTreeIntegrationTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class RTreeIntegrationTest {
	
	@JacobiInject(0)
	public Matrix input;
	
	@JacobiInject(1)
	public Matrix query;
	
	@JacobiResult(2)
	public Matrix counts;
	
	@Test
	@JacobiImport("Rand 500x2")
	@JacobiEquals(expected = 2, actual = 2)
	public void shouldBeAbleToCreateAndQueryFor500Random2DData() {
		RTree<Integer> tree = RTreeFactory.of(4, 500)
			.create(this.input, 
				RDefaultDistances.EUCLIDEAN_SQ.againstAabb(), 
				RDefaultDistances.EUCLIDEAN_SQ.againstPoint()
			);
		
		double[] result = new double[query.getRowCount()];
		for(int i = 0; i < query.getRowCount(); i++) {
			double[] q = query.getRow(i);
			
			Iterator<Integer> iter = tree.queryRange(new double[] {q[0], q[1]}, q[2] * q[2]);
			int count = 0;
			while(iter.hasNext()) {
				iter.next();
				count++;
			}
			
			result[i] = count;
		}
		
		this.counts = new ColumnVector(result);
	}

}
