package jacobi.core.classifier.cart;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.DoubleToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;

import jacobi.api.Matrix;
import jacobi.core.classifier.cart.data.Column;
import jacobi.core.classifier.cart.data.DataMatrix;
import jacobi.core.classifier.cart.data.DataTable;
import jacobi.core.classifier.cart.data.JacobiCsvDataTable;
import jacobi.core.classifier.cart.data.Sequence;
import jacobi.core.classifier.cart.data.JacobiCsvDataTable.Outlook;
import jacobi.core.classifier.cart.data.JacobiCsvDataTable.YesOrNo;
import jacobi.core.classifier.cart.node.DecisionNode;
import jacobi.core.util.Weighted;

public class OneRTest {
	
	@Test
	public void shouldBeAbleToLearnGolfDataOnOutlook() throws IOException {
		try(InputStream input = this.getClass().getResourceAsStream("/jacobi/test/data/golf.csv")){
			Matrix matrix = new JacobiCsvDataTable().encodeCsv(input, Arrays.asList(
				Outlook.class, double.class, double.class, boolean.class, YesOrNo.class
			), true);

			DoubleToIntFunction flr = v -> (int) Math.floor(v);
			
			DataTable<YesOrNo> dataTab = DataMatrix.of(matrix, 
				new TreeSet<>(Arrays.asList(
					new Column<>(0, Arrays.asList(Outlook.values()), flr),
					Column.numeric(1),
					Column.numeric(2),
					new Column<>(3, Arrays.asList(Boolean.FALSE, Boolean.TRUE), v -> v > 0 ? 1 : 0),
					new Column<>(4, Arrays.asList(YesOrNo.values()), flr)
				)), 
				new Column<>(4, Arrays.asList(YesOrNo.values()), flr)
			);
			
			
			Weighted<DecisionNode<YesOrNo>> ans = new OneR(
					new ZeroR(Impurity.ENTROPY), 
					new Partition<Void>() {

						@Override
						public Weighted<Void> measure(DataTable<?> table, Column<?> target, Sequence seq) {
							switch(target.getIndex()) {
								case 0:
									return new NominalPartition(dist -> {
										return Impurity.ENTROPY.of(dist);
									}).measure(table, target, seq);
								case 3:
									return new NominalPartition(dist -> {
										return Impurity.ENTROPY.of(dist);
									}).measure(table, target, seq);
								default:
									break;
							}
							throw new IllegalArgumentException("Unexpected column #" 
									+ target.getIndex());
						}
						
					}
				).make(dataTab, 
					this.columnSet(
						new Column<>(0, Arrays.asList(Outlook.values()), flr),
						new Column<>(3, Arrays.asList(Boolean.FALSE, Boolean.TRUE), v -> v > 0 ? 1 : 0)
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
