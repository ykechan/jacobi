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
package jacobi.core.decomp.gauss;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.util.Jacobi;
import jacobi.test.util.JacobiJUnit4ClassRunner;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Y.K. Chan
 */
@JacobiImport("/jacobi/test/data/BasicGaussianElimTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class BasicGaussianElimTest {
    
    public @JacobiInject(0) Matrix input;
    public @JacobiInject(1) Matrix step1;
    public @JacobiInject(2) Matrix step2;
    public @JacobiInject(3) Matrix step3;
    public @JacobiInject(4) Matrix step4;
    public @JacobiInject(5) Matrix step5;
    public @JacobiInject(6) Matrix step6;
    public @JacobiInject(7) Matrix step7;
    public @JacobiInject(8) Matrix step8;    
    
    private GenericGaussianElim gaussElim;

    public BasicGaussianElimTest() {
        this.gaussElim = new GenericGaussianElim();
    }
    
    @Test
    @JacobiImport("4x4")
    public void test4x4() {
        this.gaussElim.compute(this.input, (op) -> 
            this.assertByStep(op, Arrays.asList(
                input,
                step1, step2, step3,
                step4, step5, step6
            )));
    }
    
    @Test
    @JacobiImport("5x5")
    public void test5x5() {
        this.gaussElim.compute(this.input, (op) -> 
            this.assertByStep(op, Arrays.asList(
                input,
                step1, step2, step3,
                step4, step5, step6,
                step7, step8
            )));
    }
    
    @Test
    @JacobiImport("UnderDet 4x4")
    public void testUnderDet4x4() {
        this.gaussElim.compute(this.input, (op) -> 
            this.assertByStep(op, Arrays.asList(
                input,
                step1, step2, step3,
                step4, step5, step6
            )));
    }
    
    @Test
    @JacobiImport("Skip 4x4")
    public void testSkip4x4() {
        this.gaussElim.compute(this.input, (op) -> 
            this.assertByStep(op, Arrays.asList(
                input,
                step1, step2, step3,
                step4, step5, step6
            )));
    }
    
    @Test
    public void testNull5x5() {
        this.input = Matrices.zeros(5, 5);
        this.gaussElim.compute(this.input);
        Jacobi.assertEquals(Matrices.zeros(5, 5), input);
    }
    
    @Test
    @JacobiImport("7x3")
    public void test7x3() {
        this.gaussElim.compute(this.input, (op) -> 
            this.assertByStep(op, Arrays.asList(
                input,
                step1, step2, step3,
                step4, step5, step6
            )));
    }

    private ElementaryOperator assertByStep(ElementaryOperator op, List<Matrix> steps) {
        return new ElementaryOperator() {

            @Override
            public void swapRows(int i, int j) {
                Jacobi.assertEquals(steps.get(this.step++), this.getMatrix());
                op.swapRows(i, j);
                Jacobi.assertEquals(steps.get(this.step++), this.getMatrix());
            }

            @Override
            public void rowOp(int i, double a, int j) {
                op.rowOp(i, a, j);
            }

            @Override
            public Matrix getMatrix() {
                return op.getMatrix();
            }
            
            private int step = 0;
        };
    }
}
