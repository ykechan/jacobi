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
package jacobi.api.ext;

import jacobi.api.Matrix;
import jacobi.api.annotations.Facade;
import jacobi.api.annotations.Implementation;
import jacobi.api.annotations.NonPerturbative;
import jacobi.core.op.Mul;
import jacobi.core.op.Operators;
import java.util.function.Supplier;

/**
 * Extension for matrix arithmetics operations, including addition, multiplication,
 * and subtraction. 
 * 
 * This extension is a chaining interface, i.e. that is it supports doing multiple
 * operations and gets back the answer as an Supplier, without the need to
 * create extension every time.
 * 
 * This extension is non-perturbative, i.e. it preserves the value of the matrices
 * it operates upon.
 * 
 * @author Y.K. Chan
 */
@NonPerturbative
@Facade
public interface Op extends Supplier<Matrix> {
    
    /**
     * Matrix addition, i.e. C = A + B
     * @param b  2nd Operand. The first operand is the extension parameter.
     * @return  Resultant matrix
     */
    @Implementation(Operators.Add.class)
    public Op add(Matrix b);
    
    /**
     * Matrix subtraction, i.e. C = A - B
     * @param b  2nd Operand. The first operand is the extension parameter.
     * @return  Resultant matrix
     */
    @Implementation(Operators.Sub.class)
    public Op sub(Matrix b);
    
    /**
     * Matrix multiplication, i.e. C = A * B
     * @param b  2nd Operand. The first operand is the extension parameter.
     * @return  Resultant matrix
     */
    @Implementation(Mul.class)
    public Op mul(Matrix b);
    
    /**
     * Hadamand multiplication, i.e. element-by-element multiplication.
     * @param b  2nd Operand. The first operand is the extension parameter.
     * @return  Resultant matrix
     */
    @Implementation(Operators.Hadamard.class)
    public Op hadamard(Matrix b);
    
    /**
     * Matrix multiplication with a scalar. C = k * A
     * @param k  scalar operand. The first operand is the extension parameter.
     * @return  Resultant matrix
     */
    @Implementation(Operators.Hadamard.class)
    public Op mul(double k);
    
}
