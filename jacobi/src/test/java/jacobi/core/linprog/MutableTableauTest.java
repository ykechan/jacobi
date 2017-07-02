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
import jacobi.core.impl.ColumnVector;
import jacobi.core.impl.DefaultMatrix;
import jacobi.core.impl.ImmutableMatrix;
import jacobi.core.linprog.MutableTableau.Pivoting;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.Jacobi;
import jacobi.test.util.JacobiJUnit4ClassRunner;
import java.util.Arrays;
import java.util.stream.IntStream;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Y.K. Chan
 */
@JacobiImport("/jacobi/test/data/MutableTableauTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class MutableTableauTest {
    
    @JacobiInject(0)
    public Matrix c;
    
    @JacobiInject(1)
    public Matrix a;
    
    @JacobiInject(2)
    public Matrix b;
    
    @JacobiResult(100)
    public Matrix tab;
    
    @JacobiResult(101)
    public Matrix vars;
    
    @JacobiResult(102)
    public Matrix vars2;
    
    @JacobiResult(103)
    public Matrix vars3;
    
    @JacobiResult(10)
    public Matrix before;
    
    @JacobiResult(11)
    public Matrix swapped1;
    
    @JacobiResult(12)
    public Matrix swapped2;
    
    @JacobiResult(13)
    public Matrix collapsed;
    
    @Test
    @JacobiImport("construct 3x5")
    @JacobiEquals(expected = 100, actual = 100)
    @JacobiEquals(expected = 101, actual = 101)
    public void testConstruct3x5() {
        MutableTableau tableau = MutableTableau.build(false).use(this.mock()).of(c, a, b);
        Matrix mat = tableau.getMatrix();
        Jacobi.assertEquals(a, this.exclude(mat, 0, 1));
        Jacobi.assertEquals(b, this.column(mat, mat.getRowCount(), mat.getColCount() - 1));
        Jacobi.assertEquals(c, new ColumnVector(tableau.getCoeff()));
        this.vars = Matrices.unsafe(new double[][]{ Arrays.stream(tableau.getVars()).mapToDouble((i) -> i).toArray() });
        tableau.pivot(0, 1);
    }
    
    @Test
    @JacobiImport("construct 3x5")
    @JacobiEquals(expected = 100, actual = 100)
    @JacobiEquals(expected = 101, actual = 101)
    public void testConstruct3x5UsingDefaultMatrixAsColumnMatrix() {
        MutableTableau tableau = MutableTableau
                .build(false)
                .use(this.mock()).of(new DefaultMatrix(c), a, new DefaultMatrix(b));
        Matrix mat = tableau.getMatrix();
        Jacobi.assertEquals(a, this.exclude(mat, 0, 1));
        Jacobi.assertEquals(b, this.column(mat, mat.getRowCount(), mat.getColCount() - 1));
        Jacobi.assertEquals(c, new ColumnVector(tableau.getCoeff()));
        this.vars = Matrices.unsafe(new double[][]{ Arrays.stream(tableau.getVars()).mapToDouble((i) -> i).toArray() });
        tableau.pivot(0, 1);
    }
    
    @Test
    @JacobiImport("construct aux 4x6")
    @JacobiEquals(expected = 100, actual = 100)
    @JacobiEquals(expected = 101, actual = 101)
    public void testConstructAux4x6() {
        MutableTableau tableau = MutableTableau.build(true).use(this.mock()).of(c, a, b);
        this.vars = Matrices.unsafe(new double[][]{ Arrays.stream(tableau.getVars()).mapToDouble((i) -> i).toArray() });
        tableau.pivot(0, 1);
    }
    
    @Test
    @JacobiImport("swap 2,3")
    @JacobiEquals(expected = 100, actual = 100)
    @JacobiEquals(expected = 101, actual = 101)
    public void testSwap2And3() {
        MutableTableau tableau = MutableTableau.build(false).use(this.mock()).of(c, a, b);
        Matrix mat = tableau.getMatrix();
        Jacobi.assertEquals(a, this.exclude(mat, 0, 1));
        Jacobi.assertEquals(b, this.column(mat, mat.getRowCount(), mat.getColCount() - 1));
        Jacobi.assertEquals(c, new ColumnVector(tableau.getCoeff()));
        tableau.pivot(2, 3);
        this.vars = Matrices.unsafe(new double[][]{ Arrays.stream(tableau.getVars()).mapToDouble((i) -> i).toArray() });        
    }
    
    
    @Test
    @JacobiImport("swap aux 3,8")
    @JacobiEquals(expected = 100, actual = 100)
    @JacobiEquals(expected = 101, actual = 101)
    public void testSwapAux3And8() {
        MutableTableau tableau = MutableTableau.build(true).use(this.mock()).of(c, a, b);
        Matrix mat = tableau.getMatrix();
        Jacobi.assertEquals(a, this.exclude(mat, 0, 2));
        Jacobi.assertEquals(b, this.column(mat, mat.getRowCount(), mat.getColCount() - 1));
        
        tableau.pivot(3, 8);
        this.vars = Matrices.unsafe(new double[][]{ Arrays.stream(tableau.getVars()).mapToDouble((i) -> i).toArray() });        
    }
    
    @Test
    @JacobiImport("swap aux 3,9 1,3")
    @JacobiEquals(expected = 100, actual = 100)
    @JacobiEquals(expected = 101, actual = 101)
    public void testSwapAux3And9Then1And3() {
        MutableTableau tableau = MutableTableau.build(true).use(this.mock()).of(c, a, b);
        this.vars = Matrices.unsafe(new double[][]{ Arrays.stream(tableau.getVars()).mapToDouble((i) -> i).toArray() });
        tableau.pivot(3, 9);
        this.vars2 = Matrices.unsafe(new double[][]{ Arrays.stream(tableau.getVars()).mapToDouble((i) -> i).toArray() });
        tableau.pivot(1, 3);
        this.vars3 = Matrices.unsafe(new double[][]{ Arrays.stream(tableau.getVars()).mapToDouble((i) -> i).toArray() });
    }
    
    @Test
    @JacobiImport("swap aux 2,5 2,2 and collapse")
    @JacobiEquals(expected = 10, actual = 10)    
    @JacobiEquals(expected = 11, actual = 11)
    @JacobiEquals(expected = 101, actual = 101)
    @JacobiEquals(expected = 12, actual = 12)
    @JacobiEquals(expected = 102, actual = 102)
    @JacobiEquals(expected = 13, actual = 13)
    @JacobiEquals(expected = 103, actual = 103)
    public void testSwapAux2x5Then2x2ThenCollapse() {
        MutableTableau tableau = MutableTableau.build(true).use(this.mockReset(1)).of(c, a, b);
        this.before = this.asMatrix(tableau);
        tableau.pivot(2, 5);
        this.swapped1 = this.asMatrix(tableau);
        this.vars = Matrices.unsafe(new double[][]{ Arrays.stream(tableau.getVars()).mapToDouble((i) -> i).toArray() });
        tableau.pivot(2, 2);
        this.swapped2 = this.asMatrix(tableau);
        this.vars2 = Matrices.unsafe(new double[][]{ Arrays.stream(tableau.getVars()).mapToDouble((i) -> i).toArray() });
        tableau = tableau.collapse().get();
        this.collapsed = this.asMatrix(tableau);
        this.vars3 = Matrices.unsafe(new double[][]{ Arrays.stream(tableau.getVars()).mapToDouble((i) -> i).toArray() });
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void testMatrixGetterIsImmutable() {
        MutableTableau tableau = MutableTableau.build(false)
                .use(this.mock())
                .of(Matrices.zeros(5, 1), Matrices.zeros(4, 5), Matrices.zeros(4, 1));
        tableau.getMatrix().getAndSet(0, (r) -> Arrays.fill(r, 1.0));
    }
    
    @Test
    public void testCoeffVarsGetterIsImmutable() {
        MutableTableau tableau = MutableTableau.build(false)
                .use(this.mock())
                .of(Matrices.zeros(5, 1), Matrices.zeros(4, 5), Matrices.zeros(4, 1));
        int[] vars = tableau.getVars();
        double[] coeff = tableau.getCoeff();
        double[] origCoeff = Arrays.copyOf(coeff, coeff.length);
        int[] origVars = Arrays.copyOf(vars, vars.length);
        Arrays.fill(coeff, 1.0);
        Arrays.fill(vars, 1);
        Assert.assertArrayEquals(origCoeff, tableau.getCoeff(), 1e-16);
        Assert.assertArrayEquals(origVars, tableau.getVars());
    }    
    
    @Test(expected = IllegalArgumentException.class)
    public void testCoeffIsNotColumnVector() {
        MutableTableau.build(false)
            .use(this.mock())
            .of(Matrices.zeros(5, 2), Matrices.zeros(4, 5), Matrices.zeros(4, 1));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testBoundaryIsNotColumnVector() {
        MutableTableau.build(false)
            .use(this.mock())
            .of(Matrices.zeros(5, 1), Matrices.zeros(4, 5), Matrices.zeros(4, 2));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testDimensionMismatchOnConstraintAndObjective() {
        MutableTableau.build(false)
            .use(this.mock())
            .of(Matrices.zeros(6, 1), Matrices.zeros(4, 5), Matrices.zeros(4, 1));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testDimensionMismatchOnConstraint() {
        MutableTableau.build(false)
            .use(this.mock())
            .of(Matrices.zeros(5, 1), Matrices.zeros(5, 5), Matrices.zeros(4, 1));
    }

    protected Matrix exclude(Matrix matrix, int numRow, int numCol) {
        return new ImmutableMatrix() {

            @Override
            public int getRowCount() {
                return matrix.getRowCount() - numRow;
            }

            @Override
            public int getColCount() {
                return matrix.getColCount() - numCol;
            }

            @Override
            public double[] getRow(int index) {
                return Arrays.copyOf(matrix.getRow(index), this.getColCount());
            }
        };
    } 
    
    protected Matrix asMatrix(Tableau tab) {
        Matrix constraint = tab.getMatrix().copy();
        double[] coeff = Arrays.copyOf(tab.getCoeff(), constraint.getColCount());
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
                        ? coeff
                        : constraint.getRow(index);
            }
        };
    }
    
    protected Matrix column(Matrix matrix, int toRow, int col) {
        return new ColumnVector( IntStream.range(0, toRow)
                .mapToDouble((i) -> matrix.get(i, col))
                .toArray() );
    }

    protected Pivoting mock() {
        return (mat, i, j) -> this.tab = mat.copy();
    }
    
    protected Pivoting mockReset(int value) {        
        return (mat, i, j) -> {
            for(int k = 0; k < mat.getRowCount(); k++){
                mat.set(k, j, value);
            }
            this.tab = mat.copy();
        };
    }
}
