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
package jacobi.core.linprog;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.core.impl.ColumnVector;
import jacobi.core.util.IntArray;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.JacobiJUnit4ClassRunner;
import java.util.Arrays;
import java.util.stream.IntStream;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Y.K. Chan
 */
@JacobiImport("/jacobi/test/data/MutableTableauTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class MutableTableauTest {
    
    @JacobiInject(0)
    public Matrix c;
    
    @JacobiInject(1)
    public Matrix a;
    
    @JacobiInject(2)
    public Matrix b;        
    
    @JacobiResult(100)
    public Matrix result;
    
    @JacobiResult(101)
    public Matrix signs;

    @JacobiResult(102)
    public Matrix vars;
    
    @Test
    @JacobiImport("constructor")
    @JacobiEquals(expected = 100, actual = 100)
    @JacobiEquals(expected = 101, actual = 101)
    @JacobiEquals(expected = 102, actual = 102)
    public void testConstructor() {
        Tableau tab = MutableTableau.of(c, a, b).apply((mat, i, j) -> {});
        this.result = tab.getMatrix();
        this.signs = this.toMatrix(tab.getSigns());
        this.vars = this.toMatrix(tab.getVars());
    }
    
    @Test
    @JacobiImport("constructor mixed signs")
    @JacobiEquals(expected = 100, actual = 100)
    @JacobiEquals(expected = 101, actual = 101)
    @JacobiEquals(expected = 102, actual = 102)
    public void testConstructorMixedSigns() {
        Tableau tab = MutableTableau.of(c, a, b).apply((mat, i, j) -> {});
        this.result = tab.getMatrix();
        this.signs = this.toMatrix(tab.getSigns());
        this.vars = this.toMatrix(tab.getVars());
    }
    
    @Test
    @JacobiImport("swap 3,3")
    @JacobiEquals(expected = 100, actual = 100)
    @JacobiEquals(expected = 101, actual = 101)
    @JacobiEquals(expected = 102, actual = 102)
    public void testSwap3and3() {
        MutableTableau tab = MutableTableau.of(c, a, b).apply((mat, i, j) -> {});
        this.result = tab.getMatrix();
        this.signs = this.toMatrix(tab.getSigns());
        tab.swapBasis(3, 3);
        this.vars = this.toMatrix(tab.getVars());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testNullObjective() {
        MutableTableau.of(null, Matrices.zeros(3), Matrices.zeros(3, 1)).apply((mat, i, j) -> {});
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testNullConstraintMatrix() {
        MutableTableau.of(Matrices.zeros(3), null, Matrices.zeros(3, 1)).apply((mat, i, j) -> {});
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testNullConstraintBoundary() {
        MutableTableau.of(Matrices.zeros(3), Matrices.zeros(3), null).apply((mat, i, j) -> {});
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testVariableDimensionMismatch() {
        MutableTableau.of(Matrices.zeros(6), Matrices.zeros(3), Matrices.zeros(3, 1)).apply((mat, i, j) -> {});
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstraintDimensionMismatch() {
        MutableTableau.of(Matrices.zeros(3), Matrices.zeros(3), Matrices.zeros(5, 1)).apply((mat, i, j) -> {});
    }
    
    private Matrix toMatrix(IntArray array) {
        return new ColumnVector(IntStream.range(0, array.length())
                .map((i) -> array.get(i))
                .mapToDouble((i) -> i)
                .toArray()
        );
    }
}
