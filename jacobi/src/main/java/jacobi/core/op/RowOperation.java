/*
 * Copyright (C) 2015 Y.K. Chan
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
package jacobi.core.op;

import java.util.function.DoubleBinaryOperator;

/**
 * Functional interface for computing two rows of values and output to 
 * another row.
 * 
 * @author Y.K. Chan
 * @param <T>  Computation result, if any.
 */
@FunctionalInterface
public interface RowOperation<T> {
    
    /**
     * Row operation composed of element-to-element function performing to
     * each element of the row values.
     * @param op  Element-to-element function.
     * @return  Row operation representation
     */
    public static RowOperation<Void> forEach(DoubleBinaryOperator op) {
        return (u, v, w) -> {
            for(int i = 0; i < u.length; i++){
                w[i] = op.applyAsDouble(u[i], v[i]);
            }
            return null;
        };
    }
    
    /**
     * Perform operation.
     * @param u  row values of 1st operand
     * @param v  row values of 2nd operand
     * @param w  output row buffer
     * @return  Computation result, if any.
     */
    public T apply(double[] u, double[] v, double[] w);
    
}
