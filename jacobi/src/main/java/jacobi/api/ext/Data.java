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
