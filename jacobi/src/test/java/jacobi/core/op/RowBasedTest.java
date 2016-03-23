/*
 * Copyright (C) 2015 Y.K. Chan
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
package jacobi.core.op;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.JacobiJUnit4ClassRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Edwin
 */
@JacobiImport("/jacobi/test/data/RowBasedTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class RowBasedTest {
    @JacobiInject(1)
    public Matrix a;
    
    @JacobiInject(2)
    public Matrix b;
    
    @JacobiResult(3)
    public Matrix add;
    
    @JacobiResult(4)
    public Matrix sub;
    
    @JacobiResult(5)
    public Matrix hadamard;
    
    @JacobiResult(6)
    public Matrix hstream;
    
    @JacobiResult(7)
    public Matrix hfork;
    
    @Test
    @JacobiImport("rand3x3")
    @JacobiEquals(expected = 3, actual = 3)
    @JacobiEquals(expected = 4, actual = 4)
    @JacobiEquals(expected = 5, actual = 5)
    @JacobiEquals(expected = 5, actual = 6)
    @JacobiEquals(expected = 5, actual = 7)
    public void testRand3x3() {
        this.add = new Operators.Add().compute(a, b);
        this.sub = new Operators.Sub().compute(b, a);
        this.hadamard = new Operators.Hadamard().compute(a, b);
        this.hstream = Matrices.zeros(this.a.getRowCount(), this.a.getColCount());
        new Operators.Hadamard().stream(a, b, this.hstream);
        this.hfork = Matrices.zeros(this.a.getRowCount(), this.a.getColCount());
        new Operators.Hadamard().forkJoin(a, b, this.hfork, 3);
    }
    
    @Test
    @JacobiImport("rand5x5")
    @JacobiEquals(expected = 3, actual = 3)
    @JacobiEquals(expected = 4, actual = 4)
    @JacobiEquals(expected = 5, actual = 5)
    @JacobiEquals(expected = 5, actual = 6)
    @JacobiEquals(expected = 5, actual = 7)
    public void testRand5x5() {
        this.add = new Operators.Add().compute(a, b);
        this.sub = new Operators.Sub().compute(b, a);
        this.hadamard = new Operators.Hadamard().compute(a, b);
        this.hstream = Matrices.zeros(this.a.getRowCount(), this.a.getColCount());
        new Operators.Hadamard().stream(a, b, this.hstream);
        this.hfork = Matrices.zeros(this.a.getRowCount(), this.a.getColCount());
        new Operators.Hadamard().forkJoin(a, b, this.hfork, 3);
    }
    
    @Test
    @JacobiImport("rand7x7")
    @JacobiEquals(expected = 3, actual = 3)
    @JacobiEquals(expected = 4, actual = 4)
    @JacobiEquals(expected = 5, actual = 5)
    @JacobiEquals(expected = 5, actual = 6)
    @JacobiEquals(expected = 5, actual = 7)
    public void testRand7x7() {
        this.add = new Operators.Add().compute(a, b);
        this.sub = new Operators.Sub().compute(b, a);
        this.hadamard = new Operators.Hadamard().compute(a, b);
        this.hstream = Matrices.zeros(this.a.getRowCount(), this.a.getColCount());
        new Operators.Hadamard().stream(a, b, this.hstream);
        this.hfork = Matrices.zeros(this.a.getRowCount(), this.a.getColCount());
        new Operators.Hadamard().forkJoin(a, b, this.hfork, 3);
    }
    
    @Test
    @JacobiImport("randInt5x5")
    @JacobiEquals(expected = 3, actual = 3)
    @JacobiEquals(expected = 4, actual = 4)
    @JacobiEquals(expected = 5, actual = 5)
    @JacobiEquals(expected = 5, actual = 6)
    @JacobiEquals(expected = 5, actual = 7)
    public void testRandInt5x5() {
        this.add = new Operators.Add().compute(a, b);
        this.sub = new Operators.Sub().compute(b, a);
        this.hadamard = new Operators.Hadamard().compute(a, b);
        this.hstream = Matrices.zeros(this.a.getRowCount(), this.a.getColCount());
        new Operators.Hadamard().stream(a, b, this.hstream);
        this.hfork = Matrices.zeros(this.a.getRowCount(), this.a.getColCount());
        new Operators.Hadamard().forkJoin(a, b, this.hfork, 3);
    }
    
    @Test
    @JacobiImport("randInt7x7")
    @JacobiEquals(expected = 3, actual = 3)
    @JacobiEquals(expected = 4, actual = 4)
    @JacobiEquals(expected = 5, actual = 5)
    @JacobiEquals(expected = 5, actual = 6)
    @JacobiEquals(expected = 5, actual = 7)
    public void testRandInt7x7() {
        this.add = new Operators.Add().compute(a, b);
        this.sub = new Operators.Sub().compute(b, a);
        this.hadamard = new Operators.Hadamard().compute(a, b);
        this.hstream = Matrices.zeros(this.a.getRowCount(), this.a.getColCount());
        new Operators.Hadamard().stream(a, b, this.hstream);
        this.hfork = Matrices.zeros(this.a.getRowCount(), this.a.getColCount());
        new Operators.Hadamard().forkJoin(a, b, this.hfork, 3);
    }
    
    @Test
    @JacobiImport("rand3x5")
    @JacobiEquals(expected = 3, actual = 3)
    @JacobiEquals(expected = 4, actual = 4)
    @JacobiEquals(expected = 5, actual = 5)
    @JacobiEquals(expected = 5, actual = 6)
    @JacobiEquals(expected = 5, actual = 7)
    public void testRand3x5() {
        this.add = new Operators.Add().compute(a, b);
        this.sub = new Operators.Sub().compute(b, a);
        this.hadamard = new Operators.Hadamard().compute(a, b);
        this.hstream = Matrices.zeros(this.a.getRowCount(), this.a.getColCount());
        new Operators.Hadamard().stream(a, b, this.hstream);
        this.hfork = Matrices.zeros(this.a.getRowCount(), this.a.getColCount());
        new Operators.Hadamard().forkJoin(a, b, this.hfork, 3);
    }
    
    @Test
    @JacobiImport("rand7x5")
    @JacobiEquals(expected = 3, actual = 3)
    @JacobiEquals(expected = 4, actual = 4)
    @JacobiEquals(expected = 5, actual = 5)
    @JacobiEquals(expected = 5, actual = 6)
    @JacobiEquals(expected = 5, actual = 7)
    public void testRand7x5() {
        this.add = new Operators.Add().compute(a, b);
        this.sub = new Operators.Sub().compute(b, a);
        this.hadamard = new Operators.Hadamard().compute(a, b);
        this.hstream = Matrices.zeros(this.a.getRowCount(), this.a.getColCount());
        new Operators.Hadamard().stream(a, b, this.hstream);
        this.hfork = Matrices.zeros(this.a.getRowCount(), this.a.getColCount());
        new Operators.Hadamard().forkJoin(a, b, this.hfork, 3);
    }
}
