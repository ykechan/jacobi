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
package jacobi.core.decomp.qr;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.api.ext.Op;
import jacobi.api.ext.Prop;
import jacobi.core.decomp.qr.step.QRStep;
import jacobi.core.util.Triplet;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.Jacobi;
import jacobi.test.util.JacobiJUnit4ClassRunner;
import java.util.stream.DoubleStream;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * The answer used in this test is computed by SchurDecomp itself, and verified by spreadsheet. Since Schur form is
 * not unique, for fail cases compute the answer and change the spreadsheet itself. (Of course it should pass the
 * spreadsheet verification)
 * 
 * @author Y.K. Chan
 */
@JacobiImport("/jacobi/test/data/SchurDecompTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class SchurDecompTest {
    
    @JacobiInject(0)
    @JacobiResult(1)
    public Matrix input;    
    
    @JacobiResult(2)
    public Matrix ortho;
    
    @Test
    @JacobiImport("4x4")
    //@JacobiEquals(expected = 1, actual = 1)
    //@JacobiEquals(expected = 2, actual = 2)
    public void test4x4() {
        Matrix orig = this.input.copy();
        this.ortho = new SchurDecomp().computeBoth(input).getLeft();
        this.isSchur(input);
        Jacobi.assertEquals(Matrices.identity(4), ortho.ext(Op.class).mul(ortho.ext(Prop.class).transpose()).get());
        Jacobi.assertEquals(orig, ortho.ext(Op.class)
                .mul(input)
                .mul(ortho.ext(Prop.class).transpose()).get());        
    }
    
    @Test
    @JacobiImport("5x5")
    //@JacobiEquals(expected = 1, actual = 1)
    //@JacobiEquals(expected = 2, actual = 2)
    public void test5x5() {
        Matrix orig = this.input.copy();
        this.ortho = new SchurDecomp().computeBoth(input).getLeft();
        this.isSchur(input);
        
        Jacobi.assertEquals(Matrices.identity(5), ortho.ext(Op.class).mul(ortho.ext(Prop.class).transpose()).get());
        Jacobi.assertEquals(orig, ortho.ext(Op.class)
                .mul(input)
                .mul(ortho.ext(Prop.class).transpose()).get());  
    }
    
    @Test
    @JacobiImport("6x6")
    //@JacobiEquals(expected = 1, actual = 1)
    //@JacobiEquals(expected = 2, actual = 2)
    public void test6x6() {
        Matrix orig = this.input.copy();
        Triplet uev = new SchurDecomp().computeAll(input);
        this.ortho = uev.getLeft();
        this.isSchur(input);
        Jacobi.assertEquals(Matrices.identity(6), ortho.ext(Op.class).mul(ortho.ext(Prop.class).transpose()).get());
        Jacobi.assertEquals(orig, ortho.ext(Op.class)
                .mul(input)
                .mul(uev.getRight()).get());
        
        Matrix schur = new SchurDecomp().compute(orig);
        Jacobi.assertEquals(input, schur);
    }
    
    private void isSchur(Matrix matrix) { 
        for(int i = 0; i < matrix.getRowCount(); i++){
            double[] row = matrix.getRow(i);
            double max = DoubleStream.of(row).limit(Math.max(0, i - 1)).map((d) -> Math.abs(d)).max().orElse(0.0);
            Assert.assertTrue("Row " + i + ", max = " + max, max < QRStep.EPSILON);            
        }
    }
 
    /*
    private void print(Matrix matrix){
        for(double[] row : matrix.toArray()){
            for(double elem : row){
                System.out.print(elem + "\t");
            }
            System.out.println();
        }
    }
    */
}
