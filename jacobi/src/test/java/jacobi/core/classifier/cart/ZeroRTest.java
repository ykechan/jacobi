package jacobi.core.classifier.cart;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;
import java.util.function.DoubleToIntFunction;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.core.classifier.cart.data.Column;
import jacobi.core.classifier.cart.data.DataMatrix;
import jacobi.core.classifier.cart.data.DataTable;
import jacobi.core.classifier.cart.data.JacobiCsvDataTable;
import jacobi.core.classifier.cart.data.JacobiCsvDataTable.Outlook;
import jacobi.core.classifier.cart.data.JacobiCsvDataTable.YesOrNo;
import jacobi.core.classifier.cart.data.Sequence;
import jacobi.core.classifier.cart.node.DecisionNode;
import jacobi.core.util.Weighted;

public class ZeroRTest {

	@Test
	public void shouldBeAbleToLearnGolfDataOnPlayToYes() throws IOException {
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
		try(InputStream input = this.getClass().getResourceAsStream("/jacobi/test/data/golf.csv")){
			Matrix matrix = new JacobiCsvDataTable().encodeCsv(input, Arrays.asList(
				Outlook.class, double.class, double.class, boolean.class, YesOrNo.class
			), true);

			DoubleToIntFunction flr = v -> (int) Math.floor(v);
			
			DataTable<Boolean> dataTab = DataMatrix.of(matrix, 
				new TreeSet<>(Arrays.asList(
					new Column<>(0, Arrays.asList(Outlook.values()), flr),
					Column.numeric(1),
					Column.numeric(2),
					Column.signed(3),
					new Column<>(4, Arrays.asList(YesOrNo.values()), flr)
				)), 
				Column.signed(3)
			);
			
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
		try(InputStream input = this.getClass().getResourceAsStream("/jacobi/test/data/golf.csv")){
			Matrix matrix = new JacobiCsvDataTable().encodeCsv(input, Arrays.asList(
				Outlook.class, double.class, double.class, boolean.class, YesOrNo.class
			), true);

			DoubleToIntFunction flr = v -> (int) Math.floor(v);
			
			DataTable<Outlook> dataTab = DataMatrix.of(matrix, 
				new TreeSet<>(Arrays.asList(
					new Column<>(0, Arrays.asList(Outlook.values()), flr),
					Column.numeric(1),
					Column.numeric(2),
					Column.signed(3),
					new Column<>(4, Arrays.asList(YesOrNo.values()), flr)
				)), 
				new Column<>(0, Arrays.asList(Outlook.values()), flr)
			);
			
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
