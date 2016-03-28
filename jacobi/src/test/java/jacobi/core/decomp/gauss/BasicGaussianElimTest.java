/*
 * Copyright (C) 2016 Y.K. Chan
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
    
    @Test
    @JacobiImport("4x4")
    public void test4x4() {
        new GenericGaussianElim<>(
            (op) -> this.assertByStep(op, Arrays.asList(
                input,
                step1, step2, step3,
                step4, step5, step6
            ))
        ).compute(this.input);
    }
    
    @Test
    @JacobiImport("5x5")
    public void test5x5() {
        new GenericGaussianElim<>(
            (op) -> this.assertByStep(op, Arrays.asList(
                input,
                step1, step2, step3,
                step4, step5, step6,
                step7, step8
            ))
        ).compute(this.input);
    }
    
    @Test
    @JacobiImport("UnderDet 4x4")
    public void testUnderDet4x4() {
        new GenericGaussianElim<>(
            (op) -> this.assertByStep(op, Arrays.asList(
                input,
                step1, step2, step3,
                step4, step5, step6
            ))
        ).compute(this.input);
    }
    
    @Test
    @JacobiImport("Skip 4x4")
    public void testSkip4x4() {
        new GenericGaussianElim<>(
            (op) -> this.assertByStep(op, Arrays.asList(
                input,
                step1, step2, step3, step4, step5, step6
            ))
        ).compute(this.input);
    }
    
    @Test
    public void testNull5x5() {
        this.input = Matrices.zeros(5, 5);
        new GenericGaussianElim<>(
            Function.identity()
        ).compute(this.input);
        Jacobi.assertEquals(Matrices.zeros(5, 5), input);
    }
    
    @Test
    @JacobiImport("7x3")
    public void test7x3() {
        new GenericGaussianElim<>(
            (op) -> this.assertByStep(op, Arrays.asList(
                input,
                step1, step2, step3, 
                step4, step5, step6
            ))
        ).compute(this.input);
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
