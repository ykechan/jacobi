package jacobi.core.classifier.cart.rule;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;

import jacobi.api.classifier.DataTable;
import jacobi.api.classifier.cart.DecisionNode;
import jacobi.core.classifier.cart.ArraySequence;
import jacobi.core.classifier.cart.util.JacobiDefCsvReader;
import jacobi.core.classifier.cart.util.JacobiEnums.Outlook;
import jacobi.core.classifier.cart.util.JacobiEnums.YesOrNo;

public class ZeroRTest {

	@Test
	public void shouldBeAbleToLearnGolfDataOnPlayToYes() throws IOException {
		try(InputStream input = this.getClass().getResourceAsStream("/jacobi/test/data/golf.def.csv")){
			DataTable<YesOrNo> dataTab = new JacobiDefCsvReader()
					.read(input, YesOrNo.class);
			DecisionNode<YesOrNo> ans = new ZeroR() {

				@Override
				protected int argmax(double[] dist) {
					Assert.assertEquals(9.0, dist[0], 1e-12);
					Assert.assertEquals(5.0, dist[1], 1e-12);
					return super.argmax(dist);
				}
				
			}.make(dataTab, Collections.emptySet(), this.defaultSeq(dataTab.size()));
			Assert.assertEquals(YesOrNo.YES, ans.decide());
		}
	}
	
	@Test
	public void shouldBeAbleToLearnGolfDataOnWindToFalse() throws IOException {
		try(InputStream input = this.getClass().getResourceAsStream("/jacobi/test/data/golf.def.csv")){
			DataTable<Boolean> dataTab = new JacobiDefCsvReader()
					.read(input, Boolean.class);
			
			DecisionNode<Boolean> ans = new ZeroR() {

				@Override
				protected int argmax(double[] dist) {
					Assert.assertEquals(8.0, dist[0], 1e-12);
					Assert.assertEquals(6.0, dist[1], 1e-12);
					return super.argmax(dist);
				}
				
			}.make(dataTab, Collections.emptySet(), this.defaultSeq(dataTab.size()));
			
			Assert.assertFalse(ans.decide());
		}
	}
	
	@Test
	public void shouldBeAbleToLearnGolfDataOnOutcaseToSunny() throws IOException {
		try(InputStream input = this.getClass().getResourceAsStream("/jacobi/test/data/golf.def.csv")){
			DataTable<Outlook> dataTab = new JacobiDefCsvReader()
					.read(input, Outlook.class);
			
			DecisionNode<Outlook> ans = new ZeroR() {

				@Override
				protected int argmax(double[] dist) {
					Assert.assertEquals(5.0, dist[0], 1e-12); // sunny
					Assert.assertEquals(4.0, dist[1], 1e-12); // overcast
					Assert.assertEquals(5.0, dist[2], 1e-12); // rain
					return super.argmax(dist);
				}
				
			}.make(dataTab, Collections.emptySet(), this.defaultSeq(dataTab.size()));
			
			Assert.assertEquals(Outlook.SUNNY, ans.decide());
		}
	}
	
	protected ArraySequence defaultSeq(int len) {
		return new ArraySequence(IntStream.range(0, len).toArray(), 0, len);
	}

}
