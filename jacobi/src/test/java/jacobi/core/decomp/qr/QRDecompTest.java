/* 
 * The MIT License
 *
 * Copyright 2016 Y.K. Chan
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
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.Jacobi;
import jacobi.test.util.JacobiJUnit4ClassRunner;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Y.K. Chan
 */
@JacobiImport("/jacobi/test/data/QRDecompTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class QRDecompTest {
    
    @JacobiInject(0)
    public Matrix input;
    
    @JacobiInject(1)
    public Matrix step1;
    
    @JacobiInject(2)
    public Matrix step2;
    
    @JacobiInject(3)
    public Matrix step3;
    
    @JacobiInject(4)
    public Matrix step4;
    
    @JacobiInject(5)
    public Matrix step5;
    
    @JacobiResult(100)
    public Matrix q;

    @Test
    @JacobiImport("4x4")
    @JacobiEquals(expected = 100, actual = 100)
    public void test4x4() {
        this.q = this.assertEachStep(
                Arrays.asList(step1, step2, step3)
        ).computeQR(input).getLeft();
    }
    
    @Test
    @JacobiImport("6x6")
    @JacobiEquals(expected = 100, actual = 100)
    public void test6x6() {
        this.q = this.assertEachStep(Arrays.asList(
            step1, step2, step3,
            step4, step5
        )).computeQR(input).getLeft();
    }
    
    @Test
    @JacobiImport("9x3")
    @JacobiEquals(expected = 100, actual = 100)
    public void test9x3() {
        this.q = this.assertEachStep(Arrays.asList(
            step1, step2, step3
        )).computeQR(input).getLeft();
    }
    
    @Test
    @JacobiImport("Degen 8x3")
    @JacobiEquals(expected = 100, actual = 100)
    public void testDegen8x3() {
        this.q = this.assertEachStep(Arrays.asList(
            step1, 
            step1, // no change after step 1
            step2
        )).computeQR(input).getLeft();
    }
    
    private QRDecomp assertEachStep(List<Matrix> expects) {
        return new QRDecomp(){

            @Override
            protected void eliminate(Matrix matrix, Consumer<HouseholderReflector> listener, int j) {
                super.eliminate(matrix, listener, j);
                for(int k = j + 1; k < matrix.getRowCount(); k++){
                    matrix.set(k, j, 0.0);
                }
                Jacobi.assertEquals(expects.get(j), matrix);
            }
            
        };
    }
}
