package jacobi.api.classifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import jacobi.api.classifier.Column;

public class ColumnTest {

    @Test
    public void shouldNumericColumnsHaveCardinalityZero() {
        Column<Double> col = Column.numeric(6);
        Assert.assertEquals(0, col.cardinality());
        Assert.assertNotNull(col.getItems());
        Assert.assertTrue(col.getItems().isEmpty());
    }
    
    @Test
    public void shouldNumericColumnsHaveMappingFunctionToNegativeValue() {
        Column<Double> col = Column.numeric(6);
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
    
    @Test
    public void shouldSupportCreationByDoubleClass() {
    	Assert.assertEquals(Column.numeric(7), Column.of(7, double.class));
    	Assert.assertEquals(Column.numeric(7), Column.of(7, Double.class));
    }
    
    @Test
    public void shouldSupportCreationByBooleanClass() {
    	Assert.assertEquals(Column.signed(5), Column.of(5, boolean.class));
    	Assert.assertEquals(Column.signed(5), Column.of(5, Boolean.class));
    	Assert.assertEquals(Boolean.TRUE, Column.of(5, boolean.class).valueOf(1.0));
    	Assert.assertEquals(Boolean.FALSE, Column.of(5, boolean.class).valueOf(-1.0));
    	Assert.assertEquals(Boolean.FALSE, Column.of(5, boolean.class).valueOf(0.0));
    	
    	Assert.assertEquals(Boolean.TRUE, Column.of(5, Boolean.class).valueOf(1.0));
    	Assert.assertEquals(Boolean.FALSE, Column.of(5, Boolean.class).valueOf(-1.0));
    	Assert.assertEquals(Boolean.FALSE, Column.of(5, Boolean.class).valueOf(0.0));
    }
    
    @Test
    public void shouldSupportCreationByEnumClass() {
    	Column<Level> col = Column.of(4, Level.class);
    	
    	Assert.assertFalse(col.isNumeric());
    	Assert.assertEquals(5, col.cardinality());
    	
    	Assert.assertEquals(Level.NONE, col.valueOf(0.1));
    	Assert.assertEquals(Level.NONE, col.valueOf(0));
    	
    	Assert.assertEquals(Level.LOW, col.valueOf(1.2));
    	Assert.assertEquals(Level.LOW, col.valueOf(1));
    	
    	Assert.assertEquals(Level.MEDIUM, col.valueOf(2.3));
    	Assert.assertEquals(Level.MEDIUM, col.valueOf(2));
    	
    	Assert.assertEquals(Level.HIGH, col.valueOf(3.4));
    	Assert.assertEquals(Level.HIGH, col.valueOf(3));
    	
    	Assert.assertEquals(Level.CRITICAL, col.valueOf(4.7));
    	Assert.assertEquals(Level.CRITICAL, col.valueOf(4));
    }
    
    @Test
    public void shouldSupportCreationByStringList() {    	
    	Column<String> col = Column.of(4, Arrays.asList("LOW", "MID", "HI"));
    	
    	Assert.assertFalse(col.isNumeric());
    	Assert.assertEquals(3, col.cardinality());
    	
    	Assert.assertEquals("LOW", col.valueOf(0.1));
    	Assert.assertEquals("LOW", col.valueOf(0));
    	
    	Assert.assertEquals("MID", col.valueOf(1.2));
    	Assert.assertEquals("MID", col.valueOf(1));
    	
    	Assert.assertEquals("HI", col.valueOf(2.3));
    	Assert.assertEquals("HI", col.valueOf(2));
    }
    
    @Test
    public void shouldSupportCreationBySet() {
    	Column<Level> col = Column.of(3, EnumSet.allOf(Level.class));
    	Assert.assertFalse(col.isNumeric());
    	Assert.assertEquals(5, col.cardinality());
    	
    	Assert.assertEquals(Level.NONE, col.valueOf(0));
    	Assert.assertEquals(Level.LOW, col.valueOf(1));
    	Assert.assertEquals(Level.MEDIUM, col.valueOf(2));
    	Assert.assertEquals(Level.HIGH, col.valueOf(3));
    	Assert.assertEquals(Level.CRITICAL, col.valueOf(4));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenNumOfItemsIsZero() {
    	Column.nominal(2, 0, v -> (int) v);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenCollectionOfItemsIsEmpty() {
    	Column.of(2, Collections.emptySet());
    }
    
    @Test
    public void shouldAutoEncodeForItemsWithNoLabel() {
    	Column<?> cols = Column.nominal(0, 3, v -> (int) v);
    	for(int i = 0; i < cols.cardinality(); i++) {
    		Assert.assertEquals(i, cols.getItems().get(i));
    	}
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenTypeIsNotDoubleOrBooleanOrEnum() {
    	Column.of(0, String.class);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenComparingColumnWithSameIndexButDifferentCardinality() {
    	Column.nominal(0, 4, v -> (int) v).compareTo(Column.nominal(0, 10, v -> (int) v));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenComparingColumnWithSameIndexButDiffItems() {
    	Column.nominal(0, 4, v -> (int) v)
    		.compareTo(Column.of(0, Arrays.asList("A", "B", "C", "D")));
    }
    
    @Test
    public void shouldColumnNotEqualsToOtherObjects() {
    	Assert.assertFalse(Column.of(0, Boolean.class).equals("ABC"));
    }
    
    public enum Level {
    	NONE, LOW, MEDIUM, HIGH, CRITICAL
    }

}
