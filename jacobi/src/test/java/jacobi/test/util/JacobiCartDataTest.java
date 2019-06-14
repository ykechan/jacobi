package jacobi.test.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import jacobi.core.classifier.cart.data.Column;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class JacobiCartDataTest {
    
    private Workbook workbook;
    
    @Before
    public void init() throws IOException, EncryptedDocumentException, InvalidFormatException {
        try(InputStream input = this.getClass()
                .getResourceAsStream("/jacobi/test/data/JacobiCartDataTest.xlsx")){
            this.workbook = WorkbookFactory.create(input);
        }
    }
    
    @Test
    public void shouldBeAbleToLoadSimpleDataSetsIntoListOfObjects() {
        AtomicInteger count = new AtomicInteger(0);
        new JacobiCartData(this.workbook) {

            @Override
            protected List<Object[]> readElements(Sheet worksheet, int begin) {
                count.incrementAndGet();
                
                List<Object[]> elements = super.readElements(worksheet, begin);
                Assert.assertEquals(5, elements.stream()
                    .mapToInt(arr -> arr.length)
                    .max()
                    .orElseThrow(() -> new IllegalStateException())
                );
                Assert.assertEquals(5, elements.stream()
                    .mapToInt(arr -> arr.length)
                    .min()
                    .orElseThrow(() -> new IllegalStateException())
                );
                for(Object[] row : elements) {
                    Arrays.stream(row).forEach(obj -> {
                        Assert.assertNotNull(obj);
                        Assert.assertTrue(obj instanceof String 
                                ? !obj.toString().trim().isEmpty() : true);
                    });
                }
                return elements;
            }
            
        }.read("test simple data sets");
        
        Assert.assertTrue(count.get() > 0);
    }
    
    @Test
    public void shouldBeAbleToDetectColumnsForSimpleDataSets() {
        AtomicInteger count = new AtomicInteger(0);
        new JacobiCartData(this.workbook) {

            @Override
            protected List<Column<?>> detectColumns(List<Object[]> elements) {
                count.incrementAndGet();
                
                List<Column<?>> cols = super.detectColumns(elements);
                Assert.assertEquals(5, cols.size());
                for(int i = 0; i < cols.size(); i++) {
                    Assert.assertEquals(i, cols.get(i).getIndex());
                }
                Assert.assertTrue(cols.get(0).isNumeric()); 
                Assert.assertEquals(2, cols.get(1).cardinality());
                Assert.assertEquals(3, cols.get(2).cardinality());
                Assert.assertTrue(cols.get(3).isNumeric());
                
                Assert.assertTrue(cols.get(1).getItems().get(0) instanceof Boolean);
                Assert.assertTrue(cols.get(1).getItems().get(1) instanceof Boolean);
                
                Assert.assertTrue(cols.get(2).getItems().get(0) instanceof String);
                Assert.assertTrue(cols.get(2).getItems().get(1) instanceof String);
                Assert.assertTrue(cols.get(2).getItems().get(2) instanceof String);
                
                return cols;
            }

            
            
        }.read("test simple data sets");
        
        Assert.assertTrue(count.get() > 0);
    }

}
