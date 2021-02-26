package jacobi.core.classifier.bayes;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.api.classifier.Column;
import jacobi.api.classifier.DataTable;
import jacobi.core.classifier.Reweightable;
import jacobi.core.classifier.bayes.NominalLikelihoodClassifier.Learner;
import jacobi.core.classifier.cart.util.JacobiEnums.Outlook;
import jacobi.core.classifier.cart.util.JacobiEnums.YesOrNo;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.JacobiDataDef;
import jacobi.test.util.JacobiJUnit4ClassRunner;

@RunWith(JacobiJUnit4ClassRunner.class)
@JacobiImport("/jacobi/test/data/NominalLikelihoodClassifierTest.xlsx")
public class NominalLikelihoodClassifierTest {
	
	private static final List<Class<?>> ENUMS = Arrays.asList(
		Outlook.class, YesOrNo.class
	);
	
	private Workbook workbook;
	
	@JacobiInject(0)
	public Matrix input;
	
	@JacobiInject(1)
	public Matrix outcome;
	
	@JacobiInject(2)
	public Matrix weights;
	
	@JacobiInject(3)
	public Matrix model;	
	
	@JacobiInject(4)
	public Matrix props;
	
	@JacobiResult(10)
	public Matrix conj;
	
	@JacobiResult(11)
	public Matrix likelihood;
	
	@Before
	public void init() throws IOException, EncryptedDocumentException, InvalidFormatException {
		try(InputStream input = this.getClass().getResourceAsStream(
				"/jacobi/test/data/NominalLikelihoodClassifierTest.xlsx")){
			this.workbook = WorkbookFactory.create(input);
		}
	}
	
	@Test
	@JacobiImport("unweighted outlook and golf")
	@JacobiEquals(expected = 10, actual = 10)
	@JacobiEquals(expected = 11, actual = 11)
	public void shouldBeAbleToCountOutlookAndPlayInGolf() {
		DataTable<YesOrNo> dataTab = new JacobiDataDef(this.workbook, ENUMS)
				.loadDef("unweighted outlook and golf", YesOrNo.class)
				.apply(this.input, this.outcome);
		
		Column<?> outlookCol = dataTab.getColumns().get(0);
		
		Learner<YesOrNo> learner = new NominalLikelihoodClassifier.Learner<>(outlookCol);
		Matrix temp = learner.countWeights(dataTab);
		this.conj = Matrices.copy(temp);
		this.likelihood = this.exp(learner.toLnLikelihood(temp));
	}
	
	@Test
	@JacobiImport("unweighted windy and golf")
	@JacobiEquals(expected = 10, actual = 10)
	@JacobiEquals(expected = 11, actual = 11)
	public void shouldBeAbleToCountWindyAndPlayInGolf() {
		DataTable<YesOrNo> dataTab = new JacobiDataDef(this.workbook, ENUMS)
				.loadDef("unweighted windy and golf", YesOrNo.class)
				.apply(this.input, this.outcome);
		
		Column<?> windy = dataTab.getColumns().get(3);
		
		Learner<YesOrNo> learner = new NominalLikelihoodClassifier.Learner<>(windy);
		Matrix temp = learner.countWeights(dataTab);
		this.conj = Matrices.copy(temp);
		this.likelihood = this.exp(learner.toLnLikelihood(temp));
	}
	
	@Test
	@JacobiImport("weighted outlook and golf")
	@JacobiEquals(expected = 10, actual = 10)
	@JacobiEquals(expected = 11, actual = 11)
	public void shouldBeAbleToCountWeightedOutlookAndPlayInGolf() {
		DataTable<YesOrNo> dataTab = new JacobiDataDef(this.workbook, ENUMS)
				.loadDef("weighted outlook and golf", YesOrNo.class)
				.apply(this.input, this.outcome);
		
		dataTab = Reweightable.of(dataTab).reweight(
			IntStream.range(0, this.weights.getRowCount()).mapToDouble(i -> this.weights.get(i, 0)).toArray()
		);
		
		Column<?> outlookCol = dataTab.getColumns().get(0);
		
		Learner<YesOrNo> learner = new NominalLikelihoodClassifier.Learner<>(outlookCol);
		Matrix temp = learner.countWeights(dataTab);
		this.conj = Matrices.copy(temp);
		this.likelihood = this.exp(learner.toLnLikelihood(temp));
	}
	
	@Test
	@JacobiImport("outlook model predicts golf")
	@JacobiEquals(expected = 10, actual = 10)
	@JacobiEquals(expected = 11, actual = 11)
	public void shouldBeAbleToPredictPlayByOutlookModelUsingArgmax() {
		DataTable<YesOrNo> dataTab = new JacobiDataDef(this.workbook, ENUMS)
				.loadDef("weighted outlook and golf", YesOrNo.class)
				.apply(this.input, this.outcome);
		
		
		Column<?> outlookCol = dataTab.getColumns().get(0);
		NominalLikelihoodClassifier<YesOrNo> classifier = new NominalLikelihoodClassifier<>(
			dataTab.getOutcomeColumn(), outlookCol, this.model
		);
		
		for(int i = 0; i < outlookCol.cardinality(); i++){
			double[] features = new double[dataTab.getMatrix().getColCount()];
			features[i] = i;
			Assert.assertArrayEquals(this.model.getRow(i), 
				classifier.eval(features, dataTab.getOutcomeColumn()), 1e-12);
		}
	}
	
