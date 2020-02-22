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
import jacobi.test.util.JacobiJUnit4ClassRunner;
import java.util.Arrays;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Y.K. Chan
 */
@JacobiImport("/jacobi/test/data/DantzigsRuleTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class DantzigsRuleTest {
    
    @JacobiInject(0)
    public Matrix coeff;    
    
    @JacobiInject(1)
    public Matrix vars;    
    
    @JacobiResult(100)
    public Matrix result;   
    
    @Test
    @JacobiImport("test 7 columns")
    @JacobiEquals(expected = 100, actual = 100)
    public void test7columns() {
        int[] pool = new DantzigsRule().apply(this.toTableau(), 1);
        Assert.assertEquals(1, pool.length);
        this.result = Matrices.scalar(pool[0]);
    }
    
    @Test
    @JacobiImport("test 8 columns")
    @JacobiEquals(expected = 100, actual = 100)
    public void test8columns() {
        int[] pool = new DantzigsRule().apply(this.toTableau(), 10);
        Assert.assertEquals(1, pool.length);
        this.result = Matrices.scalar(pool[0]);
    }
    
    @Test
    @JacobiImport("test 6 columns no entry")
    public void test6columnsNoEntry() {
        Assert.assertEquals(0, new DantzigsRule().apply(this.toTableau(), 5).length);
    }
    
    @Test
    @JacobiImport("test 9 columns with duplicates")
    @JacobiEquals(expected = 100, actual = 100)
    public void test9columnsWithDuplicates() {
        this.result = Matrices.scalar( new DantzigsRule().apply(this.toTableau(), 1)[0] );
    }
    
    @Test    
    public void testAllZeroesNoEntry() {
        double[] row = new double[10];
        Arrays.fill(row, 0.0);        
        this.coeff = Matrices.of(new double[][]{ row });
        this.vars = Matrices.of(new double[][]{ IntStream.range(0, row.length).mapToDouble((v) -> v).toArray() });
        Assert.assertEquals(0, new DantzigsRule().apply(this.toTableau(), 1).length);
    }
    
    private Tableau toTableau() {
        return new Tableau() {

            @Override
            public Matrix getMatrix() {
                // Dantzig's rule doesn't need constraint matrix
                throw new UnsupportedOperationException();
            }

            @Override
            public double[] getCoeff() {
                return Arrays.copyOf(coeff.getRow(0), coeff.getColCount());
            }

            @Override
            public int[] getVars() {
                return DoubleStream.of(vars.getRow(0)).mapToInt((d) -> (int) d).toArray();
            }
        };
    }
}
