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

package jacobi.core.stats;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.api.ext.Stats;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.JacobiJUnit4ClassRunner;
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
    
    @Test
    @JacobiImport("12x3")
    @JacobiEquals(expected = 1, actual = 1)
    @JacobiEquals(expected = 2, actual = 2)
    @JacobiEquals(expected = 3, actual = 3)
    @JacobiEquals(expected = 4, actual = 4)
    public void test12x3() {
        this.min = Matrices.of(new double[][]{this.data.ext(Stats.class).min()});
        this.max = Matrices.of(new double[][]{this.data.ext(Stats.class).max()});
        this.mean = Matrices.of(new double[][]{this.data.ext(Stats.class).mean()});
        this.var = Matrices.of(new double[][]{this.data.ext(Stats.class).var()});
    }
    
    @Test
    @JacobiImport("30x5")
    @JacobiEquals(expected = 1, actual = 1)
    @JacobiEquals(expected = 2, actual = 2)
    @JacobiEquals(expected = 3, actual = 3)
    @JacobiEquals(expected = 4, actual = 4)
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
    }

}
