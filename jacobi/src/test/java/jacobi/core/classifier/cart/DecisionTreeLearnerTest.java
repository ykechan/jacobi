package jacobi.core.classifier.cart;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import jacobi.api.Matrix;
import jacobi.api.classifier.DataTable;
import jacobi.api.classifier.cart.DecisionNode;
import jacobi.api.classifier.cart.DecisionTreeParams;
import jacobi.core.classifier.cart.measure.Impurity;
import jacobi.core.classifier.cart.node.BinaryNumericSplit;
import jacobi.core.classifier.cart.node.DecisionNodeSerializer;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.util.JacobiDataDef;
import jacobi.test.util.JacobiJUnit4ClassRunner;

@RunWith(JacobiJUnit4ClassRunner.class)
@JacobiImport("/jacobi/test/data/DecisionTreeLearnerTest.xlsx")
public class DecisionTreeLearnerTest {
	
	@JacobiInject(0)
	public Matrix input;
	
	@JacobiInject(1)
	public Matrix outcome;
	
	public Workbook workbook;
	
	@Before
	public void init() throws IOException, EncryptedDocumentException, InvalidFormatException {
		try(InputStream input = this.getClass().getResourceAsStream(
				"/jacobi/test/data/DecisionTreeLearnerTest.xlsx")){
			this.workbook = WorkbookFactory.create(input);
		}
	}
	
	@Test
	@JacobiImport("golf")
	public void shouldBeAbleToTrainZeroROnGolfData() {
		DataTable<String> dataTab = new JacobiDataDef(this.workbook, Collections.emptyList())
			.loadDef("golf", String.class)
			.apply(this.input, this.outcome);
		
		DecisionNode<String> root = new DecisionTreeLearner<String>()
			.learn(dataTab, new DecisionTreeParams(Impurity.ERROR, 0));
		
		Assert.assertFalse(root.decide(1.0).isPresent());
		Assert.assertEquals("YES", root.decide());
	}
	
	@Test
	@JacobiImport("golf")
	public void shouldBeAbleToTrainOneROnGolfData() {
		DataTable<String> dataTab = new JacobiDataDef(this.workbook, Collections.emptyList())
			.loadDef("golf", String.class)
			.apply(this.input, this.outcome);
		
		DecisionNode<String> root = new DecisionTreeLearner<String>()
			.learn(dataTab, new DecisionTreeParams(Impurity.ENTROPY, 1));
		
		System.out.println(new DecisionNodeSerializer().toJson(root));
	}
	
	@Test
	@JacobiImport("golf")
	public void shouldBeAbleToTrain2ROnGolfData() {
		DataTable<String> dataTab = new JacobiDataDef(this.workbook, Collections.emptyList())
			.loadDef("golf", String.class)
			.apply(this.input, this.outcome);
		
		DecisionNode<String> root = new DecisionTreeLearner<String>()
			.learn(dataTab, new DecisionTreeParams(Impurity.ENTROPY, 2));
		
		System.out.println(new DecisionNodeSerializer().toJson(root));
	}
	
	@Test
	@JacobiImport("iris")
	public void shouldBeAbleToTrainZeroROnIrisData() {
		DataTable<String> dataTab = new JacobiDataDef(this.workbook, Collections.emptyList())
			.loadDef("iris", String.class)
			.apply(this.input, this.outcome);
		
		DecisionNode<String> root = new DecisionTreeLearner<String>()
			.learn(dataTab, new DecisionTreeParams(Impurity.ERROR, 0));
		
		System.out.println(new DecisionNodeSerializer().toJson(root));
	}
	
	@Test
	@JacobiImport("iris")
	public void shouldBeAbleToTrainOneROnIrisData() {
		DataTable<String> dataTab = new JacobiDataDef(this.workbook, Collections.emptyList())
			.loadDef("iris", String.class)
			.apply(this.input, this.outcome);
		
		DecisionNode<String> root = new DecisionTreeLearner<String>()
			.learn(dataTab, new DecisionTreeParams(Impurity.ENTROPY, 1));
		
		Assert.assertTrue(root instanceof BinaryNumericSplit);
		
	}
	
	@Test
	@JacobiImport("iris")
	public void shouldBeAbleToTrain2ROnIrisData() {
		DataTable<String> dataTab = new JacobiDataDef(this.workbook, Collections.emptyList())
			.loadDef("iris", String.class)
			.apply(this.input, this.outcome);
		
		DecisionNode<String> root = new DecisionTreeLearner<String>()
			.learn(dataTab, new DecisionTreeParams(Impurity.ERROR, 2));
		
		System.out.println(new DecisionNodeSerializer().toJson(root));
	}

}
