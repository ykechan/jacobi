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
package jacobi.core.decomp.qr.step.shifts;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.core.givens.GivensPair;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.Jacobi;
import jacobi.test.util.JacobiJUnit4ClassRunner;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Y.K. Chan
 */
@JacobiImport("/jacobi/test/data/BulgeMoverTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class BulgeMoverTest {
    
    @JacobiInject(0)
    @JacobiResult(10)
    public Matrix input;
    
    @JacobiResult(11)
    public Matrix givens;
    
    @JacobiInject(-1)
    public Map<Integer, Matrix> steps;

    @Test
    @JacobiImport("6x6")
    @JacobiEquals(expected = 10, actual = 10)
    @JacobiEquals(expected = 11, actual = 11)
    public void test6x6() {
        List<GivensPair> rotList = this.mock(0, 3, 6, true).compute(input, () -> {});
        this.givens = this.toMatrix(rotList);
    }
    
    @Test
    @JacobiImport("5x5 in 7x7")
    @JacobiEquals(expected = 10, actual = 10)
    @JacobiEquals(expected = 11, actual = 11)
    public void test5x5In7x7() {
        List<GivensPair> rotList = this.mock(1, 3, 6, true).compute(input, () -> {});
        this.givens = this.toMatrix(rotList);
    }
    
    @Test
    @JacobiImport("5x5 in 7x7 Partial")
    @JacobiEquals(expected = 10, actual = 10)
    @JacobiEquals(expected = 11, actual = 11)
    public void test5x5In7x7Partial() {
        List<GivensPair> rotList = this.mock(1, 3, 6, false).compute(input, () -> {});
        this.givens = this.toMatrix(rotList);
    }
    
    protected BulgeMover mock(int at, int target, int endRow, boolean full) {
        AtomicInteger step = new AtomicInteger(0);
        return new BulgeMover(at, target, endRow, full){

            @Override
            protected GivensPair pushBulge(Matrix input, int atRow, Runnable listener) {
                GivensPair pair = super.pushBulge(input, atRow, listener);
                Jacobi.assertEquals(steps.get(step.incrementAndGet()), input);
                return pair;
            }
            
        };
    }
    
    protected Matrix toMatrix(List<GivensPair> rotList) {
        return Matrices.of(rotList.stream()
                .map((g) -> Arrays.asList( 
                        new double[]{ g.getUpper().getMag(), g.getUpper().getCos(), g.getUpper().getSin() },
                        new double[]{ g.getLower().getMag(), g.getLower().getCos(), g.getLower().getSin() }                         
                )) 
                .flatMap((l) -> l.stream())
                .toArray((n) -> new double[n][])
        );
    }
    
}
