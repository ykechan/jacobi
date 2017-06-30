/* 
 * The MIT License
 *
 * Copyright 2017 Y.K. Chan
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package jacobi.core.data;

import java.util.AbstractList;
import java.util.Arrays;

/**
 * A limited size vector that elements can be inserted and extracted. Prepending
 * or appending an element takes constant time, and inserting or extracting
 * elements takes linear time.
 * 
 * <p>
 * This class is package private since it is un-safe, in the sense that
 * it requires correct pre-determination of number of prepending or maximum
 * number of elements.
 * </p>
 * 
 * @author Y.K. Chan
 */
class Buffer extends AbstractList<Double> {
    
    /**
     * Constructor.
     * @param offset  maximum number of prepend operation
     * @param max   maximum number of elements the array will expand into
     */
    public Buffer(int offset, int max) {
        this.array = new double[max];
        this.swapper = new double[max];
        this.start = offset;
        this.offset = offset;
        this.length = 0;
    }
    
    /**
     * Get starting position.
     * @return  Starting position.
     */
    protected int getStartingPosition() {
        return this.start;
    }
    
    /**
     * Get maximum size the buffer can grow into.
     * @return  Maximum size of the buffer
     */
    protected int getMaximumLength() {
        return this.array.length;
    }
    
    /**
     * Initialize this buffer with a vector.
     * @param values  vector
     */
    public void fill(double[] values) {
        this.offset = this.start;
        System.arraycopy(values, 0, this.array, this.offset, values.length);
        this.length = values.length;
    }
    
    /**
     * Select only the given columns, discard others.
     * @param cols  column indices
     */
    public void select(int[] cols) {        
        int k = this.start;
        for(int i : cols){
            //System.out.println("i = " + cols[i] + ", k = " + k + ", length = " +swapper.length);
            this.swapper[k++] = this.get(i);
        }
        this.offset = this.start;
        this.length = cols.length;
        
        double[] temp = this.array;
        this.array = this.swapper;
        this.swapper = temp;
    }
    
    /**
     * Insert an element.
     * @param index  Index of the element that will be inserted
     * @param elem   Element value
     */
    public void insert(int index, double elem) {
        if(index == 0){
            this.array[--this.offset] = elem;            
        }else if(index >= this.length){
            this.array[this.offset + this.length] = elem;
        }else{
            int j = this.offset + index;
            System.arraycopy(this.array, j, this.array, j + 1, length - index);
            this.array[j] = elem;
        }
        this.length++;
    }

    @Override
    public Double get(int index) {
        return this.array[this.offset + index];
    }

    @Override
    public int size() {
        return this.length;
    }
    
    /**
     * Create an array containing elements of this buffer.
     * @return  An array
     */
    public double[] getArray() {
        return Arrays.copyOfRange(this.array, this.offset, this.offset + this.length);
    }

    private int start, offset, length;
    private double[] array, swapper;
}
