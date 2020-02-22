package jacobi.core.classifier;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.api.classifier.Column;
import jacobi.api.classifier.DataTable;
import jacobi.api.classifier.Instance;

public class ReweightableTest {
	
	@Test
	public void shouldBeAbleToCastIfGivenReweightable() {
		DataTable<?> dataTab = new ReweightableDataTable<Object>();
		Assert.assertTrue(dataTab == Reweightable.of(dataTab));
	}
	
	@Test
	public void shouldBeAbleToReweightOnTheFly() {
		DataTable<String> dataTab = new DataTable<String>() {

			@Override
			public List<Column<?>> getColumns() {
				return Arrays.asList(
					Column.signed(0),
					Column.signed(1),
					Column.signed(2)
				);
			}

			@Override
			public Column<String> getOutcomeColumn() {
				return Column.of(-1, Arrays.asList("A", "B", "C"));
			}

			@Override
			public Matrix getMatrix() {
				return Matrices.identity(3);
			}

			@Override
			public List<Instance> getInstances(Column<?> column) {
				return Arrays.asList(
					new Instance(0, 0, 1.0),
					new Instance(1, 1, 2.0),
					new Instance(0, 2, 3.0)
				);
			}
			
		};
		
		Assert.assertArrayEquals(Arrays.asList(
			new Instance(0, 0, 1.0),
			new Instance(1, 1, 2.0),
			new Instance(0, 2, 3.0)
		).toArray(new Instance[0]), dataTab.getInstances(Column.signed(0)).toArray(new Instance[0]));
		
		Assert.assertArrayEquals(Arrays.asList(
			new Instance(0, 0, Math.PI),
			new Instance(1, 1, Math.E),
			new Instance(0, 2, 7.8)
		).toArray(new Instance[0]), Reweightable.of(dataTab)
			.reweight(new double[] { Math.PI, Math.E, 7.8 })
			.getInstances(Column.signed(0)).toArray(new Instance[0]));
	}
	
	@Test
	public void shouldBeAbleToRetainColumnDefs() {
		DataTable<String> dataTab = new DataTable<String>() {

			@Override
			public List<Column<?>> getColumns() {
				return Arrays.asList(
					Column.signed(0),
					Column.signed(1),
					Column.signed(2)
				);
			}

			@Override
			public Column<String> getOutcomeColumn() {
				return Column.of(-1, Arrays.asList("A", "B", "C"));
			}

			@Override
			public Matrix getMatrix() {
				return Matrices.identity(3);
			}

			@Override
			public List<Instance> getInstances(Column<?> column) {
				return Arrays.asList(
					new Instance(0, 0, 1.0),
					new Instance(1, 1, 2.0),
					new Instance(0, 2, 3.0)
				);
			}
			
		};
		
		DataTable<String> reweight = Reweightable.of(dataTab).reweight(new double[] {
			Math.E, Math.PI, 3.7
		});
		
		Assert.assertArrayEquals(
			dataTab.getColumns().toArray(), 
			reweight.getColumns().toArray()
		);
		
		Assert.assertEquals(dataTab.getOutcomeColumn(), reweight.getOutcomeColumn());				
	}
	
	protected static class ReweightableDataTable<T> implements DataTable<T>, Reweightable<T> {

		@Override
		public DataTable<T> reweight(double[] weights) {
			return null;
		}

		@Override
		public List<Column<?>> getColumns() {
			return null;
		}

		@Override
		public Column<T> getOutcomeColumn() {
			return null;
		}

		@Override
		public Matrix getMatrix() {
			return null;
		}

		@Override
		public List<Instance> getInstances(Column<?> column) {
			return null;
		}
		
	}

}
