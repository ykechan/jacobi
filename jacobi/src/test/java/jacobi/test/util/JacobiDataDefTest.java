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

import jacobi.api.classifier.Column;
import jacobi.core.classifier.cart.util.JacobiEnums.Outlook;
import jacobi.core.classifier.cart.util.JacobiEnums.YesOrNo;

public class JacobiDataDefTest {
	
	private static List<Class<?>> ENUMS = Arrays.asList(
		Outlook.class, YesOrNo.class
	);
	
	private Workbook workbook;
	
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

}
