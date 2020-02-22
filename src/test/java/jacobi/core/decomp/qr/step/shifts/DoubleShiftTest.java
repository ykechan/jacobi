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
package jacobi.core.decomp.qr.step.shifts;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.core.impl.ColumnVector;
import jacobi.core.util.Pair;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.JacobiJUnit4ClassRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Y.K. Chan
 */
@JacobiImport("/jacobi/test/data/DoubleShiftTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class DoubleShiftTest {
    
    @JacobiInject(0)
    public Matrix input;
    
    @JacobiResult(1)
    public Matrix shiftValue;
    
    @JacobiResult(2)
    public Matrix reEig;
    
    @JacobiResult(3)
    public Matrix imEig;
    
    @JacobiResult(4)
    public Matrix implicit;
    
    @Test
    @JacobiImport("5x5 at 3")
    @JacobiEquals(expected = 1, actual = 1)
    @JacobiEquals(expected = 2, actual = 2)
    @JacobiEquals(expected = 3, actual = 3)
    @JacobiEquals(expected = 4, actual = 4)
    public void test5x5At3() {
        DoubleShift shift = DoubleShift.of(input, 3);
        this.shiftValue = new ColumnVector( shift.getTr(), shift.getDet(), shift.delta );
        Pair eig = shift.eig();
        reEig = eig.getLeft();
        imEig = eig.getRight();        
        this.implicit = Matrices.identity(5);
        double[] upper = this.implicit.getRow(0);
        double[] mid = this.implicit.getRow(1);
        double[] lower = this.implicit.getRow(2);
        shift.getImplicitG(input, 0)
                .applyLeft(upper, mid, lower, 0, 5);
        this.implicit.setRow(0, upper).setRow(1, mid).setRow(2, lower);
    }

    @Test
    @JacobiImport("6x6 at 3")
    @JacobiEquals(expected = 1, actual = 1)
    @JacobiEquals(expected = 2, actual = 2)
    @JacobiEquals(expected = 3, actual = 3)
    @JacobiEquals(expected = 4, actual = 4)
    public void test6x6At3() {
        DoubleShift shift = DoubleShift.of(input, 3);
        this.shiftValue = new ColumnVector( shift.getTr(), shift.getDet(), shift.delta );
        Pair eig = shift.eig();
        reEig = eig.getLeft();
        imEig = eig.getRight();        
        this.implicit = Matrices.identity(4);
        double[] upper = this.implicit.getRow(0);
        double[] mid = this.implicit.getRow(1);
        double[] lower = this.implicit.getRow(2);
        shift.getImplicitG(input, 1)
                .applyLeft(upper, mid, lower, 0, 4);
        this.implicit.setRow(0, upper).setRow(1, mid).setRow(2, lower);
    }

    @Test
    @JacobiImport("Implicit Q 5x5")
    @JacobiEquals(expected = 4, actual = 4)
    public void testImplicitQ5x5() {
        this.implicit = DoubleShift.of(input, input.getRowCount() - 2).getImplicitQ(input, 0);
    }
    
    @Test
    @JacobiImport("Implicit Q 6x6 at 3")
    @JacobiEquals(expected = 4, actual = 4)
    public void testImplicitQ6x6At3() {
        this.implicit = DoubleShift.of(input, input.getRowCount() - 3).getImplicitQ(input, 1);
    }
}
