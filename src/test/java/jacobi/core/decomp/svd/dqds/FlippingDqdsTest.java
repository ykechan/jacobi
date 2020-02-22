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
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.JacobiJUnit4ClassRunner;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Y.K. Chan
 */
@JacobiImport("/jacobi/test/data/FlippingDqdsTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class FlippingDqdsTest {
    
    @JacobiInject(0)
    public Matrix input;
    
    @JacobiResult(1)
    public Matrix output;
    
    @Test
    @JacobiImport("5x5")
    @JacobiEquals(expected = 1, actual = 1)
    public void test5x5() {
        boolean forward = true;
        double[] zElem = this.toArray(input, forward);
        new FlippingDqds(this.mock(), 0.0).compute(zElem, 0, 5, forward, State.empty());
        this.output = this.toMatrix(zElem, 0, 5, !forward);
    }
    
    @Test
    @JacobiImport("5x5")
    @SuppressWarnings("InfiniteRecursion") // false positive
    public void test5x5FallThrough() {
        boolean forward = true;
        double[] zElem = this.toArray(input, forward);
        AtomicBoolean ran = new AtomicBoolean(false);
        new FlippingDqds((elem, begin, end, forth, state) -> {
                    ran.set(true);
                    return State.empty();
                }, 10.0)
                .compute(zElem, 0, 5, forward, State.empty());        
        Assert.assertTrue(ran.get());
    }
    
    @Test
    @JacobiImport("1,5 in 7x7")
    @JacobiEquals(expected = 1, actual = 1)
    public void test1And5In7x7() {
        boolean forward = true;
        double[] zElem = this.toArray(input, forward);
        for(int i = 2; i < zElem.length; i += 4){
            zElem[i] = zElem[i - 2];
            zElem[i + 1] = zElem[i - 1];
        }
        new FlippingDqds(this.mock(), 0.0).compute(zElem, 1, 5, forward, State.empty());
        this.output = this.toMatrix(zElem, 0, 7, !forward);
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
    
    private DqdsStep mock() {
        return (zElem, begin, end, forward, prev) -> {
            throw new UnsupportedOperationException();
        };
    }
    
}
