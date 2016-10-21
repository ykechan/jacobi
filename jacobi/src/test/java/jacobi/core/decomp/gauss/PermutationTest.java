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
package jacobi.core.decomp.gauss;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.api.ext.Op;
import jacobi.test.util.Jacobi;
import java.util.Arrays;
import java.util.stream.IntStream;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Y.K. Chan
 */
public class PermutationTest {
    
    @Test
    public void testNormal() {
        Matrix perm = new Permutation(5);
        // a default permutation matrix is identicial to an identity matrix
        Jacobi.assertEquals(Matrices.identity(5), perm);
        // but Permutation is immutable, thus this constructor is not much useful
        
        int[] indices = IntStream.range(0, 6).toArray();
        // 0 4 2 3 1 5
        indices[1] = 4; indices[4] = 1;
        
        Permutation p = new Permutation(indices, 100); // order is trusted
        Assert.assertEquals(100.0, p.det(), 1e-16);
        
        // permutation is equvalient to swapping rows in identity matrix
        Jacobi.assertEquals(Matrices.identity(6).swapRow(1, 4), p);
        
        Matrix clone = p.copy();
        indices[1] = 1; indices[4] = 1;
        
        // clones are detach with orginal matrix
        Jacobi.assertEquals(Matrices.identity(6).swapRow(1, 4), clone);
    }
    
    @Test
    public void testMul() {
        // multiply a permutation matrix with another matrix
        // is equivalent to swapping rows
        Matrix matrix = Matrices.zeros(5, 3)
                .getAndSet(0, (r) -> Arrays.fill(r, 0.0))
                .getAndSet(1, (r) -> Arrays.fill(r, 1.0))
                .getAndSet(2, (r) -> Arrays.fill(r, 2.0))
                .getAndSet(3, (r) -> Arrays.fill(r, 3.0))
                .getAndSet(4, (r) -> Arrays.fill(r, 4.0));
        int[] indices = IntStream.range(0, matrix.getRowCount()).toArray();
        indices[0] = 4; indices[4] = 0;
        indices[2] = 3; indices[3] = 2;
        
        Permutation perm = new Permutation(indices, 1);
        Jacobi.assertEquals(matrix.copy().swapRow(0, 4).swapRow(2, 3), perm.mul(matrix));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testMulWithNull() {
        new Permutation(5).ext(Op.class).mul(null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testMulRowCountMismatch() {
        new Permutation(5).ext(Op.class).mul(Matrices.zeros(7, 3));
    }
    
}
