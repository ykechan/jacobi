package jacobi.core.graph.util;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;

public class ArrayMinHeapTest {        
    
    @Test
    public void testShouldBeAbleToDoHeapSort() {
        Random rand = new Random(Double.doubleToLongBits(-Math.sqrt(2.0)));
        double[] array = IntStream.range(0, 1024).mapToDouble(i -> rand.nextDouble()).toArray();
        
        MinHeap heap = new ArrayMinHeap();
        for(int i = 0; i < array.length; i++) {
            heap.push(i, array[i]);
        }
        
        double[] result = new double[array.length];
        int k = 0;
        while(!heap.isEmpty()) {
            result[k++] = heap.findMin();
            heap.pop();
        }
        Arrays.sort(array);
        Assert.assertArrayEquals(array, result, 1e-12);
    }
    
    @Test
    public void testShouldBeAbleToAssociateElementsWithWeights() {
        Random rand = new Random(Double.doubleToLongBits(-Math.PI * Math.E));
        double[] array = IntStream.range(0, 1111).mapToDouble(i -> rand.nextDouble()).toArray();
        
        MinHeap heap = new ArrayMinHeap();
        for(int i = 0; i < array.length; i++) {
            heap.push(i, array[i]);
        }
        
        while(!heap.isEmpty()) {
            double weight = heap.findMin();
            int elem = heap.pop();
            Assert.assertEquals(array[elem], weight, 1e-12);
        }
    }

}
