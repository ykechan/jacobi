/*
 * Copyright (C) 2015 Y.K. Chan
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
package jacobi.core.facade;

import jacobi.api.annotations.Facade;
import jacobi.api.annotations.Implementation;
import java.util.Date;
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
    }
        
    @Test
    public void testNoMethod() throws Exception {
        FacadeEngine engine = new FacadeEngine();
        this.expected.expect(RuntimeException.class);
        engine.invoke(null, this, new Object[]{});
    }
    
    @Test
    public void testNoArgs() throws Exception {
        FacadeEngine engine = new FacadeEngine();
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
}
