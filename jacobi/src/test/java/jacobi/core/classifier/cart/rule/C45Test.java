package jacobi.core.classifier.cart.rule;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import jacobi.core.classifier.cart.data.DataTable;
import jacobi.core.classifier.cart.util.JacobiDefCsvReader;
import jacobi.core.classifier.cart.util.JacobiEnums.YesOrNo;

public class C45Test {
	
	@Test
	public void testShouldBeAbleToSortGolfData() throws IOException {
		try(InputStream input = this.getClass().getResourceAsStream("/jacobi/test/data/golf.def.csv")){
			DataTable<YesOrNo> dataTab = new JacobiDefCsvReader()
					.read(input, YesOrNo.class);
			
			
		}
	}

}
