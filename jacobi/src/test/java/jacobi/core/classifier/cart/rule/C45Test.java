package jacobi.core.classifier.cart.rule;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
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
import jacobi.core.classifier.cart.node.NominalSplit;
import jacobi.core.classifier.cart.util.JacobiDefCsvReader;
import jacobi.core.classifier.cart.util.JacobiEnums.Iris;
import jacobi.core.classifier.cart.util.JacobiEnums.YesOrNo;
import jacobi.core.util.Weighted;

public class C45Test {
	
	@Test
	public void testShouldBeAbleToSortGolfData() throws IOException {
		try(InputStream input = this.getClass().getResourceAsStream("/jacobi/test/data/golf.def.csv")){
			DataTable<YesOrNo> dataTab = new JacobiDefCsvReader()
					.read(input, YesOrNo.class);
			
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
			DataTable<Iris> dataTab = new JacobiDefCsvReader()
					.read(input, Iris.class);
			
			DecisionNode<Iris> root = C45.of(new RankedBinaryPartition(Impurity.ENTROPY)).make(
				dataTab, 
				new TreeSet<>(dataTab.getColumns()), 
				this.defaultSeq(dataTab.size())
			);
			
			System.out.println(this.toJson(root));
		}
	}
	
	@SuppressWarnings("unchecked")
	protected String toJson(DecisionNode<?> node) {
		if(node.split() == null){
			return "\"" + node.decide() + "\"";
		}
		
		StringBuilder buf = new StringBuilder().append('{');
		if(node instanceof NominalSplit){			
			List<DecisionNode<?>> children = ((NominalSplit) node).getChildren();
			for(int i = 0; i < node.split().cardinality(); i++) {
				if(i > 0) {
					buf.append(',');
				}
				buf.append('\"').append('#').append(node.split().getIndex()).append('=')				
					.append(node.split().valueOf(i))
					.append('\"')
					.append(':')
					.append(this.toJson(children.get(i)));				
			}
		}
		
		if(node instanceof BinaryNumericSplit) {
			BinaryNumericSplit<?> split = (BinaryNumericSplit<?>) node;
			buf.append('\"')
				.append('#').append(split.split().getIndex())
					.append(" < ").append(split.getThreshold())
				.append('\"').append(':').append(this.toJson(split.getLeft()))
				.append(',')
				.append('\"')
				.append('#').append(split.split().getIndex())
					.append(" > ").append(split.getThreshold())
				.append('\"').append(':').append(this.toJson(split.getRight()));
		}
		
		return buf.append('}').toString();
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
	
	protected ArraySequence defaultSeq(int len) {
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
