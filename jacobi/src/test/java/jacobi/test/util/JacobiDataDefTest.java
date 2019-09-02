package jacobi.test.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
import jacobi.api.classifier.Instance;
import jacobi.core.classifier.cart.util.JacobiEnums.Outlook;
import jacobi.core.classifier.cart.util.JacobiEnums.YesOrNo;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;

@RunWith(JacobiJUnit4ClassRunner.class)
@JacobiImport("/jacobi/test/data/JacobiDataDefTest.xlsx")
public class JacobiDataDefTest {
	
	private static List<Class<?>> ENUMS = Arrays.asList(
		Outlook.class, YesOrNo.class
	);
	
	private Workbook workbook;
	
	@JacobiInject(0)
	public Matrix input;
	
	@JacobiInject(1)
	public Matrix outcome;
	
	@Before
	public void init() throws IOException, EncryptedDocumentException, InvalidFormatException {
		try(InputStream input = this.getClass().getResourceAsStream(
				"/jacobi/test/data/JacobiDataDefTest.xlsx")){
			this.workbook = WorkbookFactory.create(input);
		}
	}
	
	@Test
	public void shouldBeAbleToReadColumnDefsInGolf() {
		Map<Integer, Column<?>> colDef = new JacobiDataDef(this.workbook, ENUMS)
				.getColumnDefs("golf");
		
		Column<?> col = colDef.get(0);
		Assert.assertNotNull(col);
		Assert.assertEquals(Outlook.class.getEnumConstants().length, col.cardinality());
		for(Outlook val : Outlook.values()) {
			Assert.assertEquals(val, col.valueOf(val.ordinal()));
		}
		
		col = colDef.get(2);
		Assert.assertNotNull(col);
		Assert.assertEquals(Outlook.class.getEnumConstants().length, col.cardinality());
		for(Outlook val : Outlook.values()) {
			Assert.assertEquals(val, col.valueOf(val.ordinal()));
		}
		
	}
	
	@Test
	@JacobiImport("golf")
	public void shouldBeAbleToCombineDataAndDefInGolf() {
		DataTable<YesOrNo> dataTab = new JacobiDataDef(this.workbook, ENUMS)
				.loadDef("golf", YesOrNo.class)
				.apply(this.input, this.outcome);
		
		List<Instance> insts = dataTab.getInstances(dataTab.getColumns().get(0));
			
		Assert.assertEquals(14, insts.size());
		this.assertEquals(new Instance(0, 1, 1.0), insts.get(0), 1e-12);
		this.assertEquals(new Instance(0, 1, 1.0), insts.get(1), 1e-12);
		this.assertEquals(new Instance(1, 0, 1.0), insts.get(2), 1e-12);
		this.assertEquals(new Instance(2, 0, 1.0), insts.get(3), 1e-12);
		this.assertEquals(new Instance(2, 0, 1.0), insts.get(4), 1e-12);
		this.assertEquals(new Instance(2, 1, 1.0), insts.get(5), 1e-12);
		this.assertEquals(new Instance(1, 0, 1.0), insts.get(6), 1e-12);
		this.assertEquals(new Instance(0, 1, 1.0), insts.get(7), 1e-12);
		this.assertEquals(new Instance(0, 0, 1.0), insts.get(8), 1e-12);
		this.assertEquals(new Instance(2, 0, 1.0), insts.get(9), 1e-12);
		this.assertEquals(new Instance(0, 0, 1.0), insts.get(10), 1e-12);
		this.assertEquals(new Instance(1, 0, 1.0), insts.get(11), 1e-12);
		this.assertEquals(new Instance(1, 0, 1.0), insts.get(12), 1e-12);
		this.assertEquals(new Instance(2, 1, 1.0), insts.get(13), 1e-12);
	}
	
	protected void assertEquals(Instance expected, Instance actual, double eps) {
		Assert.assertEquals(expected.feature, actual.feature);
		Assert.assertEquals(expected.outcome, actual.outcome);
		Assert.assertEquals(expected.weight, actual.weight, eps);
	}

}
