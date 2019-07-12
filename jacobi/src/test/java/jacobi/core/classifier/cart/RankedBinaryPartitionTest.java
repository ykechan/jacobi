package jacobi.core.classifier.cart;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Test;

import jacobi.api.Matrix;
import jacobi.core.classifier.cart.data.Column;
import jacobi.core.classifier.cart.data.DataTable;
import jacobi.core.classifier.cart.data.Instance;
import jacobi.core.impl.ColumnVector;

public class RankedBinaryPartitionTest {
	
	@Test
	public void shouldBeAbleToBreakAtPurePartition() {
		
	}
	
	protected <T> DataTable<T> mock(double[] values, List<T> list) {
		List<T> items = list.stream().distinct().sorted().collect(Collectors.toList());
		Column<T> outCol = new Column<>(1, items, v -> (int) v);
		return new DataTable<T>() {

			@Override
			public List<Column<?>> getColumns() {
				return Collections.singletonList(Column.numeric(1));
			}

			@Override
			public Column<T> getOutcomeColumn() {
				return outCol;
			}

			@Override
			public Matrix getMatrix() {
				return new ColumnVector(values);
			}

			@Override
			public List<Instance> getInstances(Column<?> column) {
				return IntStream.range(0, list.size())
					.mapToObj(i -> new Instance(i, items.indexOf(list.get(i)), 1.0))
					.collect(Collectors.toList());
			}
			
		};
	}

}
