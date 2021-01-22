package jacobi.core.clustering;

import java.util.ArrayList;
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

@JacobiImport("/jacobi/test/data/PseudoSilhouetteCoeffTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class PseudoSilhouetteCoeffTest {
	
	@JacobiInject(0)
	public Matrix input;
	
	@JacobiInject(1)
	public Matrix clusterMat;
	
	@JacobiResult(10)
	public Matrix ans;
	
	@Test
	@JacobiImport("test gauss 5D 4 centroids")
	@JacobiEquals(expected = 10, actual = 10)
	public void shouldBeAbleToComputeSilhouetteInGauss5D4CentroidsWith2Clusters() {
		List<int[]> clusters = this.toClusters(this.clusterMat);
		double sil = new PseudoSilhouetteCoeff(EuclideanCluster.getInstance(), Integer.MAX_VALUE)
				.applyAsDouble(input, clusters);
		this.ans = Matrices.scalar(sil);
	}
	
	@Test
	@JacobiImport("test gauss 5D 4 centroids (4)")
	@JacobiEquals(expected = 10, actual = 10)
	public void shouldBeAbleToComputeSilhouetteInGauss5D4CentroidsWith4Clusters() {
		List<int[]> clusters = this.toClusters(this.clusterMat);
		double sil = new PseudoSilhouetteCoeff(EuclideanCluster.getInstance(), Integer.MAX_VALUE)
				.applyAsDouble(input, clusters);
		this.ans = Matrices.scalar(sil);
	}
	
	protected List<int[]> toClusters(Matrix input) {
		List<int[]> clusters = new ArrayList<>(input.getRowCount());
		for(int i = 0; i < input.getRowCount(); i++){
			double[] row = input.getRow(i);
			int n = row.length;
			while(n > 1 && row[n - 1] == 0.0){
				n--;
			}
			
			clusters.add(Arrays.stream(row).limit(n).mapToInt(v -> (int) v).toArray());
		}
		return clusters;
	}

}
