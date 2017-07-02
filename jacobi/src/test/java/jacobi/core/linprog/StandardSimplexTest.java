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

import jacobi.api.Matrix;
import jacobi.core.impl.ImmutableMatrix;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.Jacobi;
import jacobi.test.util.JacobiJUnit4ClassRunner;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Y.K. Chan
 */
@JacobiImport("/jacobi/test/data/StandardSimplexTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class StandardSimplexTest {
    
    @JacobiInject(0)
    public Matrix c;
    
    @JacobiInject(1)
    public Matrix a;
    
    @JacobiInject(2)
    public Matrix b;
    
    @JacobiInject(-1)
    public Map<Integer, Matrix> steps;
    
    @JacobiResult(1000)
    public Matrix x;
    
    @Test
    @JacobiImport("Dantzigs Rule 5x6")
    @JacobiEquals(expected = 1000, actual = 1000)
    public void testDanzigsRule5x6() {
        this.x = new StandardSimplex(0.0, 0.0, 256L, this.assertBefore(new DantzigsRule(), 10)).compute(c, a, b)
                .orElseThrow(() -> new IllegalStateException("Answer un-obtained"));
    }
    
    @Test
    @JacobiImport("Aux problem 4x5")
    //@JacobiEquals(expected = 1000, actual = 1000)
    public void testAuxProblem4x5() {
        this.x = new StandardSimplex(0.0, 0.0, 256L, this.assertBefore(new DantzigsRule(), 10)).compute(c, a, b)
                .orElseThrow(() -> new IllegalStateException("Answer un-obtained"));
    }
    
    @Test
    @JacobiImport("Aux Infeasible Problem 5x6")
    public void testAuxInfeasibleProblem5x6() {
        Assert.assertFalse(
                new StandardSimplex(0.0, 0.0, 256L, this.assertBefore(new DantzigsRule(), 10)).compute(c, a, b)
                    .isPresent()
        );
    }
    
    @Test
    @JacobiEquals(expected = 1000, actual = 1000)
    @JacobiImport("Aux feasible Problem 4x6")    
    public void testAuxFeasibleProblem4x6() {
        this.x =  new StandardSimplex(0.0, 0.0, 256L, this.assertBefore(new DantzigsRule(), 10)).compute(c, a, b)
                    .get();
    }
    
    @Test(expected = IllegalStateException.class)
    @JacobiImport("Dantzigs Rule 5x6")
    public void testExhaused() {
        this.x = new StandardSimplex(1L, this.wrongRule()).compute(c, a, b)
                .orElseThrow(() -> new IllegalStateException("Answer un-obtained"));
    }
    
    @Test
    @JacobiImport("Unbounded Problem 4x5")    
    public void testUnboundedProblem4x5() {
        Assert.assertFalse(new StandardSimplex(0.0, 0.0, 256L, new DantzigsRule()).compute(c, a, b).isPresent());
    }
    
    protected PivotingRule wrongRule() {
        return (t, u) -> {
            double[] coeff = t.getCoeff();
            for(int i = 0; i < coeff.length; i++){
                if(coeff[i] <= 0.0){
                    return new int[]{i};
                }
            }
            throw new UnsupportedOperationException("Not able to be wrong");
        };
    }
    
    protected PivotingRule assertBefore(PivotingRule rule, int from) {
        AtomicInteger step = new AtomicInteger(from);
        Map<Integer, Matrix> expected = steps;
        return (tab, num) -> {            
            int[] enter = rule.apply(tab, num);
            int k = step.getAndIncrement();
            if(expected.containsKey(k)){
                Jacobi.assertEquals(expected.get(k), this.asMatrix(tab));
            }
            return enter;
        };
    }
    
    protected Matrix asMatrix(Tableau tab) {
        Matrix constraint = tab.getMatrix();
        return new ImmutableMatrix() {

            @Override
            public int getRowCount() {
                return constraint.getRowCount() + 1;
            }

            @Override
            public int getColCount() {
                return constraint.getColCount();
            }

            @Override
            public double[] getRow(int index) {
                return index == constraint.getRowCount()
                        ? Arrays.copyOf(tab.getCoeff(), constraint.getColCount())
                        : constraint.getRow(index);
            }
        };
    }
    
}
