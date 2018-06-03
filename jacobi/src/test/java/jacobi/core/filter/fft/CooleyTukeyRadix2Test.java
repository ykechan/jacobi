/*
 * The MIT License
 *
 * Copyright (c) 2018 Y.K. Chan.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package jacobi.core.filter.fft;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.JacobiJUnit4ClassRunner;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;

import java.util.stream.IntStream;
import org.junit.runner.RunWith;

@JacobiImport("/jacobi/test/data/CooleyTukeyRadix2Test.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class CooleyTukeyRadix2Test {
    
    @JacobiInject(0)
    public Matrix input;
    
    @JacobiResult(1)
    public Matrix output;
        
    @Test
    @JacobiImport("Pure Real 10")
    @JacobiEquals(expected = 1, actual = 1)
    public void testPureReal10() {
        ComplexVector vec = ComplexVector.of(this.input.getRow(0), this.input.getRow(1));        
        this.mockNoPivot().merge(vec, 0, vec.length());
        this.output = Matrices.of(new double[][]{vec.real, vec.imag});
    }
    
    @Test
    @JacobiImport("Pure Real 10")
    @JacobiEquals(expected = 1, actual = 1)
    public void testPureImaginary10() { 
        ComplexVector vec = ComplexVector.of(this.input.getRow(0), this.input.getRow(1));        
        this.mockNoPivot().merge(vec, 0, vec.length());
        this.output = Matrices.of(new double[][]{vec.real, vec.imag});
    }
    
    @Test
    @JacobiImport("Complex 16")
    @JacobiEquals(expected = 1, actual = 1)
    public void testComplex16() { 
        ComplexVector vec = ComplexVector.of(this.input.getRow(0), this.input.getRow(1));        
        this.mockNoPivot().merge(vec, 0, vec.length());
        this.output = Matrices.of(new double[][]{vec.real, vec.imag});
    }
    
    @Test
    @JacobiImport("Complex 6")
    @JacobiEquals(expected = 1, actual = 1)
    public void testComplex6UsingPivot3() {
        ComplexVector vec = ComplexVector.of(this.input.getRow(0), this.input.getRow(1));        
        new CooleyTukeyRadix2().merge(vec, 0, vec.length());
        Arrays.stream(vec.real).forEach(System.out::println);
        Arrays.stream(vec.imag).forEach(System.out::println);
        this.output = Matrices.of(new double[][]{vec.real, vec.imag});
    }
    
    @Test
    @JacobiImport("Complex 8")
    @JacobiEquals(expected = 1, actual = 1)
    public void testComplex8UsingPivot2() {
        ComplexVector vec = ComplexVector.of(this.input.getRow(0), this.input.getRow(1));        
        new CooleyTukeyRadix2(){

            @Override
            protected ComplexVector select(int len) {
                return super.select(2);
            }
            
        }.merge(vec, 0, vec.length());
        this.output = Matrices.of(new double[][]{vec.real, vec.imag});
    }
    
    @Test
    @JacobiImport("Complex 24")
    @JacobiEquals(expected = 1, actual = 1)
    public void testComplex24UsingPivot4() {
        ComplexVector vec = ComplexVector.of(this.input.getRow(0), this.input.getRow(1));        
        new CooleyTukeyRadix2(){

            @Override
            protected ComplexVector select(int len) {
                return super.select(4);
            }
            
        }.merge(vec, 0, vec.length());
        this.output = Matrices.of(new double[][]{vec.real, vec.imag});
    }
    
    @Test
    @JacobiImport("Complex 24")
    @JacobiEquals(expected = 1, actual = 1)
    public void testComplex24UsingPivot6() {
        ComplexVector vec = ComplexVector.of(this.input.getRow(0), this.input.getRow(1));        
        new CooleyTukeyRadix2().merge(vec, 0, vec.length());
        this.output = Matrices.of(new double[][]{vec.real, vec.imag});
    }

    @Test
    public void testSelectingTheBestPivotForArcLength1To128() {
        ComplexVector[] pivots = IntStream.range(0, 7)
                .mapToObj(n -> ComplexVector.of(new double[n], new double[n]))
                .toArray(n -> new ComplexVector[n]);
        // no special value for 3/5
        pivots[3] = pivots[1];
        pivots[5] = pivots[1];
        CooleyTukeyRadix2 fft = new CooleyTukeyRadix2(pivots);
        for(int i = 1; i < 129; i++){
            ComplexVector pivot = fft.select(i);
            Assert.assertEquals("Pivot " + pivot.length() + " for " + i + " is not correct.", 0, i % pivot.length());
            for(ComplexVector piv : pivots){
                if(piv.length() > pivot.length()){
                    Assert.assertTrue("Pivot " + pivot.length() + " for " + i + " is not optimal: "
                            + piv.length() + " is better.", i % piv.length() != 0);
                }
            }
        }
    }
    
    @Test
    public void testPivotValues() {
        CooleyTukeyRadix2 fft = new CooleyTukeyRadix2();
        for(int i = 1; i < 129; i++){
            ComplexVector pivot = fft.select(i);
            for(int k = 0; k < pivot.length(); k++){
                Assert.assertEquals(Math.cos(k * Math.PI / pivot.length()), pivot.real[k], 1e-12);
                Assert.assertEquals(-Math.sin(k * Math.PI / pivot.length()), pivot.imag[k], 1e-12);
                
                double val = 4 * pivot.real[k] * pivot.real[k];
                Assert.assertTrue(Math.abs(val) - val < 1e-16);
                val = 4 * pivot.imag[k] * pivot.imag[k];
                Assert.assertTrue(Math.abs(val) - val < 1e-16);
            }
        }
    }

    protected CooleyTukeyRadix2 mockNoPivot() {
        return new CooleyTukeyRadix2(IntStream.range(0, 7)
                .mapToObj(n -> ComplexVector.of(new double[]{1.0}, new double[]{0.0}))
                .toArray(n -> new ComplexVector[n])
        );
    }
    
}