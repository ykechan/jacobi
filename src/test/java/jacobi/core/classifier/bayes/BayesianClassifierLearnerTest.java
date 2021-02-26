package jacobi.core.classifier.bayes;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.IntStream;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import jacobi.api.Matrix;
import jacobi.api.classifier.Column;
import jacobi.api.classifier.DataTable;
import jacobi.api.classifier.bayes.BayesianClassifierParams;
import jacobi.core.classifier.Reweightable;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.util.JacobiDataDef;
import jacobi.test.util.JacobiJUnit4ClassRunner;

@RunWith(JacobiJUnit4ClassRunner.class)
@JacobiImport("/jacobi/test/data/BayesianClassifierLearnerTest.xlsx")
public class BayesianClassifierLearnerTest {
	
	private Workbook workbook;
	
	@JacobiInject(0)
	public Matrix input;
	
	@JacobiInject(1)
	public Matrix outcome;
	
	@JacobiInject(2)
	public Matrix weights;
	
	@JacobiInject(100)
	public Matrix results;
	
	@Before
	public void init() throws IOException, EncryptedDocumentException, InvalidFormatException {
		try(InputStream input = this.getClass().getResourceAsStream(
				"/jacobi/test/data/BayesianClassifierLearnerTest.xlsx")){
			this.workbook = WorkbookFactory.create(input);
		}
	}
	
	@Test
	@JacobiImport("test fruits example")
	public void shouldBeAbleToLearnFromTestFruitsExample() {
		DataTable<String> dataTab = new JacobiDataDef(this.workbook, Collections.emptyList())
				.loadDef("test fruits example", String.class)
				.apply(this.input, this.outcome);

		double[] weights = IntStream.range(0, this.weights.getRowCount())
				.mapToDouble(i -> this.weights.get(i, 0)).toArray();
		
		dataTab = Reweightable.of(dataTab).reweight(weights);
		BayesianClassifierParams params = new BayesianClassifierParams()
				.setPseudoCount(0.0); // do not use pseudo-count
		BayesianClassifier<String> classifier = new BayesianClassifierLearner<String>().learn(dataTab, params);
		
		Column<String> outCol = dataTab.getOutcomeColumn();
		
		for(int i = 0; i < this.results.getRowCount(); i++){
			double[] row = this.results.getRow(i);
			double[] feats = Arrays.copyOf(row, dataTab.getColumns().size());
			
			int ans = outCol.getItems().indexOf(classifier.apply(feats));
			Assert.assertEquals((int) row[row.length - 1], ans);
		}
	}
	
	@Test
	@JacobiImport("test promotion example")
	public void shouldBeAbleToLearnFromTestPromotionExample() {
		DataTable<Boolean> dataTab = new JacobiDataDef(this.workbook, Collections.emptyList())
				.loadDef("test promotion example", Boolean.class)
				.apply(this.input, this.outcome);

		double[] weights = IntStream.range(0, this.input.getRowCount()).mapToDouble(i -> 1.0).toArray();
		
		dataTab = Reweightable.of(dataTab).reweight(weights);
		BayesianClassifierParams params = new BayesianClassifierParams()
				.setPseudoCount(0.0); // do not use pseudo-count
		BayesianClassifier<Boolean> classifier = new BayesianClassifierLearner<Boolean>().learn(dataTab, params);
		
		for(int i = 0; i < this.results.getRowCount(); i++){
			double[] row = this.results.getRow(i);
			double[] feats = Arrays.copyOf(row, dataTab.getColumns().size());
			
			boolean ans = classifier.apply(feats);
			Assert.assertEquals((int) row[row.length - 1], ans ? 1 : 0);
		}
	}

}
