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
package jacobi.core.facade;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.api.annotations.Facade;
import jacobi.api.annotations.Pure;
import jacobi.api.annotations.Implementation;

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 *
 * @author Y.K. Chan
 */
public class FacadeEngineTest {

    @Rule
    public ExpectedException expected;
    
    public FacadeEngineTest() {
        this.expected = ExpectedException.none();
    }
    
    @Before
    public void setUp() {
        DoSthImpl.COUNT.set(0);
        DoSthElseImpl.COUNT.set(0);
    }
    
    @Test
    public void testNormal() throws Exception {
        
        String str = "123";
        FacadeEngine engine = new FacadeEngine();
        Assert.assertEquals(
                "123?17",
                engine.invoke(DoSomethingWithString.class.getMethod("doSth", int.class),
                str,
                new Object[]{17}));
        Assert.assertEquals(
                "123?18",
                engine.invoke(DoSomethingWithString.class.getMethod("doSth", int.class),
                str,
                new Object[]{18}));
        Assert.assertEquals(
                "123?19",
                engine.invoke(DoSomethingWithString.class.getMethod("doSth", int.class),
                str,
                new Object[]{19}));
        
        Assert.assertEquals(1, DoSthImpl.COUNT.get());
        
        Assert.assertEquals(
                123.0,
                (double) engine.invoke(DoSomethingWithString.class.getMethod("doSthElse"),
                "123",
                new Object[]{}), 0.0);
        
        Assert.assertEquals(
                123.0,
                (double)engine.invoke(DoSomethingWithString.class.getMethod("doSthElse"),
                "123.0",
                new Object[]{}), 0.0);
        
        Assert.assertEquals(1, DoSthImpl.COUNT.get());
        Assert.assertEquals(1, DoSthElseImpl.COUNT.get());
        
        Assert.assertEquals(
                "123?20",
                engine.invoke(DoSomethingWithString.class.getMethod("doSth", int.class),
                str,
                new Object[]{20}));
        
        Assert.assertEquals(1, DoSthImpl.COUNT.get());
        Assert.assertEquals(1, DoSthElseImpl.COUNT.get());
        
        Assert.assertEquals(
                Optional.of(Math.PI),
                engine.invoke(DoSomethingWithString.class.getMethod("doSthMayYieldDouble"), str, new Object[]{} )
        );
        
        Assert.assertEquals(
                Optional.of((long) str.length()),
                engine.invoke(DoSomethingWithString.class.getMethod("doSthMayYieldLong"),
                str, new Object[]{} ));
    }
        
    @Test
    public void testNoMethod() throws Exception {
        FacadeEngine engine = FacadeEngine.getInstance();
        this.expected.expect(RuntimeException.class);
        engine.invoke(null, this, new Object[]{});
    }
    
    @Test
    public void testNoArgs() throws Exception {
        FacadeEngine engine = FacadeEngine.getInstance();
        this.expected.expect(RuntimeException.class);
        engine.invoke(DoSomethingWithString.class.getMethod("doSth", int.class),
                "123",
                null);
    }
    
    @Test
    public void testNoImpl() throws Exception {
        FacadeEngine engine = new FacadeEngine();
        this.expected.expect(RuntimeException.class);
        engine.invoke(DoSomethingWithString.class.getMethod("dunnoHowToDo"),
                "123",
                null);
    }
    
    @Facade(String.class)
    public interface DoSomethingWithString {
        
        @Implementation(DoSthImpl.class)
        public String doSth(int i);
        
        @Implementation(DoSthElseImpl.class)
        public double doSthElse();
        
        @Implementation(DoSthMayYieldDouble.class)
        public Optional<Double> doSthMayYieldDouble();
        
        @Implementation(DoSthMayYieldLong.class)
        public Optional<Long> doSthMayYieldLong();
        
        public Date dunnoHowToDo();
    }
    
    public static class DoSthImpl {

        public DoSthImpl() {
            this(100);
        }
        
        // extra constructor allowed
        public DoSthImpl(int i) { 
            COUNT.incrementAndGet();
        }
        
        public String compute(String str, int i) {
            return str + "?" + i;
        }
        
