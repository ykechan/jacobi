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

package jacobi.core.util;

import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * An item associated with a weight value.
 * 
 * This class is immutable.
 * 
 * @author Y.K. Chan
 * @param <T>  Type of item
 */
public class Weighted<T> implements Comparable<Weighted<T>> {
    
    /**
     * Get comparator in ascending order of weight.
     * @param <T>  Item type
     * @return  Comparator in ascending order of weight
     */
    public static <T> Comparator<Weighted<T>> asc() {
        return (a, b) -> a.compareTo(b);
    }
    
    /**
     * Get comparator in descending order of weight.
     * @param <T>  Item type
     * @return  Comparator in descending order of weight
     */
    public static <T> Comparator<Weighted<T>> desc() {
        return (a, b) -> -a.compareTo(b);
    }    
    
    /**
     * Item contained.
     */
    public final T item;
    
    /**
     * Associated weight value.
     */
    public final double weight;

    /**
     * Constructor.
     * @param item  Item to be associated.
     * @param weight  Associated weight value
     */
    public Weighted(T item, double weight) {
        this.item = item;
        this.weight = weight;
    }

    @Override
    public int compareTo(Weighted<T> o) {
        return Double.compare(weight, o.weight);
    }

    @Override
    public String toString() {
        return String.valueOf(this.item) + '(' + this.weight + ')';
    }        
        
}
