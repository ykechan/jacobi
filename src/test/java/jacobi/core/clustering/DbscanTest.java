package jacobi.core.clustering;

import java.awt.Color;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import jacobi.api.Matrix;
import jacobi.api.unsupervised.Unsupervised;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.util.JacobiJUnit4ClassRunner;
import jacobi.test.util.JacobiSvg;

@JacobiImport("/jacobi/test/data/DbscanTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class DbscanTest {
	
	@JacobiInject(0)
	public Matrix input;
	
	@JacobiInject(1)
	public Matrix query;
	
	@JacobiInject(10)
	public Matrix oracle;
	
	@Test
	@JacobiImport("test noisy circle")
	public void shouldBeAbleToClusterNoisyCircle() throws IOException {
		double[] q = query.getRow(0);
		int minPts = (int) q[0];
		double eps = q[1];
		
		List<int[]> clusters = new Dbscan(minPts, eps).compute(this.input);
		Assert.assertEquals(2, clusters.size());
		
		int[] cluster0 = clusters.get(0);
		int[] cluster1 = clusters.get(1);
		
		Arrays.sort(cluster0);
		Arrays.sort(cluster1);
		
		int[] inner = cluster0[0] == 0 ? cluster0 : cluster1;
		int[] outer = inner == cluster0 ? cluster1 : cluster0;
		
		int numInner = (int) this.oracle.get(0, 0);
		Assert.assertArrayEquals(IntStream.range(0, numInner).toArray(), inner);
		Assert.assertArrayEquals(IntStream.range(numInner, this.input.getRowCount()).toArray(), outer);
	}

}
