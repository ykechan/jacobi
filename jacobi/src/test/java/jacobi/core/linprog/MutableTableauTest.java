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
    
    @Test
    @JacobiImport("construct 3x5")
    @JacobiEquals(expected = 100, actual = 100)
    @JacobiEquals(expected = 101, actual = 101)
    public void testConstruct3x5() {
        MutableTableau tableau = MutableTableau.of(c, a, b).apply(this.mock());
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
        MutableTableau tableau = MutableTableau.ofAux(c, a, b).apply(this.mock());
        this.vars = Matrices.unsafe(new double[][]{ Arrays.stream(tableau.getVars()).mapToDouble((i) -> i).toArray() });
        tableau.pivot(0, 1);
    }
    /*
    @Test
    @JacobiImport("swap 2,3")
    @JacobiEquals(expected = 100, actual = 100)
    @JacobiEquals(expected = 101, actual = 101)
    public void testSwap2And3() {
        MutableTableau tableau = MutableTableau.of(c, a, b).apply(this.mock());
        Matrix mat = tableau.getMatrix();
        Jacobi.assertEquals(a, this.exclude(mat, 0, 1));
        Jacobi.assertEquals(b, this.column(mat, mat.getRowCount(), mat.getColCount() - 1));
        Jacobi.assertEquals(c, new ColumnVector(tableau.getCoeff()));
        tableau.pivot(2, 3);
        this.vars = Matrices.unsafe(new double[][]{ Arrays.stream(tableau.getVars()).mapToDouble((i) -> i).toArray() });        
    }
    */
    /*
    @Test
    @JacobiImport("swap aux 0,5")
    @JacobiEquals(expected = 100, actual = 100)
    @JacobiEquals(expected = 101, actual = 101)
    public void testSwapAux0And5() {
        MutableTableau tableau = MutableTableau.ofAux(c, a, b).apply(this.mock());
        //Matrix mat = tableau.getMatrix();
        //Jacobi.assertEquals(a, this.exclude(mat, 1, 2));
        //Jacobi.assertEquals(b, this.column(mat, mat.getRowCount() - 1, mat.getColCount() - 1));
        
        //Jacobi.assertEquals(c, new ColumnVector(tableau.getCoeff()));
        tableau.pivot(0, 5);
        this.vars = Matrices.unsafe(new double[][]{ Arrays.stream(tableau.getVars()).mapToDouble((i) -> i).toArray() });        
    }
    */
    @Test
    @JacobiImport("swap aux 3,9 1,3")
    @JacobiEquals(expected = 100, actual = 100)
    @JacobiEquals(expected = 101, actual = 101)
    public void testSwapAux3And9Then1And3() {
        MutableTableau tableau = MutableTableau.ofAux(c, a, b).apply(this.mock());
        this.vars = Matrices.unsafe(new double[][]{ Arrays.stream(tableau.getVars()).mapToDouble((i) -> i).toArray() });
        tableau.pivot(3, 9);
        this.vars2 = Matrices.unsafe(new double[][]{ Arrays.stream(tableau.getVars()).mapToDouble((i) -> i).toArray() });
        tableau.pivot(1, 3);
        this.vars3 = Matrices.unsafe(new double[][]{ Arrays.stream(tableau.getVars()).mapToDouble((i) -> i).toArray() });
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
    
    protected Matrix column(Matrix matrix, int toRow, int col) {
        return new ColumnVector( IntStream.range(0, toRow)
                .mapToDouble((i) -> matrix.get(i, col))
                .toArray() );
    }

    protected Pivoting mock() {
        return (mat, i, j) -> this.tab = mat.copy();
    }
}
