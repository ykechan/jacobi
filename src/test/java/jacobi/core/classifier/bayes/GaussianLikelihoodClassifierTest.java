package jacobi.core.classifier.bayes;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.api.classifier.Column;
import jacobi.api.classifier.DataTable;
import jacobi.core.classifier.bayes.GaussianLikelihoodClassifier.Learner;
import jacobi.core.clustering.ClusterMetric;
import jacobi.core.clustering.GaussianCluster;
import jacobi.core.clustering.StandardScoreCluster;
import jacobi.core.util.Pair;
import jacobi.core.util.Weighted;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.JacobiDataDef;
import jacobi.test.util.JacobiJUnit4ClassRunner;

@RunWith(JacobiJUnit4ClassRunner.class)
@JacobiImport("/jacobi/test/data/GaussianLikelihoodClassifierTest.xlsx")
public class GaussianLikelihoodClassifierTest {
	
	private Workbook workbook;
	
	@JacobiInject(0)
	public Matrix input;
	
	@JacobiInject(1)
	public Matrix outcome;
	
	@JacobiInject(2)
	public Matrix weights;
	
	@JacobiInject(3)
	public Matrix testData;
	
	@JacobiResult(10)
	public Matrix likelihoods;
	
	@Before
	public void init() throws IOException, EncryptedDocumentException, InvalidFormatException {
		try(InputStream input = this.getClass().getResourceAsStream(
				"/jacobi/test/data/GaussianLikelihoodClassifierTest.xlsx")){
			this.workbook = WorkbookFactory.create(input);
		}
	}
	
	@Test
	@JacobiImport("iris")
	@JacobiEquals(expected = 10, actual = 10)
	public void shouldBeAbleToTestOnIrisDataUsingFullGauss() {
		DataTable<String> dataTab = new JacobiDataDef(this.workbook, Collections.emptyList())
				.loadDef("iris", String.class)
				.apply(this.input, this.outcome);
		
		Column<String> outCol = dataTab.getOutcomeColumn();
		
		GaussianLikelihoodClassifier<String, ?> classifier = 
				this.fullLearner(String.class).learn(dataTab, null);
		
		double[][] results = new double[this.input.getRowCount()][];
		for(int i = 0; i < this.input.getRowCount(); i++){
			double[] feats = this.input.getRow(i);
			double[] lnLikes = classifier.eval(feats, outCol);
			
			results[i] = lnLikes;
		}
		this.likelihoods = Matrices.wrap(results);
	}
	
	@Test
	@JacobiImport("wine")
	@JacobiEquals(expected = 10, actual = 10)
	public void shouldBeAbleToTestOnWineDataUsingNaiveGauss() {
		DataTable<String> dataTab = new JacobiDataDef(this.workbook, Collections.emptyList())
				.loadDef("wine", String.class)
				.apply(this.input, this.outcome);
		
		Column<String> outCol = dataTab.getOutcomeColumn();
		
		GaussianLikelihoodClassifier<String, ?> classifier = 
				this.naiveLearner(String.class).learn(dataTab, null);
		
		double[][] results = new double[this.testData.getRowCount()][];
		for(int i = 0; i < this.testData.getRowCount(); i++){
			double[] feats = this.testData.getRow(i);
			double[] lnLikes = classifier.eval(feats, outCol);
			
			int out = outCol.getItems().indexOf(classifier.apply(feats));
			results[i] = Arrays.copyOf(lnLikes, lnLikes.length + 1);
			results[i][lnLikes.length] = out;
		}
		this.likelihoods = Matrices.wrap(results);
	}

	private <T> Learner<T, ?> fullLearner(Class<T> clazz) {
		ClusterMetric<Weighted<Pair>> metric = GaussianCluster.getInstance();
		return new Learner<T, Weighted<Pair>>(new ClusterMetric<Weighted<Pair>>(){

			@Override
			public Weighted<Pair> expects(Matrix matrix) {
				Weighted<Pair> model = metric.expects(matrix);
				return model;
			}

			@Override
			public double distanceBetween(Weighted<Pair> cluster, double[] vector) {
				return metric.distanceBetween(cluster, vector);
			}
			
		});
	}
	
	private <T> Learner<T, ?> naiveLearner(Class<T> clazz) {
		ClusterMetric<Matrix> metric = StandardScoreCluster.getInstance();
		return new Learner<T, Matrix>(metric);
	}
}
