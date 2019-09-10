package jacobi.core.classifier.cart.measure;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;

import jacobi.api.Matrix;
import jacobi.api.classifier.Column;
import jacobi.api.classifier.DataTable;
import jacobi.api.classifier.Instance;
import jacobi.core.classifier.cart.ArraySequence;
import jacobi.core.classifier.cart.measure.Impurity;
import jacobi.core.classifier.cart.measure.RankedBinaryPartition;
import jacobi.core.impl.ColumnVector;
import jacobi.core.util.Weighted;

public class RankedBinaryPartitionTest {
	
	@Test
	public void shouldBeAbleToBreakAtPurePartition() {
		Weighted<double[]> split = new RankedBinaryPartition(Impurity.ENTROPY)
			.measure(
				this.mock(
					new double[] {1.0, 2.0, 3.0, 4.0, 5.0}, 
					Arrays.asList(Boolean.FALSE, Boolean.FALSE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE)
				), 
				Column.numeric(0), 
				this.defaultSeq(5)
			);
		
		Assert.assertEquals(0.0, split.weight, 1e-12);
		Assert.assertEquals(1, split.item.length, 1e-12);
		Assert.assertEquals(2.5, split.item[0], 1e-12);
	}
	
	@Test
	public void shouldBeAbleToBreakAtPurePartitionBySequence() {
		Weighted<double[]> split = new RankedBinaryPartition(Impurity.ENTROPY)
			.measure(
				this.mock(
					new double[] {5.0, 1.0, 3.0, 4.0, 2.0}, 
					Arrays.asList(Boolean.TRUE, Boolean.FALSE, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE)
				), 
				Column.numeric(0), 
				new ArraySequence(new int[] {1, 4, 2, 3, 0}, 0, 5)
			);
		
		Assert.assertEquals(0.0, split.weight, 1e-12);
		Assert.assertEquals(1, split.item.length, 1e-12);
		Assert.assertEquals(2.5, split.item[0], 1e-12);
	}
	
	@Test
	public void shouldBeAbleToReturnNaNAtPureOutcome() {
		Weighted<double[]> split = new RankedBinaryPartition(Impurity.ENTROPY)
				.measure(
					this.mock(
						new double[] {5.0, 1.0, 3.0, 4.0, 2.0}, 
						Arrays.asList(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE)
					), 
					Column.numeric(0), 
					new ArraySequence(new int[] {1, 4, 2, 3, 0}, 0, 5)
				);
			
		Assert.assertTrue(Double.isNaN(split.weight));
		Assert.assertEquals(0, split.item.length);
	}
	
	protected <T> DataTable<T> mockWeighted(double[] values, List<Weighted<T>> list) {
		List<T> items = list.stream()
				.map(i -> i.item)
				.distinct().sorted().collect(Collectors.toList());
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
					.mapToObj(i -> new Instance(i, 
						items.indexOf(list.get(i).item), 
						list.get(i).weight))
					.collect(Collectors.toList());
			}
			
		};
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
	
	protected ArraySequence defaultSeq(int len) {
		return new ArraySequence(IntStream.range(0, len).toArray(), 0, len);
	}

}
