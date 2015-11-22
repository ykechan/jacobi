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
import java.util.Calendar;
import java.util.Date;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 *
 * @author Y.K. Chan
 */
public class FunctorTest {

    @Rule
    public ExpectedException expected;

    public FunctorTest() {
        this.expected = ExpectedException.none();
    }
    
    @Test
    public void testNormal() throws Exception {
        Invocator func = new Functor(TestInterface.class.getMethod("doSth", int.class),
            DoSthImpl.class);
        String str = "This is a string.";
        int num = 17;
        Object result = func.invoke(str, new Object[]{num});
        Assert.assertTrue(result instanceof Integer);
        Assert.assertEquals(str.length() + 17, ((Integer) result).intValue());
    }
    
    @Test
    public void testNotFacade() throws Exception {
        this.expected.expect(RuntimeException.class);
        Invocator func = new Functor(NotAFacade.class.getMethod("doSth", int.class),
            DoSthImpl.class);
    }
    
    @Test
    public void testAmbiguousImpl() throws Exception {
        this.expected.expect(RuntimeException.class);
        Invocator func = new Functor(NotAFacade.class.getMethod("doSth", int.class),
            ConfusedImpl.class);
    }
    
    @Test
    public void testNoImpl() throws Exception {
        this.expected.expect(RuntimeException.class);
        Invocator func = new Functor(NotAFacade.class.getMethod("doSth", int.class),
            NoImpl.class);
    }
    
    @Test
    public void testInvalidFacadeArg() throws Exception {
        Invocator func = new Functor(TestInterface.class.getMethod("doSth", int.class),
            DoSthImpl.class);
        Date date = Calendar.getInstance().getTime();
        int num = 17;
        this.expected.expect(RuntimeException.class);
        Object result = func.invoke(date, new Object[]{num});
    }
    
    @Test
    public void testInvalidMethodArg() throws Exception {
        Invocator func = new Functor(TestInterface.class.getMethod("doSth", int.class),
            DoSthImpl.class);
        String str = "this is a string";
        double num = 17;
        this.expected.expect(RuntimeException.class);
        Object result = func.invoke(str, new Object[]{num});
    }
    
    @Test
    public void testWithoutNoArgConstructor() throws Exception {
        this.expected.expect(RuntimeException.class);
        Invocator func = new Functor(TestInterface.class.getMethod("doSth", int.class),
            CantCreateImpl.class);
    }
 
    @Facade(String.class)
    public interface TestInterface {

        @Implementation(DoSthImpl.class)
        public int doSth(int i);
        
    }
    
    public interface NotAFacade {
        
        @Implementation(DoSthImpl.class)
        public int doSth(int i);
        
    }
    
    public static class DoSthImpl {
        
        public int implOfDoSth(String str, int i) {
            return str.length() + i;
        }

        private int privateMethodIgnored(String str, int i) {
            return -1;
        }
    }
    
    public static class ConfusedImpl {
        
        public int canUseThis(String str, int i) {
            return str.length() + i;
        }
        
        public int orUseThis(String str, int i) {
            return str.length() - i;
        }

    }
    
    public static class NoImpl {
        
        public Object cantUseThis(String str, int i) {
            return str.length() + i;
        }

    }
    
    public static class CantCreateImpl {
        
        public CantCreateImpl(int key) {
            throw new UnsupportedOperationException("Cant create me");
        }
        
        public int canUseThis(String str, int value) {
            return 10;
        }

    }
}
