/*
 * The MIT License
 *
 * Copyright 2017 Y.K. Chan.
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
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Y.K. Chan
 */
@JacobiImport("/jacobi/test/data/GivensBatchRQTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class GivensBatchRQTest {
    
    @JacobiInject(0)
    public Matrix input;
    
    @JacobiInject(1)
    public Matrix batch;
    
    @JacobiInject(10)
    @JacobiResult(10)
    public Matrix output;
    
    @JacobiInject(11)
    @JacobiResult(11)
    public Matrix partner;
    
    @Test
    @JacobiImport("6x6")
    @JacobiEquals(expected = 10, actual = 10)
    @JacobiEquals(expected = 11, actual = 11)
    public void test6x6() {
        GivensBatchRQ batRq = new GivensBatchRQ(this.toBatch(batch));
        batRq.compute(input, 0, 6, GivensMode.UPPER);
        this.output = this.input;
        this.partner = Matrices.identity(6);
        batRq.compute(this.partner, 0, 6, GivensMode.FULL);
    }
    
    private GivensBatch toBatch(Matrix bat) {
        GivensPair first = null;
        List<GivensPair> givList = new ArrayList<>();
        for(int i = 0; i < bat.getRowCount() - 1; i+=2){
            double[] upper = bat.getRow(i);
            double[] lower = bat.getRow(i + 1);
            GivensPair pair = new GivensPair(
                    new Givens(upper[0], upper[1], upper[2]), 
                    new Givens(lower[0], lower[1], lower[2])
            );
            if(i == 0){
                first = pair;
            }else{
                givList.add(pair);
            }
        }
        double[] row = bat.getRow(bat.getRowCount() - 1);
        Givens last = new Givens(row[0], row[1], row[2]);
        return new GivensBatch(first, givList, last, 0.0);
    }

}
