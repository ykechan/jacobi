package jacobi.core.classifier.cart;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import jacobi.api.Matrix;
import jacobi.core.classifier.cart.data.Column;
import jacobi.core.classifier.cart.data.DataTable;
import jacobi.core.classifier.cart.data.Sequence;
import jacobi.core.classifier.cart.node.DecisionNode;
import org.junit.Assert;
import org.junit.Test;

public class ZeroRTest {
    
    @Test
    public void shouldBeAbleToFindMostFrequentItem() {
        int[] items = new int[] {0, 1, 0, 1, 0};
        DecisionNode node = new ZeroR().learn(this.mockData(items), 
                IntStream.range(0, items.length).mapToDouble(v -> 1.0).toArray(), 
                Collections.emptySet(), 
                new Sequence(
                    IntStream.range(0, items.length).toArray(),
                    0, items.length
                ));
        Assert.assertNotNull(node);
        Assert.assertNull(node.split());
        Assert.assertEquals(0, node.decide());
        Assert.assertFalse(node.decide( 1.0).isPresent());
        Assert.assertFalse(node.decide(-1.0).isPresent());
    }
    
    @Test
    public void shouldBeAbleToFindMostFrequentItemAfterWeighted() {
        int[] items = new int[] {0, 1, 0, 1, 0};
        double[] weights = new double[] {0.1, 10.0, 0.1, 5.0, 0.2};
        DecisionNode node = new ZeroR().learn(this.mockData(items), 
                weights, 
                Collections.emptySet(), 
                new Sequence(
                    IntStream.range(0, items.length).toArray(),
                    0, items.length
                ));
        Assert.assertNotNull(node);
        Assert.assertNull(node.split());
        Assert.assertEquals(1, node.decide());
        Assert.assertFalse(node.decide( 1.0).isPresent());
        Assert.assertFalse(node.decide(-1.0).isPresent());
    }
    
    @Test
    public void shouldBeAbleToLimitScopeBySequence() {
        int[] items = new int[] {0, 1, 2, 1, 2};
        double[] weights = IntStream.range(0, items.length).mapToDouble(i -> 1.0).toArray();
        DecisionNode node = new ZeroR().learn(this.mockData(items), 
                weights, 
                Collections.emptySet(), 
                new Sequence(
                    new int[] {2, 4, 3, 0, 1},
                    0, 3
                ));
        Assert.assertNotNull(node);
        Assert.assertNull(node.split());
        Assert.assertEquals(2, node.decide());
        Assert.assertFalse(node.decide( 1.0).isPresent());
        Assert.assertFalse(node.decide(-1.0).isPresent());
    }
    
    private DataTable mockData(int[] outcomes) {
        int max = Arrays.stream(outcomes).max().orElse(-1) + 1;
        return new DataTable() {

            @Override
            public List<Column<?>> getColumns() {
                throw new UnsupportedOperationException();
            }

            @Override
            public Matrix getMatrix() {
                throw new UnsupportedOperationException();
            }


            @Override
            public int[] nominals(int index) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Column<?> getOutcomeColumn() {
                return Column.nominal(-1, max, v -> (int) Math.floor(v));
            }

            @Override
            public int[] outcomes() {
                return outcomes;
            }

            @Override
            public int size() {
                return outcomes.length;
            }
            
        };
    }

}
