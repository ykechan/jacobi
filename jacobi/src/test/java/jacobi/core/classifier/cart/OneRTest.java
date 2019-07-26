package jacobi.core.classifier.cart;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.IntStream;

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
			
			System.out.println(ans.weight);
			System.out.println(ans.item.split().getIndex());
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
