/*
 * The MIT License
 *
 * Copyright 2016 Y.K. Chan.
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
package jacobi.core.givens;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.JacobiJUnit4ClassRunner;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Y.K. Chan
 */
@JacobiImport("/jacobi/test/data/GivensRQTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class GivensRQTest {
    
    @JacobiInject(0)
    public Matrix input;
    
    @JacobiInject(1)
    public Matrix givens;    
    
    @JacobiResult(10)
    public Matrix output;
    
    @JacobiInject(2)
    @JacobiResult(11)
    public Matrix partner;
    
    @Test
    @JacobiImport("4x4")
    @JacobiEquals(expected = 10, actual = 10)
    @JacobiEquals(expected = 11, actual = 11)
    public void test4x4() {
        GivensRQ rot = new GivensRQ(this.toGivensList(givens));
        rot.compute(input, 0, 4, GivensMode.UPPER);
        this.output = this.input;
        this.partner = Matrices.identity(4);
        rot.compute(partner, 0, 4, GivensMode.FULL);
    }
    
    @Test
    @JacobiImport("4x4")
    @JacobiEquals(expected = 10, actual = 10)
    @JacobiEquals(expected = 11, actual = 11)
    public void test4x4InParallel() {
        GivensRQ rot = this.parallelMock(this.toGivensList(givens));
        rot.compute(input, 0, 4, GivensMode.UPPER);
        this.output = this.input;
        this.partner = Matrices.identity(4);
        rot.compute(partner, 0, 4, GivensMode.FULL);
    }
    
    @Test
    @JacobiImport("4x4 in 6x6")
    @JacobiEquals(expected = 10, actual = 10)
    @JacobiEquals(expected = 11, actual = 11)
    public void test4x4In6x6() {
        GivensRQ rot = new GivensRQ(this.toGivensList(givens));
        rot.compute(input, 1, 5, GivensMode.UPPER);
        this.output = this.input;
        rot.compute(this.partner, 1, 5, GivensMode.FULL);
    }
    
    @Test
    @JacobiImport("4x4 in 6x6")
    @JacobiEquals(expected = 10, actual = 10)
    @JacobiEquals(expected = 11, actual = 11)
    public void test4x4In6x6InParallel() {
        GivensRQ rot = this.parallelMock(this.toGivensList(givens));
        rot.compute(input, 1, 5, GivensMode.UPPER);
        this.output = this.input;
        rot.compute(this.partner, 1, 5, GivensMode.FULL);
    }
    
    private List<Givens> toGivensList(Matrix givens) {
        Givens[] rotList = new Givens[givens.getRowCount()];
        for(int i = 0; i < rotList.length; i++){
            double[] row = givens.getRow(i);
            rotList[i] = new Givens(row[0], row[1], row[2]);
        }
        return Arrays.asList(rotList);
    }
    
    private GivensRQ parallelMock(List<Givens> rotList) {
        return new GivensRQ(rotList){

            @Override
            protected int rotateInSerial(Matrix matrix, int begin, int end, GivensMode mode) {
                return this.rotateInParallel(matrix, begin, end, mode);
            }
            
        };
    }
    
}
