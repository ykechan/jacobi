/*
 * Copyright (C) 2016 Y.K. Chan
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package jacobi.core.data;

import java.util.AbstractList;
import java.util.Arrays;

/**
 * A limited size vector that elements can be inserted and extracted. Prepending
 * or appending an element takes constant time, and inserting or extracting
 * elements takes linear time.
 * 
 * This class is package private since it is un-safe, in the sense that
 * it requires correct pre-determination of number of prepending or maximum
 * number of elements.
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
