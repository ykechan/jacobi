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
package jacobi.core.decomp.svd.dqds;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.core.decomp.svd.dqds.DqdsStep.State;
import jacobi.core.util.Pair;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.JacobiJUnit4ClassRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Y.K. Chan
 */
@JacobiImport("/jacobi/test/data/UnsafeDqdsTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class UnsafeDqdsTest { 
    
    @JacobiInject(0)
    public Matrix input;
    
    @JacobiInject(1)
    public Matrix shift;
    
    @JacobiResult(10)
    public Matrix output;
    
    @Test
    @JacobiImport("5x5")
    @JacobiEquals(expected = 10, actual = 10)
    public void test5x5() {
        State state = new State(-1, 0.0, shift.get(0, 0), 0.0, 0.0, 0.0);
        double[] elem = this.toArray(this.input, true);
        new UnsafeDqds().compute(elem, 0, 5, true, state);
        this.output = this.toMatrix(elem, 0, 5, false);
    }
    
    @Test
    @JacobiImport("5x5 shift 1")
    @JacobiEquals(expected = 10, actual = 10)
    public void testShifted5x5Iteration1() {
        boolean forward = true;
        State state = new State(-1, 0.0, shift.get(0, 0), 0.0, 0.0, 0.0);
        double[] elem = this.toArray(this.input, forward);
        new UnsafeDqds().compute(elem, 0, 5, forward, state);
        this.output = this.toMatrix(elem, 0, 5, !forward);
    }
    
    @Test
    @JacobiImport("5x5 shift 2")
    @JacobiEquals(expected = 10, actual = 10)
    public void testShifted5x5Iteration2() {
        boolean forward = false;
        State state = new State(-1, 0.0, shift.get(0, 0), 0.0, 0.0, 0.0);
        double[] elem = this.toArray(this.input, forward);
        new UnsafeDqds().compute(elem, 0, 5, forward, state);
        this.output = this.toMatrix(elem, 0, 5, !forward);
    }
    
    @Test
    @JacobiImport("5x5 shift 3")
    @JacobiEquals(expected = 10, actual = 10)
    public void testShifted5x5Iteration3() {
        boolean forward = true;
        State state = new State(-1, 0.0, shift.get(0, 0), 0.0, 0.0, 0.0);
        double[] elem = this.toArray(this.input, forward);
        new UnsafeDqds().compute(elem, 0, 5, forward, state);
        this.output = this.toMatrix(elem, 0, 5, !forward);
    }
    
    @Test
    @JacobiImport("5x5 shift 4")
    @JacobiEquals(expected = 10, actual = 10)
    public void testShifted5x5Iteration4() {
        boolean forward = false;
        State state = new State(-1, 0.0, shift.get(0, 0), 0.0, 0.0, 0.0);
        double[] elem = this.toArray(this.input, forward);
        new UnsafeDqds().compute(elem, 0, 5, forward, state);
        this.output = this.toMatrix(elem, 0, 5, !forward);
    }
    
    @Test
    @JacobiImport("5x5 shift 5")
    @JacobiEquals(expected = 10, actual = 10)
    public void testShifted5x5Iteration5() {
        boolean forward = true;
        State state = new State(-1, 0.0, shift.get(0, 0), 0.0, 0.0, 0.0);
        double[] elem = this.toArray(this.input, forward);
        new UnsafeDqds().compute(elem, 0, 5, forward, state);
        this.output = this.toMatrix(elem, 0, 5, !forward);
    }
    
    @Test
    @JacobiImport("5x5 shift 6")
    @JacobiEquals(expected = 10, actual = 10)
    public void testShifted5x5Iteration6() {
        boolean forward = false;
        State state = new State(-1, 0.0, shift.get(0, 0), 0.0, 0.0, 0.0);
        double[] elem = this.toArray(this.input, forward);
        new UnsafeDqds().compute(elem, 0, 5, forward, state);
        this.output = this.toMatrix(elem, 0, 5, !forward);
    }
    
    @Test
    @JacobiImport("6x6")
    @JacobiEquals(expected = 10, actual = 10)
    public void test6x6() {
        State state = new State(-1, 0.0, shift.get(0, 0), 0.0, 0.0, 0.0);
        double[] elem = this.toArray(this.input, true);
        new UnsafeDqds().compute(elem, 0, 6, true, state);
        this.output = this.toMatrix(elem, 0, 6, false);
    }
    
    @Test
    @JacobiImport("6x6 with shift")
    @JacobiEquals(expected = 10, actual = 10)
    public void test6x6WithShift() {
        State state = new State(-1, 0.0, shift.get(0, 0), 0.0, 0.0, 0.0);
        double[] elem = this.toArray(this.input, true);
        new UnsafeDqds().compute(elem, 0, 6, true, state);
        this.output = this.toMatrix(elem, 0, 6, false);
    }
    
    private double[] toArray(Matrix matrix, boolean head) {
        Assert.assertTrue(matrix.getRowCount() == matrix.getColCount());
        double[] elem = new double[4*matrix.getRowCount()];
        for(int i = 0; i < matrix.getRowCount(); i++){
            double[] row = matrix.getRow(i);
            elem[4*i + (head ? 0 : 2)] = row[i];
            if(i + 1 < matrix.getColCount()){
                elem[4*i + (head ? 0 : 2) + 1] = row[i + 1];
            }
        }
        return elem;
    }
    
    private Matrix toMatrix(double[] zElem, int begin, int end, boolean head) {
        int offset = head ? 0 : 2;
        Matrix matrix = Matrices.zeros(end - begin);
        for(int i = 0; i < matrix.getRowCount(); i++){
            int j = i;
            matrix.getAndSet(j, (r) -> {
                r[j] = zElem[offset + 4*j];
                if(j + 1 < r.length){
                    r[j + 1] = zElem[offset + 4*j + 1];
                }
            });
        }
        return matrix;
    }
    
}
