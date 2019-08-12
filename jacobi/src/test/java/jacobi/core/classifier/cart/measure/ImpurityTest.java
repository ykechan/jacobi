package jacobi.core.classifier.cart.measure;

import java.util.Collections;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import jacobi.core.classifier.cart.measure.Impurity;

public class ImpurityTest {
    
    @Test
    public void shouldEntropyBeZeroWhenOnlyOneGroupHaveItem() {
        Assert.assertEquals(0.0, Impurity.ENTROPY.of(new double[] {5.0, 0.0, 0.0}), 1e-12);
        Assert.assertEquals(0.0, Impurity.ENTROPY.of(new double[] {0.0, 6.0, 0.0}), 1e-12);
        Assert.assertEquals(0.0, Impurity.ENTROPY.of(new double[] {0.0, 0.0, 7.0}), 1e-12);
    }
    
    @Test
    public void shouldEntropyBeLnNWhenEveryGroupsHaveEqualNumberOfItem() {
        Assert.assertEquals(Math.log(3.0), Impurity.ENTROPY.of(new double[] {5.0, 5.0, 5.0}), 1e-12);
        Assert.assertEquals(Math.log(3.0), Impurity.ENTROPY.of(new double[] {6.0, 6.0, 6.0}), 1e-12);
        Assert.assertEquals(Math.log(3.0), Impurity.ENTROPY.of(new double[] {7.0, 7.0, 7.0}), 1e-12);
        
        Assert.assertEquals(Math.log(2.0), Impurity.ENTROPY.of(new double[] {8.0, 8.0}), 1e-12);
        Assert.assertEquals(Math.log(2.0), Impurity.ENTROPY.of(new double[] {9.0, 9.0}), 1e-12);
    }
    
    @Test 
    public void shouldEntropyBeMaxWhenEveryGroupsHaveEqualNumberOfItem() {
        Random rand = new Random(Double.doubleToLongBits(Math.PI));        
    }

}
