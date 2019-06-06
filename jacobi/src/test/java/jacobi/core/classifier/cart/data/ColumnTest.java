package jacobi.core.classifier.cart.data;

import java.util.Arrays;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

public class ColumnTest {

    @Test
    public void shouldNumericColumnsHaveCardinalityZero() {
        Column<Void> col = Column.numeric(6);
        Assert.assertEquals(0, col.cardinality());
        Assert.assertNotNull(col.getItems());
        Assert.assertTrue(col.getItems().isEmpty());
    }
    
    @Test
    public void shouldNumericColumnsHaveMappingFunctionToNegativeValue() {
        Column<Void> col = Column.numeric(6);
        Random rand = new Random(-5588225660038622592L);
        for(int i = 0; i < 1024; i++) {
            Assert.assertTrue(col.getMapping().applyAsInt(1024.0 * rand.nextDouble()) < 0);
        }
    }
    
    @Test
    public void shouldSupportBoolNominalColumn() {
        Column<Boolean> col = new Column<>(0, Arrays.asList( Boolean.FALSE, Boolean.TRUE ),
                v -> v > 0 ? 1 : 0);
        
        Assert.assertEquals(2, col.cardinality());
        Assert.assertNotNull(col.getItems());
        Assert.assertEquals(Boolean.FALSE, col.valueOf(0));
        Assert.assertEquals(Boolean.TRUE, col.valueOf(1));
        
        Assert.assertEquals(Boolean.FALSE, col.valueOf(-1.78));
        Assert.assertEquals(Boolean.TRUE, col.valueOf(Math.PI));
    }

}
