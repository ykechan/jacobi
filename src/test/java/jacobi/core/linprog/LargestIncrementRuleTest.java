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

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.Jacobi;
import jacobi.test.util.JacobiJUnit4ClassRunner;
import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Y.K. Chan
 */
@JacobiImport("/jacobi/test/data/LargestIncrementRuleTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class LargestIncrementRuleTest {
    
    @JacobiInject(0)
    public Matrix objective;
    
    @JacobiInject(1)
    public Matrix constraint;
    
    @JacobiInject(2)
    public Matrix boundary;
    
    @JacobiResult(10)
    public Matrix pivots;
    
    @JacobiInject(11)
    public Matrix delta;
    
    @Test
    @JacobiImport("5x6 Top 3")
    @JacobiEquals(expected = 10, actual = 10)
    public void test5x6Top3() {
        Tableau tab = MutableTableau.build(false).use((mat, row, col) -> {}).of(objective, constraint, boundary);
        int[] pvts = this.mock(delta).apply(tab, 3);
        this.pivots = Matrices.wrap(new double[][]{ Arrays.stream(pvts).mapToDouble((i) -> i).toArray() } );
    }
    
    @Test
    @JacobiImport("4x7 Top 4")
    @JacobiEquals(expected = 10, actual = 10)
    public void test4x7Top4() {
        Tableau tab = MutableTableau.build(false).use((mat, row, col) -> {}).of(objective, constraint, boundary);
        int[] pvts = this.mock(delta).apply(tab, 4);
        this.pivots = Matrices.wrap(new double[][]{ Arrays.stream(pvts).mapToDouble((i) -> i).toArray() } );
    }
    
    @Test
    @JacobiImport("4x6 Unbounded")
    @JacobiEquals(expected = 10, actual = 10)
    public void test4x6Unbounded() {
        Tableau tab = MutableTableau.build(false).use((mat, row, col) -> {}).of(objective, constraint, boundary);
        int[] pvts = new LargestIncrementRule().apply(tab, 4);
        this.pivots = Matrices.wrap(new double[][]{ Arrays.stream(pvts).mapToDouble((i) -> i).toArray() } );
    }
    
    protected LargestIncrementRule mock(Matrix delta) {
        return new LargestIncrementRule(){

            @Override
            protected double[] compute(Matrix mat, double[] coeff) {
                double[] array = super.compute(mat, coeff);
                Jacobi.assertEquals(delta, Matrices.wrap(new double[][]{array}));
                return array;
            }
            
        };
    }
    
}
