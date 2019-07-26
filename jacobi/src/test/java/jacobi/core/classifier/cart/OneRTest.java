package jacobi.core.classifier.cart;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;

import jacobi.core.classifier.cart.data.Column;
import jacobi.core.classifier.cart.data.DataTable;
import jacobi.core.classifier.cart.data.Sequence;
import jacobi.core.classifier.cart.node.DecisionNode;
import jacobi.core.classifier.cart.util.JacobiDefCsvDataTable;
import jacobi.core.classifier.cart.util.JacobiEnums.YesOrNo;
import jacobi.core.util.Weighted;

public class OneRTest {
	
	@Test
	public void shouldBeAbleToLearnGolfDataOnOutlook() throws IOException {
		try(InputStream input = this.getClass().getResourceAsStream("/jacobi/test/data/golf.def.csv")){
			DataTable<YesOrNo> dataTab = new JacobiDefCsvDataTable()
					.read(input, YesOrNo.class);
			
			Weighted<DecisionNode<YesOrNo>> ans = new OneR(
					new ZeroR(Impurity.ENTROPY), 
					new NominalPartition(Impurity.ENTROPY)
				).make(dataTab, 
					this.columnSet(
						dataTab.getColumns().get(0),
						dataTab.getColumns().get(3)
					), 
					this.defaultSeq(dataTab.size())
				);
			
			/*
			 * sunny,85,85,false,no
				sunny,80,90,true,no
				overcast,83,78,false,yes
				rain,70,96,false,yes
				rain,68,80,false,yes
				rain,65,70,true,no
				overcast,64,65,true,yes
				sunny,72,95,false,no
				sunny,69,70,false,yes
				rain,75,80,false,yes
				sunny,75,70,true,yes
				overcast,72,90,true,yes
				overcast,81,75,false,yes
				rain,71,80,true,no
			 */
			// sunny: yes(2) no(3)
			// overcast: yes(4) no(0)
			// rain: yes(3) no(2)
			
			Assert.assertEquals(
				  5 * Impurity.ENTROPY.of(new double[] {2, 3})
				+ 4 * Impurity.ENTROPY.of(new double[] {4, 0})
				+ 5 * Impurity.ENTROPY.of(new double[] {3, 2}), ans.weight, 1e-12);
			
			
		}
	}
	
	protected Sequence defaultSeq(int len) {
		return new Sequence(IntStream.range(0, len).toArray(), 0, len);
	}
	
	protected Set<Column<?>> columnSet(Column<?>... cols) {
		Set<Column<?>> set = new TreeSet<>();
		for(Column<?> col : cols) {
			set.add(col);
		}
		return set;
	}

}
