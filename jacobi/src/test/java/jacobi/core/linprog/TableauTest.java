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

import jacobi.api.Matrix;
import jacobi.core.impl.ColumnVector;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.JacobiJUnit4ClassRunner;
import java.util.stream.IntStream;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Y.K. Chan
 */
@JacobiImport("/jacobi/test/data/TableauTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class TableauTest {
    
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

    @Test
    @JacobiImport("constructor")
    @JacobiEquals(expected = 100, actual = 100)
    public void testConstructor() {
        this.result = Tableau.of(c, a, b).getMatrix();
        Assert.assertEquals(a.getColCount() + 1, this.result.getColCount());
        Assert.assertEquals(a.getRowCount() + 1, this.result.getRowCount());
    }
    
    @Test
    @JacobiImport("constructor mixed signs")
    @JacobiEquals(expected = 100, actual = 100)
    @JacobiEquals(expected = 101, actual = 101)
    public void testConstructorMixedSigns() {
        Tableau tab = Tableau.of(c, a, b);
        this.result = tab.getMatrix();
        this.signs = this.signsAsMatrix(tab);
        Assert.assertEquals(a.getColCount() + 1, this.result.getColCount());
        Assert.assertEquals(a.getRowCount() + 1, this.result.getRowCount());
    }
    
    @Test
    @JacobiImport("swap 2,3")
    @JacobiEquals(expected = 100, actual = 100)
    public void testSwap2And3() {
        this.result = Tableau.of(c, a, b).swapBasis(2, 3).getMatrix();
    }
    
    @Test
    @JacobiImport("swap 3,1")
    @JacobiEquals(expected = 100, actual = 100)
    public void testSwap3And1() {
        this.result = Tableau.of(c, a, b).swapBasis(3, 1).getMatrix();
    }
    
    @Test
    @JacobiImport("swap 3,2")
    @JacobiEquals(expected = 100, actual = 100)
    public void testSwap3And2() {
        this.result = Tableau.of(c, a, b).swapBasis(3, 2).getMatrix();
    }
    
    @Test(expected = UnsupportedOperationException.class)
    @JacobiImport("Invalid neg-pos swap 3,3")
    public void testInvalidNegPosSwap3And3() {
        this.result = Tableau.of(c, a, b).swapBasis(3, 3).getMatrix();
    }
    
    @Test(expected = UnsupportedOperationException.class)
    @JacobiImport("Invalid pos-neg swap 2,2")
    public void testInvalidPosNegSwap2And2() {
        this.result = Tableau.of(c, a, b).swapBasis(2, 2).getMatrix();
    }
    
    private Matrix signsAsMatrix(Tableau tab) {
        return new ColumnVector(IntStream.range(0, tab.getRowCount())
                .map((i) -> tab.getSign(i))
                .mapToDouble((i) -> (double) i)
                .toArray());
    }
}
