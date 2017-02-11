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
package jacobi.api.ext;

import jacobi.api.Matrix;
import jacobi.api.annotations.Facade;
import jacobi.api.annotations.Implementation;
import jacobi.api.annotations.Immutate;
import jacobi.core.data.Augmented;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/** 
 * Extension for column manipulation. This interface allows creating a new matrix
 * from an existing matrix, by appending/prepending/inserting/selecting certain 
 * columns and discard others.
 * 
 * Often times data comes in rows of tuples like &lt;x, y&gt;, and say if one 
 * would like to do a linear regression in the form y = a*x + b, one first need
 * to obtain matrices &lt;x, 1.0&gt; and &lt;y&gt;. While it is not difficult
 * to code this, it can get repetitive cumbersome. This extension provides
 * and easy way to obtain the data matrix in required shape.
 * 
 * Implementation of this extension should be lazy, that it does not build
 * the full matrix until the get() method is called.
 * 
 * @author Y.K. Chan
 */
@Immutate
@Facade
public interface Data extends Supplier<Matrix> {
    
    /**
     * Append a new element, evaluated by given function, at the end of each row.
     * @param func  Function that accepts a immutable List of values as vector, 
     *              and evalutes the value of the new element.
     * @return  Data object that builds with appending.
     */
    @Implementation(Augmented.Append.class)
    public Data append(Function<List<Double>, Double> func);
    
    /**
     * Prepend a new element, evaluated by given function, at the end of each row.
     * @param func  Function that accepts a immutable List of values as vector, 
     *              and evalutes the value of the new element.
     * @return  Data object that builds with prepending.
     */
    @Implementation(Augmented.Prepend.class)
    public Data prepend(Function<List<Double>, Double> func);
    
    /**
     * Insret a new element, evaluated by given function, at the end of each row.
     * @param at   Index of the new element after insertion
     * @param func  Function that accepts a immutable List of values as vector, 
     *              and evalutes the value of the new element.
     * @return  Data object that builds with insertion.
     */
    @Implementation(Augmented.Insert.class)
    public Data insert(int at, Function<List<Double>, Double> func);
    
    /**
     * Select chosen elements, and discard others.
     * @param cols  Column indices of selected columns.
     * @return  Data object that builds with appending.
     */
    @Implementation(Augmented.Select.class)
    public Data select(int... cols);

    @Override
    public Matrix get();
    
}
