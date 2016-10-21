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
package jacobi.core.op;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.core.stats.RowReduce;
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
    
    @Test(expected = IllegalStateException.class)
    public void testInvalidRowRange() {
        new RowReduce( (a, b) -> {} ){

            @Override
            public double[] compute(Matrix matrix) {
                return this.serial(matrix, 0, 0);
            }
            
        }.compute(Matrices.identity(1));
        
    }
    
    @Test(expected = IllegalArgumentException.class)    
    public void testInvalidMatrix() {
        new RowReduce.Max().compute(null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testRowCountMismatch() {
        new RowBased((u, v, w) -> null)
                .compute(Matrices.identity(3), Matrices.identity(4));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testColCountMismatch() {
        new RowBased((u, v, w) -> null)
                .compute(Matrices.zeros(1, 3), Matrices.zeros(1, 4));
    }
    
}
