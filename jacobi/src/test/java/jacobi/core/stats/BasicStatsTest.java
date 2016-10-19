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
package jacobi.core.stats;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.api.ext.Stats;
import jacobi.core.impl.Empty;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.JacobiJUnit4ClassRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Y.K. Chan
 */
@JacobiImport("/jacobi/test/data/BasicStatsTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class BasicStatsTest {
    
    @JacobiInject(0)
    public Matrix data;
    
    @JacobiResult(1)
    public Matrix min;
    
    @JacobiResult(2)
    public Matrix max;
    
    @JacobiResult(3)
    public Matrix mean;
    
    @JacobiResult(4)
    public Matrix var;
    
    @JacobiResult(5)
    public Matrix stddev;
    
    @JacobiResult(6)
    public Matrix covar;
    
    @Test
    @JacobiImport("12x3")
    @JacobiEquals(expected = 1, actual = 1)
    @JacobiEquals(expected = 2, actual = 2)
    @JacobiEquals(expected = 3, actual = 3)
    @JacobiEquals(expected = 4, actual = 4)
    @JacobiEquals(expected = 5, actual = 5)
    @JacobiEquals(expected = 6, actual = 6)
    public void test12x3() {
        this.min = Matrices.of(new double[][]{this.data.ext(Stats.class).min()});
        this.max = Matrices.of(new double[][]{this.data.ext(Stats.class).max()});
        this.mean = Matrices.of(new double[][]{this.data.ext(Stats.class).mean()});
        this.var = Matrices.of(new double[][]{this.data.ext(Stats.class).var()});        
        this.stddev = Matrices.of(new double[][]{this.data.ext(Stats.class).stdDev()});
        this.covar = this.data.ext(Stats.class).covar();
    }
    
    @Test
    @JacobiImport("30x5")
    @JacobiEquals(expected = 1, actual = 1)
    @JacobiEquals(expected = 2, actual = 2)
    @JacobiEquals(expected = 3, actual = 3)
    @JacobiEquals(expected = 4, actual = 4)
    @JacobiEquals(expected = 5, actual = 5)
    @JacobiEquals(expected = 6, actual = 6)
    public void test30x5() {
        this.min = Matrices.of(new double[][]{
            new RowReduce.Min().compute(this.data)
        });
        this.max = Matrices.of(new double[][]{
            new RowReduce.Max().compute(this.data)
        });
        this.mean = Matrices.of(new double[][]{
            new RowReduce.Mean().compute(this.data)
        });
        this.var = Matrices.of(new double[][]{
            new Variance().compute(this.data)
        });
        this.stddev = Matrices.of(new double[][]{
            new Variance.StdDev().compute(this.data)
        });
        this.covar = new Covar().compute(this.data);
    }

    @Test
    public void testEmpty() {
        Assert.assertArrayEquals(new double[0], new RowReduce.Min().compute(Empty.getInstance()), 1e-16);
        Assert.assertArrayEquals(new double[0], new RowReduce.Max().compute(Empty.getInstance()), 1e-16);
        Assert.assertArrayEquals(new double[0], new RowReduce.Mean().compute(Empty.getInstance()), 1e-16);
        Assert.assertArrayEquals(new double[0], new Variance().compute(Empty.getInstance()), 1e-16);
        Assert.assertArrayEquals(new double[0], new Variance.StdDev().compute(Empty.getInstance()), 1e-16);
    }
}
