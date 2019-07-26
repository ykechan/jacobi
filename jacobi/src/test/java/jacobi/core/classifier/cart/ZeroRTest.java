package jacobi.core.classifier.cart;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;

import jacobi.core.classifier.cart.data.DataTable;
import jacobi.core.classifier.cart.data.Sequence;
import jacobi.core.classifier.cart.node.DecisionNode;
import jacobi.core.classifier.cart.util.JacobiDefCsvDataTable;
import jacobi.core.classifier.cart.util.JacobiEnums.Outlook;
import jacobi.core.classifier.cart.util.JacobiEnums.YesOrNo;
import jacobi.core.util.Weighted;

public class ZeroRTest {

	@Test
	public void shouldBeAbleToLearnGolfDataOnPlayToYes() throws IOException {
		try(InputStream input = this.getClass().getResourceAsStream("/jacobi/test/data/golf.def.csv")){
			DataTable<YesOrNo> dataTab = new JacobiDefCsvDataTable()
					.read(input, YesOrNo.class);
			
			Weighted<DecisionNode<YesOrNo>> ans = new ZeroR(dist -> { 
					Assert.assertEquals(9.0, dist[0], 1e-12);
					Assert.assertEquals(5.0, dist[1], 1e-12);
					return Math.PI;
				})
				.make(dataTab, Collections.emptySet(), this.defaultSeq(dataTab.size()));
			
			Assert.assertEquals(Math.PI, ans.weight, 1e-12);
			Assert.assertEquals(YesOrNo.YES, ans.item.decide());
		}
	}
	
	@Test
	public void shouldBeAbleToLearnGolfDataOnWindToFalse() throws IOException {
		try(InputStream input = this.getClass().getResourceAsStream("/jacobi/test/data/golf.def.csv")){
			DataTable<Boolean> dataTab = new JacobiDefCsvDataTable()
					.read(input, Boolean.class);
			
			Weighted<DecisionNode<Boolean>> ans = new ZeroR(dist -> { 
					Assert.assertEquals(8.0, dist[0], 1e-12);
					Assert.assertEquals(6.0, dist[1], 1e-12);
					return Math.PI;
				})
				.make(dataTab, Collections.emptySet(), this.defaultSeq(dataTab.size()));
			
			Assert.assertEquals(Math.PI, ans.weight, 1e-12);
			Assert.assertFalse(ans.item.decide());
		}
	}
	
	@Test
	public void shouldBeAbleToLearnGolfDataOnOutcaseToSunny() throws IOException {
		try(InputStream input = this.getClass().getResourceAsStream("/jacobi/test/data/golf.def.csv")){
			DataTable<Outlook> dataTab = new JacobiDefCsvDataTable()
					.read(input, Outlook.class);
			
			Weighted<DecisionNode<Outlook>> ans = new ZeroR(dist -> { 
					Assert.assertEquals(5.0, dist[0], 1e-12); // sunny
					Assert.assertEquals(4.0, dist[1], 1e-12); // overcast
					Assert.assertEquals(5.0, dist[2], 1e-12); // rain
					return Math.PI;
				})
				.make(dataTab, Collections.emptySet(), this.defaultSeq(dataTab.size()));
			
			Assert.assertEquals(Math.PI, ans.weight, 1e-12);
			Assert.assertEquals(Outlook.SUNNY, ans.item.decide());
		}
	}
	
	protected Sequence defaultSeq(int len) {
		return new Sequence(IntStream.range(0, len).toArray(), 0, len);
	}

}
