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
package jacobi.core.data;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.api.ext.Data;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.JacobiJUnit4ClassRunner;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Y.K. Chan
 */
@JacobiImport("/jacobi/test/data/DataTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class DataTest {
    
    @JacobiInject(0)
    public Matrix input;
    
    @JacobiResult(1)
    public Matrix output;
    
    @Test
    @JacobiImport("Quadratic")
    @JacobiEquals(expected = 1, actual = 1)
    public void testRegressQuadratic() {
        this.output = this.input.ext(Data.class)
                .select(0, 1)
                .append((r) -> r.get(0) * r.get(1))
                .append((r) -> r.get(0) * r.get(0))
                .append((r) -> r.get(1) * r.get(1))
                .prepend((r) -> 1.0)
                .get();
    }

    @Test
    @JacobiImport("LogNormal")
    @JacobiEquals(expected = 1, actual = 1)
    public void testLogNormal() {
        Data data = this.input.ext(Data.class);
        for(int i = 0; i < this.input.getColCount(); i++){            
            int target = i;
            data = data.append((r) -> Math.log(r.get(target)));
        }
        this.output = data.select(IntStream.range(input.getColCount(), 2*input.getColCount()).toArray()).get();
    }
    
    @Test
    @JacobiImport("Prepend Bias")
    @JacobiEquals(expected = 1, actual = 1)
    public void testPrependBias() {
        this.output = this.input.ext(Data.class)
                .prepend((r) -> 1.0)
                .get();
    }
    
    @Test
    @JacobiImport("Insert SinX")
    @JacobiEquals(expected = 1, actual = 1)
    public void testInsertSinX() {
        this.output = this.input.ext(Data.class)
                .insert(1, (r) -> Math.sin(r.get(0)))
                .get();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testInsertNegative() {
        Matrices.zeros(5).ext(Data.class).insert(-1, (r) -> 1.0).get();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testInsertIndexOutOfBound() {
        //Matrices.zeros(5).ext(Data.class).insert(5, (r) -> 1.0).get();
        Matrices.zeros(5).ext(Data.class).insert(6, (r) -> 1.0).get();
    }
    
    public interface MatrixSuppler extends Supplier<Matrix> {

        @Override
        public Matrix get();
        
    }
    
    public class ConcreteSupplier implements MatrixSuppler {

        @Override
        public Matrix get() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        
    }
}
