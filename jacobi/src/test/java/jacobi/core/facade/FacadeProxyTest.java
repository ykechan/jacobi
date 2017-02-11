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

import jacobi.api.annotations.Facade;
import jacobi.api.annotations.Implementation;
import java.io.Serializable;
import java.util.function.Supplier;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Y.K. Chan
 */
public class FacadeProxyTest {    
    
    @Test
    public void testNormalFluent() {
        TestFluentInterface facade = FacadeProxy.of(TestFluentInterface.class, "I am a string");
        Assert.assertEquals(new DoSthImpl().compute("I am a string", 0), facade.doSth(0).get());
    }
    
    @Test(expected = RuntimeException.class)
    public void testFluentWrongSupplier() {
        TestFluentWrongSupplier facade = FacadeProxy.of(TestFluentWrongSupplier.class, "I am a string");
    }
    
    @Test(expected = RuntimeException.class)
    public void testFluentWrongReturnType() {
        TestFluentWrongReturnType facade = FacadeProxy.of(TestFluentWrongReturnType.class, "I am a string");
        facade.doSth("").get();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testNotAFacade() {
        FacadeProxy.of(Serializable.class, "Unsupported interface.");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testFacadeParameterMismatch() {
        FacadeProxy.of(TestFluentWrongSupplier.class, 1337L);
    }
    
    @Facade(String.class)
    public interface TestFluentInterface extends Supplier<String> {
        
        @Implementation(DoSthImpl.class)
        public TestFluentInterface doSth(int i);
        
    }
    
    @Facade(String.class)
    public interface TestFluentWrongSupplier extends Supplier<Integer> {
        
        @Implementation(DoSthImpl.class)
        public TestFluentInterface doSth(int i);
        
    }
    
    @Facade(String.class)
    public interface TestFluentWrongReturnType extends Supplier<String> {
        
        @Implementation(DoSthImpl.class)
        public TestFluentWrongReturnType doSth(String str);
        
    }

    public static class DoSthImpl {
        
        public String compute(String str, int i) {
            return "String = " + str + ", i = " + i;
        }
        
        public int compute(String a, String b) {
            return a.length() + b.length();
        }
        
    }
}
