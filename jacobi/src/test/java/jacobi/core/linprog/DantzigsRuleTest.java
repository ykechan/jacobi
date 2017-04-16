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
import jacobi.core.util.IntArray;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.JacobiJUnit4ClassRunner;
import java.util.Arrays;
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
    public Matrix matrix;
    
    @JacobiInject(1)
    public Matrix vars;
    
    @JacobiResult(2)
    public Matrix pivot;
    
    @Test
    @JacobiImport("5 vars no swaps")
    @JacobiEquals(expected = 2, actual = 2)
    public void test5VarsNoSwaps() {
        this.pivot = Matrices.scalar( new DantzigsRule().find(this.matrix, this.toArray(this.vars)) );
    }
    
    @Test
    @JacobiImport("7 vars swapped")
    @JacobiEquals(expected = 2, actual = 2)
    public void test7VarsSwapped() {
        this.pivot = Matrices.scalar( new DantzigsRule().find(this.matrix, this.toArray(this.vars)) );
    }
    
    @Test
    @JacobiImport("8 vars with ties")
    @JacobiEquals(expected = 2, actual = 2)
    public void test8VarsWithTies() {
        this.pivot = Matrices.scalar( new DantzigsRule().find(this.matrix, this.toArray(this.vars)) );
    }
    
    private IntArray toArray(Matrix matrix) {
        return new IntArray(Arrays.stream(matrix.getRow(0)).mapToInt((i) -> (int) i).toArray() );
    }
    
}
