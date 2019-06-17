package jacobi.core.classifier.cart;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import jacobi.core.classifier.cart.Partitioner.Instance;
import jacobi.core.classifier.cart.data.Column;
import jacobi.core.util.Weighted;
import org.junit.Assert;
import org.junit.Test;

public class NominalPartitionerTest {
    
    @Test
    public void shouldBeAbleToMeasurePerfectlySeparableData() {
        List<Instance> data = Arrays.asList(
            new Instance(0, 2, 1.0),
            new Instance(1, 0, 1.0),
            new Instance(2, 1, 1.0),
            new Instance(0, 2, 1.0),
            new Instance(1, 0, 1.0),
            new Instance(2, 1, 1.0)
        );
        
        AtomicInteger count = new AtomicInteger(0);
        Weighted<Void> w = new NominalPartitioner(dist -> {
            count.incrementAndGet();
            for(double v : dist) {
                Assert.assertTrue(v == 0.0 || v == 2.0);
            }
            Assert.assertEquals(2.0, Arrays.stream(dist).sum(), 1e-12);
            return 1.0;
        }).partition(
            Column.nominal(0, 3, v -> 0), 
            Column.nominal(-1, 3, v -> 0), 
            data);
        
        Assert.assertEquals(3.0, w.weight, 1e-12);
        Assert.assertEquals(3, count.get());
    }
    
    @Test
    public void shouldBeAbleToMeasurePerfectlyEvenData() {
        List<Instance> data = Arrays.asList(
                new Instance(0, 0, 1.0),
                new Instance(0, 1, 1.0),
                new Instance(0, 2, 1.0),
                new Instance(1, 0, 1.0),
                new Instance(1, 1, 1.0),
                new Instance(1, 2, 1.0),
                new Instance(2, 0, 1.0),
                new Instance(2, 1, 1.0),
                new Instance(2, 2, 1.0)
        );
        
        AtomicInteger count = new AtomicInteger(0);
        Weighted<Void> w = new NominalPartitioner(dist -> {
            count.incrementAndGet();
            for(double v : dist) {
                Assert.assertTrue(v == 1.0);
            }
            Assert.assertEquals(3.0, Arrays.stream(dist).sum(), 1e-12);
            return 1.0;
        }).partition(
            Column.nominal(0, 3, v -> 0), 
            Column.nominal(-1, 3, v -> 0), 
            data);
        
        Assert.assertEquals(3.0, w.weight, 1e-12);
        Assert.assertEquals(3, count.get());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenGivenNumericFeatureColumn() {
        new NominalPartitioner(dist -> Math.PI).partition(
            Column.numeric(0), 
            Column.nominal(-1, 3, v -> 0), 
            Collections.emptyList());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenGivenNumericGoalColumn() {
        new NominalPartitioner(dist -> Math.PI).partition(
            Column.nominal(0, 3, v -> 0), 
            Column.numeric(-1), 
            Collections.emptyList());
    }

}
