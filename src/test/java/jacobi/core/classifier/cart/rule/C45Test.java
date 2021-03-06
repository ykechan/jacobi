package jacobi.core.classifier.cart.rule;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;

import jacobi.api.Matrix;
import jacobi.api.classifier.Column;
import jacobi.api.classifier.DataTable;
import jacobi.api.classifier.cart.DecisionNode;
import jacobi.core.classifier.cart.ArraySequence;
import jacobi.core.classifier.cart.Sequence;
import jacobi.core.classifier.cart.measure.Impurity;
import jacobi.core.classifier.cart.measure.Partition;
import jacobi.core.classifier.cart.measure.RankedBinaryPartition;
import jacobi.core.classifier.cart.node.BinaryNumericSplit;
import jacobi.core.classifier.cart.node.Decision;
import jacobi.core.classifier.cart.node.DecisionNodeSerializer;
import jacobi.core.classifier.cart.util.JacobiDefCsvReader;
import jacobi.core.classifier.cart.util.JacobiEnums.Iris;
import jacobi.core.classifier.cart.util.JacobiEnums.YesOrNo;
import jacobi.core.util.Weighted;

public class C45Test {
	
	@Test
	public void testShouldBeAbleToSortGolfData() throws IOException {
		try(InputStream input = this.getClass().getResourceAsStream("/jacobi/test/data/golf.def.csv")){
			DataTable<YesOrNo> dataTab = new JacobiDefCsvReader().read(input, YesOrNo.class);
			
			AtomicInteger count = new AtomicInteger(0);
			
			new C45(new Partition() {

				@Override
				public Weighted<double[]> measure(DataTable<?> table, Column<?> target, Sequence seq) {
					if(!target.isNumeric()){
						return null;
					}
					count.incrementAndGet();
					Matrix mat = table.getMatrix();
					int col = target.getIndex();
					
					for(int i = 1; i < seq.length(); i++) {
						Assert.assertFalse(mat.get(seq.indexAt(i - 1), col) > mat.get(seq.indexAt(i), col));
					}
					return null;
				}
				
			}, this::mock).make(
				dataTab, 
				new TreeSet<>(dataTab.getColumns()), 
				this.defaultSeq(dataTab.size()));
			
			Assert.assertEquals(2, count.get());
		}
	}
	
	@Test
	public void testShouldBeAbleToSortIrisData() throws IOException {
		try(InputStream input = this.getClass().getResourceAsStream("/jacobi/test/data/iris.def.csv")){
			DataTable<Iris> dataTab = new JacobiDefCsvReader().read(input, Iris.class);
			
			DecisionNode<Iris> root = C45.of(new RankedBinaryPartition(Impurity.ENTROPY)).make(
				dataTab, 
				new TreeSet<>(dataTab.getColumns()), 
				this.defaultSeq(dataTab.size())
			);
			
			Assert.assertTrue(root instanceof BinaryNumericSplit);
			Assert.assertTrue(2.0 < ((BinaryNumericSplit<?>) root).getThreshold());
			Assert.assertTrue(4.7 > ((BinaryNumericSplit<?>) root).getThreshold());
			
			Assert.assertTrue(((BinaryNumericSplit<?>) root).getLeft() instanceof Decision);
			Assert.assertEquals(Iris.SETOSA, ((BinaryNumericSplit<?>) root).getLeft().decide());
			
			System.out.println(new DecisionNodeSerializer().toJson(root));
		}
	}	
	
	protected Rule mock(Partition part) {
		return new Rule() {

			@Override
			public <T> DecisionNode<T> make(DataTable<T> dataTable, 
					Set<Column<?>> features, 
					Sequence seq) {
				for(Column<?> target : features) {
					part.measure(dataTable, target, seq);
				}
				return null;
			}
			
		};
	}
	
	protected Sequence defaultSeq(int len) {
        return new ArraySequence(IntStream.range(0, len).toArray(), 0, len);
    }
    
    protected Set<Column<?>> columnSet(Column<?>... cols) {
        Set<Column<?>> set = new TreeSet<>();
        for(Column<?> col : cols) {
            set.add(col);
        }
        return set;
    }

}
