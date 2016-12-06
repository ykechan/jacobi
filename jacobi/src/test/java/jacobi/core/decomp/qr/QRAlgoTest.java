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
package jacobi.core.decomp.qr;

import jacobi.api.Matrix;
import jacobi.core.decomp.qr.step.QRSteps;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.Jacobi;
import jacobi.test.util.JacobiJUnit4ClassRunner;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Y.K. Chan
 */
@JacobiImport("/jacobi/test/data/QRAlgoTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class QRAlgoTest {
    
    @JacobiInject(-1)
    public Map<Integer, Matrix> steps;
    
    @JacobiResult(100)
    public Matrix result;
    
    @Test
    @JacobiImport("Full 4x4")
    @JacobiEquals(expected = 100, actual = 100)
    public void test4x4() {
        Matrix input = this.steps.get(0);
        this.mock().compute(input, null, true);
        this.result = input;
    }
    
    protected SchurDecomp mock() {
        AtomicInteger count = new AtomicInteger(0);                
        return new SchurDecomp((m, p, begin, end, full) -> {
            int k = count.incrementAndGet(); 
            if(steps.containsKey(k)){ 
                Jacobi.assertEquals(steps.get(k), m);
            }
            return QRSteps.getStandard().compute(m, p, begin, end, full);
        });
    }
}
