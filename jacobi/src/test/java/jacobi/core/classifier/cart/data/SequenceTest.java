package jacobi.core.classifier.cart.data;

import java.util.List;
import java.util.stream.IntStream;

import jacobi.core.classifier.cart.data.Sequence;
import org.junit.Assert;
import org.junit.Test;

public class SequenceTest {
    
    @Test
    public void shouldBeAbleToGroupByEvenAndOdd() {
        Sequence seq = new Sequence(new int[] {0, 1, 2, 3, 4}, 0, 5);
        List<Sequence> groups = seq.groupBy(i -> i % 2);
        
        Assert.assertEquals(2, groups.size());
        Sequence seq0 = groups.get(0);
        Sequence seq1 = groups.get(1);        
        Assert.assertArrayEquals(new int[] {0, 2, 4}, 
            IntStream.range(0, seq0.length()).map(seq0::indexAt).toArray());
        Assert.assertArrayEquals(new int[] {1, 3}, 
            IntStream.range(0, seq1.length()).map(seq1::indexAt).toArray());
        
        Assert.assertArrayEquals(new int[] {0, 2, 4, 1, 3}, 
                IntStream.range(0, seq.length()).map(seq::indexAt).toArray());
    }

}
