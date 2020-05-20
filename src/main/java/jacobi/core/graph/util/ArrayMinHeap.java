package jacobi.core.graph.util;

import java.util.Arrays;
import java.util.NoSuchElementException;

/**
 * Implementation of a binary min-heap using backed primitive array.
 * 
 * @author Y.K. Chan
 * @deprecated Use jacobi.core.util.Enque and jacobi.core.util.MinHeap instead
 */
@Deprecated
public class ArrayMinHeap implements MinHeap {
    
    /**
     * Constructor.
     */
    public ArrayMinHeap() {
        this(DEFAULT_ARRAY_SIZE, DEFAULT_STEP_SIZE);
    }
    
    /**
     * Constructor.
     * @param initCapacity  Initial capacity
     * @param rateOfChange  Initial expanding rate
     */
    public ArrayMinHeap(int initCapacity, int rateOfChange) {
        this.elements = new int[initCapacity];
        this.weights = new double[initCapacity];
        this.count = 0;
        this.step = rateOfChange;
    }

    @Override
    public boolean isEmpty() {
        return this.count == 0;
    }

    @Override
    public double findMin() {
        if(this.count < 1){
            throw new NoSuchElementException();
        }
        return this.weights[0];
    }

    @Override
    public void push(int element, double weight) {
        this.ensureCapacity(this.count + 1);
        int index = this.count++;
        this.elements[index] = element;
        this.weights[index] = weight;
        while(index > 0){
            int next = (index - 1) / 2;
            if(this.weights[index] > this.weights[next]){
                return;
            }
            this.swap(index, next);
            index = next;
        }
    }

    @Override
    public int pop() {
        if(this.count < 1){
            throw new NoSuchElementException();
        }
        int elem = this.elements[0];
        this.swap(0, --this.count);
        int index = 0;
        while(2 * index + 1 < this.count){
            int left = 2 * index + 1;
            int right = left + 1;
            int next = right >= this.count 
                    || this.weights[left] < this.weights[right] ? left : right;
            if(this.weights[index] < this.weights[next]) {
                break;
            }
            this.swap(index, next);
            index = next;
        }
        return elem;
    }
    
    /**
     * Ensure array capacity.
     * @param size  Capacity size
     */
    protected void ensureCapacity(int size) {
        while(size > this.elements.length) {
            int next = this.elements.length + this.step;
            this.elements = Arrays.copyOf(this.elements, next);
            this.weights = Arrays.copyOf(this.weights, next);
            this.step += 2;
        }
    }
    
    /**
     * Swap two elements and their weights by array indices.
     * @param i  Index of the first element
     * @param j  Index of the second element
     */
    protected void swap(int i, int j) {
        int temp = this.elements[i];
        this.elements[i] = this.elements[j];
        this.elements[j] = temp;
        
        double tmp = this.weights[i];
        this.weights[i] = this.weights[j];
        this.weights[j] = tmp;
    }
    
    private int[] elements;
    private double[] weights;
    private int count, step;
    
    protected static final int DEFAULT_ARRAY_SIZE = 64;
    
    protected static final int DEFAULT_STEP_SIZE = 17;
}
