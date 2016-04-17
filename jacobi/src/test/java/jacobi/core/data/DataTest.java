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

package jacobi.core.data;

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
    public void test() {
        for(Method m : ConcreteSupplier.class.getMethods()){
            System.out.println(m.getDeclaringClass() + "::" + m.getName() 
                    + "("
                    + Arrays.asList(m.getParameterTypes())
                    + ") : "
                    + m.getReturnType());
        }
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
