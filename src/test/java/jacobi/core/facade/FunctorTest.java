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

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import jacobi.api.annotations.Facade;
import jacobi.api.annotations.Implementation;

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
        Assert.assertEquals(DoSthImpl.class.getMethod("implOfDoSth", String.class, int.class)
                , ((Functor) func).getMethod());
        Assert.assertEquals(str.length() + 17, ((Integer) result).intValue());
    }
    
    @Test
    public void shouldBeAbleToUseSingletonEnumAsImpl() throws Exception {
        Invocator func = new Functor(TestInterface.class.getMethod("doSthElse", int.class),
            EnumImpl.class);
        String str = "This is a string.";
        int num = 17;
        Object result = func.invoke(str, new Object[]{num});
        Assert.assertTrue(result instanceof Integer);
        Assert.assertEquals(EnumImpl.class.getMethod("apply", String.class, int.class)
                , ((Functor) func).getMethod());
        Assert.assertEquals(100 * str.length() + 17, ((Integer) result).intValue());
    }
    
    @Test
    public void testNotFacade() throws Exception {
        this.expected.expect(RuntimeException.class);
        Invocator func = new Functor(NotAFacade.class.getMethod("doSth", int.class),
            DoSthImpl.class);
    }
    
    @Test
    public void testAmbiguousImpl() throws Exception {
        this.expected.expect(UnsupportedOperationException.class);
        Invocator func = new Functor(TestInterface.class.getMethod("doSth", int.class),
            ConfusedImpl.class);
    }
    
    @Test
    public void testNoImpl() throws Exception {
        this.expected.expect(RuntimeException.class);
        Invocator func = new Functor(TestInterface.class.getMethod("doSth", int.class),
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
    
    @Test(expected = UnsupportedOperationException.class)
    public void testWithExceptionThrown() throws Exception {
        Invocator func = new Functor(TestInterface.class.getMethod("doSth", int.class),
            CantUseThis.class);
        func.invoke("", new Object[]{0});
    }
    
    @Test
    public void testNoImplMethod() throws Exception {
        Invocator func = new Functor(TestInterface.class.getMethod("doSth", int.class),
            CantUseThis.class);
    }
    
    @Test
    public void testGenericInterface() throws Exception {
    	Invocator func = new Functor(GenericFacade.class.getMethod("firstOf", List.class),
    			FirstOf.class);
    	Date target = new SimpleDateFormat("yyyy-MM-dd").parse("1991-07-10");
    	
    	Object result = func.invoke("", new Object[] {Collections.singletonList(target)});
    	Assert.assertTrue(result instanceof Optional);
    	Assert.assertTrue(target == ((Optional<?>) result).get());
    }
    
    @Test(expected = ClassCastException.class)
    public void testGenericInterfaceWithWrongGenericReturnType() throws Exception {
    	Invocator func = new Functor(GenericFacade.class.getMethod("firstOf", List.class),
    			WrongGeneric.class);
    	Date target = new SimpleDateFormat("yyyy-MM-dd").parse("1991-07-10");
    	
    	Object result = func.invoke("", new Object[] {Collections.singletonList(target)});
    	Assert.assertTrue(result instanceof Optional);
    	Date date = ((Optional<Date>) result).get();
    }
    
    @Test
    public void testGenericInterfaceWithCorrectGenericReturnType() throws Exception {
    	Invocator func = new Functor(GenericFacade.class.getMethod("firstOf", List.class),
    			WrongGeneric.class);
    	String target = "1991-07-10";
    	
    	Object result = func.invoke(target, new Object[] {Collections.singletonList(target)});
    	Assert.assertTrue(result instanceof Optional);
    	String date = ((Optional<String>) result).get();
    	
    	Assert.assertEquals("1991-07-10", date);
    }
    
    @Test
    public void testGenericInterfaceWithMultipleOptionalReturnType() throws Exception {
    	Invocator func = new Functor(GenericOptionalOfDifferentTypes.class.getMethod("pipe"),
    			EchoAndToDate.class);
    	String target = "1991-07-10";
    	
    	Object result = func.invoke(target, new Object[]{});
    	Assert.assertTrue(result instanceof Optional);
    	String date = ((Optional<String>) result).get();
    	
    	Assert.assertEquals("1991-07-10", date);
    }
    
    @Facade(String.class)
    public interface TestInterface {

        @Implementation(DoSthImpl.class)
        public int doSth(int i);
        
        @Implementation(EnumImpl.class)
        public int doSthElse(int i);
        
    }
    
    public interface NotAFacade {
        
        @Implementation(DoSthImpl.class)
        public int doSth(int i);
        
    }
    
    @Facade(String.class)
    public interface GenericFacade {
    	
    	@Implementation(FirstOf.class)
    	public <T> Optional<T> firstOf(List<T> list);
    	
    }
    
    public static class DoSthImpl {
        
        public int implOfDoSth(String str, int i) {
            return str.length() + i;
        }

        private int privateMethodIgnored(String str, int i) {
            return -1;
        }
    }
    
    public enum EnumImpl {
    	
    	INSTANCE;
    	
    	public int apply(String str, int i) {
    		return 100 * str.length() + i;
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
        
        public int cantUseThis(String str) {
            return str.length() + 0;
        }
        
        public int cantUseThis(Object str, double value) {
            return str.hashCode() + (int) value;
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
    
    public static class CantUseThis {
        
        public int cantUseThis(String str, int value) throws IOException {
            throw new IOException();
        }

    }    
    
    public static class FirstOf {
    	
    	public <T> Optional<T> first(String str, List<T> list) {
    		return list == null || list.isEmpty() 
    			? Optional.empty() 
    			: Optional.of(list.get(0));
    	}
    	
    }
    
    public static class WrongGeneric {
    	
    	public <T> Optional<String> first(String str, List<T> list) {
    		return Optional.of(str);
    	}
    	
    }
    
    @Facade(String.class)
    public interface GenericOptionalOfDifferentTypes {
    	
    	public Optional<String> pipe();
    	
    	public Optional<Date> parse();
    	
    }
    
    public static class EchoAndToDate {
    	
    	public Optional<String> echo(String str) {
    		return Optional.of(str);
    	}
    	
    	public Optional<Date> toDate(String str) {
    		try {
				return Optional.of(new SimpleDateFormat("yyyy-MM-dd").parse(str));
			} catch (ParseException e) {
				throw new UnsupportedOperationException(e);
			}
    	}
    	
    }
    
}
