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

import jacobi.api.Matrix;
import jacobi.api.annotations.Immutate;
import jacobi.core.util.Throw;

/**
 * Implementation classes for operators.
 * 
 * @author Y.K. Chan
 */
public class Operators {
    
    /**
     * Matrix addition.
     */
    @Immutate
    public static class Add extends RowBased {

        public Add() {
            super((a, b) -> a + b);
        }
        
    }
    
    /**
     * Matrix subtraction.
     */
    @Immutate
    public static class Sub extends RowBased {

        public Sub() {
            super((a, b) -> a - b);
        }
        
    }
    
    /**
     * Hadamard product (element-by-element multiplication) of matrices.
     */
    @Immutate
    public static class Hadamard extends RowBased {

        public Hadamard() {
            super((a, b) -> a * b);
        }
     
        public Matrix compute(Matrix a, double k) {
            Throw.when().isNull(() -> a, () -> "1st operand is null.");
            return super.compute(a, new Scalar(a.getRowCount(), a.getColCount(), k));
        }
    }    
}
