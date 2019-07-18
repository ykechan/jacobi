package jacobi.core.classifier.cart;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import jacobi.api.Matrix;
import jacobi.core.classifier.cart.data.Column;
import jacobi.core.classifier.cart.data.DataTable;
import jacobi.core.classifier.cart.data.Instance;

public class NominalPartitionTest {
	
	protected <T> DataTable<T> mock(List<Object[]> pairs, int goalIndex) {
		if(pairs.isEmpty()) {
			throw new IllegalArgumentException();
		}
		
		Object[] first = pairs.get(0);
		if(first.length == 0) {
			throw new IllegalArgumentException();
		}
		// ...
		return new DataTable<T>() {

			@Override
			public List<Column<?>> getColumns() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Column<T> getOutcomeColumn() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Matrix getMatrix() {
				throw new UnsupportedOperationException();
			}

			@Override
			public List<Instance> getInstances(Column<?> column) {
				// TODO Auto-generated method stub
				return null;
			}
			
		};
	}
	
	protected Column<?> createColumn(int index, List<?> items) {
		if(items.isEmpty()) {
			throw new IllegalArgumentException("No item.");
		}
		Object item = items.get(0);
		for(Object i : items) {
			if(!item.getClass().isInstance(i)){
				throw new IllegalArgumentException("Inconsistent value type "
					+ item + " and " + i);
			}
		}
		
		return new Column<>(index, 
			item.getClass().isEnum()
			? Arrays.asList(item.getClass().getEnumConstants())
			: item instanceof String
				? items.stream()
					.map(s -> s.toString().trim().toUpperCase())
					.distinct()
					.collect(Collectors.toList())
				: items.stream().distinct().collect(Collectors.toList()), 
			v -> (int) Math.floor(v));
	}

}