        public static final AtomicInteger COUNT = new AtomicInteger(0);
    }
    
    public static class DoSthElseImpl {

        public DoSthElseImpl() {
            COUNT.incrementAndGet();
        }
        
        public double whatShouldIDo(String str) {
            return Double.parseDouble(str);
        }
        
        public static final AtomicInteger COUNT = new AtomicInteger(0);
    }
    
    public static class DoSthMayYieldDouble {
        
        public Optional<Double> itIsAPie(String str) {        
            return Optional.of(Math.PI);
        }
        
    }
    
    public static class DoSthMayYieldLong {
        
        public Optional<Long> itIsLength(String str) {        
            return Optional.of(Long.valueOf(str.length()));
        }
        
    }
    
    /*
    The Facade engine centers around Matrix. One key feature is support Pure
    annotation, which specifies this facade method does not mutates the values of
    the underlying matrix, or specifies this implementation class operates without
    changing the value. 
    */
    
    @Pure
    @Facade
    public interface FindFromMatrix {
        
        @Implementation(ImmutateImpl.class)
        public int findLies();
        
        @Implementation(MutatingImpl.class)
        public int findHonest();
        
    }
    
    @Facade
    public interface DeriveFromMatrix {
        
        @Pure
        @Implementation(ImmutateImpl.class)
        public String deriveLies();
        
        @Pure
        @Implementation(MutatingImpl.class)
        public String deriveHonest();
        
    }    
    
    @Pure
    public static class ImmutateImpl {
        
        public int find(Matrix matrix) {
            // i lied
            matrix.setRow(0, new double[matrix.getColCount()]);
            return matrix.getRowCount();
        }
        
        public String derive(Matrix matrix) {
            // you can't stop me
            matrix.setRow(0, new double[matrix.getColCount()]);
            return String.valueOf(matrix.getRowCount());
        }
        
    }
        
    public static class MutatingImpl {
        
        public int find(Matrix matrix) {
            // at least I'm honest            
            matrix.setRow(0, new double[matrix.getColCount()]);
            return matrix.getRowCount();
        }
        
        public String derive(Matrix matrix) {
            // at least I'm honest
            matrix.setRow(0, new double[matrix.getColCount()]);
            return String.valueOf(matrix.getRowCount());
        }
        
    }
    
    @Test
    public void testImmutateFacade() throws Exception {
        FacadeEngine engine = FacadeEngine.getInstance();
        double[] array = {Math.sqrt(2.0), Math.PI, Math.E};
        Matrix matrix = Matrices.of(new double[][]{array});
        
        engine.invoke(FindFromMatrix.class.getMethod("findLies"), 
                matrix, 
                new Object[0]);
        
        Assert.assertArrayEquals(matrix.getRow(0), new double[array.length], 1e-16);
        
        matrix.setRow(0, array);
        
        Assert.assertArrayEquals(matrix.getRow(0), new double[]{Math.sqrt(2.0), Math.PI, Math.E}, 1e-16);
        
        engine.invoke(FindFromMatrix.class.getMethod("findHonest"), 
                matrix, 
                new Object[0]);
        
        Assert.assertArrayEquals(matrix.getRow(0), new double[]{Math.sqrt(2.0), Math.PI, Math.E}, 1e-16);
        
        matrix.setRow(0, array);
        Assert.assertArrayEquals(matrix.getRow(0), new double[]{Math.sqrt(2.0), Math.PI, Math.E}, 1e-16);
        
        engine.invoke(DeriveFromMatrix.class.getMethod("deriveLies"), 
                matrix, 
                new Object[0]);
        
        Assert.assertArrayEquals(matrix.getRow(0), new double[array.length], 1e-16);
        
        matrix.setRow(0, array);
        
        Assert.assertArrayEquals(matrix.getRow(0), new double[]{Math.sqrt(2.0), Math.PI, Math.E}, 1e-16);
        
        engine.invoke(DeriveFromMatrix.class.getMethod("deriveHonest"), 
                matrix, 
                new Object[0]);
        
        Assert.assertArrayEquals(matrix.getRow(0), new double[]{Math.sqrt(2.0), Math.PI, Math.E}, 1e-16);
    }
    
}