	@Test
	@JacobiImport("laplace outlook and golf")
	//@JacobiEquals(expected = 10, actual = 10)
	@JacobiEquals(expected = 11, actual = 11)
	public void shouldBeAbleToUseLaplaceSmoothingOnOutlookAgainstGolf() {
		DataTable<YesOrNo> dataTab = new JacobiDataDef(this.workbook, ENUMS)
				.loadDef("laplace outlook and golf", YesOrNo.class)
				.apply(this.input, this.outcome);
		
		Column<YesOrNo> outCol = dataTab.getOutcomeColumn();
		Column<?> outlookCol = dataTab.getColumns().get(0);
		
		Learner<YesOrNo> learner = new NominalLikelihoodClassifier.Learner<>(outlookCol);
		NominalLikelihoodClassifier<YesOrNo> classifier = learner.learn(dataTab, this.props.get(0, 0));
		
		this.likelihood = Matrices.zeros(outlookCol.cardinality(), dataTab.getOutcomeColumn().cardinality());
		for(int i = 0; i < this.likelihood.getRowCount(); i++){
			double[] features = new double[dataTab.getMatrix().getColCount()];
			features[outlookCol.getIndex()] = i;
			
			this.likelihood.getAndSet(i, r -> {
				double[] lnP = classifier.eval(features, outCol);
				for(int k = 0; k < r.length; k++){
					r[k] = Math.exp(lnP[k]);
				}
			});
		}
	}
	
	@Test
	@JacobiImport("unweighted windy and golf")
	//@JacobiEquals(expected = 10, actual = 10)
	@JacobiEquals(expected = 11, actual = 11)
	public void shouldBeAbleToUseLaplaceSmoothingOnDemandInOutlookAgainstGolf() {
		DataTable<YesOrNo> dataTab = new JacobiDataDef(this.workbook, ENUMS)
				.loadDef("unweighted windy and golf", YesOrNo.class)
				.apply(this.input, this.outcome);
		
		Column<?> windy = dataTab.getColumns().get(3);
		Column<YesOrNo> outCol = dataTab.getOutcomeColumn();
		
		Learner<YesOrNo> learner = new NominalLikelihoodClassifier.Learner<>(windy);
		NominalLikelihoodClassifier<YesOrNo> classifier = learner.learn(dataTab, -0.1);
		
		this.likelihood = Matrices.zeros(windy.cardinality(), dataTab.getOutcomeColumn().cardinality());
		for(int i = 0; i < this.likelihood.getRowCount(); i++){
			double[] features = new double[dataTab.getMatrix().getColCount()];
			features[windy.getIndex()] = i;
			
			this.likelihood.getAndSet(i, r -> {
				double[] lnP = classifier.eval(features, outCol);
				for(int k = 0; k < r.length; k++){
					r[k] = Math.exp(lnP[k]);
				}
			});
		}
	}
	
	@Test
	@JacobiImport("laplace outlook and golf")
	//@JacobiEquals(expected = 10, actual = 10)
	@JacobiEquals(expected = 11, actual = 11)
	public void shouldBeAbleToDisableLaplaceSmoothingOnDemandInOutlookAgainstGolf() {
		DataTable<YesOrNo> dataTab = new JacobiDataDef(this.workbook, ENUMS)
				.loadDef("laplace outlook and golf", YesOrNo.class)
				.apply(this.input, this.outcome);
		
		Column<YesOrNo> outCol = dataTab.getOutcomeColumn();
		Column<?> outlookCol = dataTab.getColumns().get(0);
		
		Learner<YesOrNo> learner = new NominalLikelihoodClassifier.Learner<>(outlookCol);
		NominalLikelihoodClassifier<YesOrNo> classifier = learner.learn(dataTab, -this.props.get(0, 0));
		
		this.likelihood = Matrices.zeros(outlookCol.cardinality(), dataTab.getOutcomeColumn().cardinality());
		for(int i = 0; i < this.likelihood.getRowCount(); i++){
			double[] features = new double[dataTab.getMatrix().getColCount()];
			features[outlookCol.getIndex()] = i;
			
			this.likelihood.getAndSet(i, r -> {
				double[] lnP = classifier.eval(features, outCol);
				for(int k = 0; k < r.length; k++){
					r[k] = Math.exp(lnP[k]);
				}
			});
		}
	}
	
	@Test
	@JacobiImport("test mock model")
	public void shouldBeAbleToUseTestMockModel() {
		Column<Integer> featCol = Column.nominal(0, this.model.getRowCount(), v -> (int) v);
		Column<Integer> outCol = Column.nominal(-1, this.model.getColCount(), v -> (int) v);
		
		NominalLikelihoodClassifier<Integer> classifier = new NominalLikelihoodClassifier<>(outCol, featCol, this.model);
		for(int i = 0; i < featCol.cardinality(); i++){
			double[] feat = new double[]{i};
			int ans = classifier.apply(feat);
			
			Assert.assertEquals(this.argmax(this.model.getRow(i)), ans);
		}
	}
	
	protected Matrix exp(Matrix matrix) {
		Matrix result = Matrices.zeros(matrix.getRowCount(), matrix.getColCount());
		for(int i = 0; i < result.getRowCount(); i++){
			double[] lnL = matrix.getRow(i);
			result.getAndSet(i, r -> {
				for(int j = 0; j < r.length; j++){
					r[j] = Math.exp(lnL[j]);
				}
			});
		}
		return result;
	}
	
	protected int argmax(double[] array) {
		int max = 0;
		for(int i = 1; i < array.length; i++){
			if(array[i] > array[max]){
				max = i;
			}
		}
		return max;
	}

}
