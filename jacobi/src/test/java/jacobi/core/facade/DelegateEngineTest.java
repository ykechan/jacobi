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

import jacobi.api.annotations.Delegate;
import jacobi.api.annotations.Facade;
import jacobi.api.annotations.Immutate;
import jacobi.api.annotations.Implementation;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Supplier;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Y.K. Chan
 */
public class DelegateEngineTest {

    @Test
    public void testNormal() throws Exception { 
        NonFinalString result = (NonFinalString) DelegateEngine.getInstance().invoke(
                StringFacade.class.getMethod("doSth", NonFinalString.class), 
                new NonFinalString("I"), 
                new Object[]{ new NonFinalString("duck") });
        Assert.assertEquals("I did something with duck.", result.toString());
        
        result = (NonFinalString) DelegateEngine.getInstance().invoke(
                StringFacade.class.getMethod("doSth", NonFinalString.class), 
                new StringThatCanDoSth("I"), 
                new Object[]{ new NonFinalString("duck") });
        
        Assert.assertEquals("I did duck easily.", result.toString());
        
        result = (NonFinalString) DelegateEngine.getInstance().invoke(
                StringFacade.class.getMethod("doSth", int.class), 
                new StringThatCanDoSth("You"), 
                new Object[]{ 911 });
        
        Assert.assertEquals("You did 911.", result.toString());
        
        DelegateEngine.getInstance().clearCache();
    }
    
    @Test
    public void testDuplicatedMethodKeys() throws Exception {
        Integer result = (Integer) DelegateEngine.getInstance().invoke(
                SpecificSupplier.class.getMethod("get"), 
                new ConcreteDelegate("I"), 
                new Object[]{});
        Assert.assertEquals(1337, result.intValue());
    }
    
    @Test(expected = RuntimeException.class)
    public void testTargetException() throws Exception {
        AtomicLong count = new AtomicLong(0);
        try {
            DelegateEngine.getInstance().invoke(
                StringFacade.class.getMethod("doSth", AtomicLong.class), 
                new StringFailsToDoSth("I"), 
                new Object[]{count});        
        } finally {
            Assert.assertEquals(1L, count.get());
        }
    }
    
    @Test(expected = RuntimeException.class)
    public void testTargetLoudException() throws Exception {
        AtomicLong count = new AtomicLong(0);
        try {
            DelegateEngine.getInstance().invoke(
                StringFacade.class.getMethod("doSth", AtomicLong.class), 
                new StringFailsToDoSthLoudly("I"), 
                new Object[]{count});        
        } finally {
            Assert.assertEquals(1L, count.get());
        }
    }
    
    @Test(expected = RuntimeException.class)
    public void testInvalidMethod() throws Exception {
        Method method = StringDoSthSecretly.class.getDeclaredMethod("secretMethod");
        Delegator delegate = new Delegator(method);
        Assert.assertEquals(method, delegate.getMethod());
        delegate.invoke(new StringDoSthSecretly(""), new Object[0]);
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void testAmbiguousDelegate() throws Exception {
        DelegateEngine.getInstance().invoke(
                StringFacade.class.getMethod("doSth", double.class), 
                new AmbiguousDelegate("I"), 
                new Object[]{Math.E});        
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testDelegateToANonFacade() throws Exception {
        DelegateEngine.getInstance().invoke(
                StringFacade.class.getMethod("doSth", int.class), 
                new DelegateToNonFacade("I"), 
                new Object[]{ 0 });        
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testDelegateToMismatchFacadeVariable() throws Exception {
        DelegateEngine.getInstance().invoke(
                StringFacade.class.getMethod("doSth", int.class), 
                new DelegateToMatrixFacade("I"), 
                new Object[]{ 0 });        
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testDelegateToNonExistantMethod() throws Exception {
        DelegateEngine.getInstance().invoke(
                StringFacade.class.getMethod("doSth", int.class), 
                new DelegateToNonExistantMethod("I"), 
                new Object[]{ 0 });        
    }
    
    @Test
    public void testMutatingDelegateToImmutateFacade() throws Exception {
        DelegateEngine.getInstance().invoke(
                ImmutateStringFacade.class.getMethod("doSth", String.class), 
                new MutatingDelegate("I"), 
                new Object[]{ " do not mutate." });        
    }
    
    public static class NonFinalString {

        public NonFinalString(String string) {
            this.string = string;
        }        

        @Override
        public String toString() {
            return this.string;
        }
        
        protected String string;
    }        
    
    @Facade(NonFinalString.class)
    public interface StringFacade {
        
        @Implementation(DoSomethingImpl.class)
        public NonFinalString doSth(NonFinalString str);
        
        @Implementation(DoSomethingImpl.class)
        public NonFinalString doSth(int i);
        
        public NonFinalString doSth(AtomicLong i);
        
        public Object doSth(double d);
    }
    
    @Immutate
    @Facade(NonFinalString.class)
    public interface ImmutateStringFacade {
        
        public NonFinalString doSth(String i);
        
    }
    
    public interface NotAFacade {
        
        @Implementation(DoSomethingImpl.class)
        public NonFinalString iNeedImpl(int i);
        
    }
    
    @Facade
    public interface MismatchFacadeVariable {
        
        public NonFinalString doSth(int i);
        
    }
    
    public static class DoSomethingImpl {
        
        public NonFinalString doSth(NonFinalString str, NonFinalString arg) {
            return new NonFinalString(str.toString() + " did something with " + arg + ".");
        }
        
        public NonFinalString doSth(NonFinalString str, int i) {
            return new NonFinalString(str.toString() + " did " + i + ".");
        }
        
    }
    
    public static class StringThatCanDoSth extends NonFinalString {

        public StringThatCanDoSth(String string) {
            super(string);
        }
        
        @Delegate(facade = StringFacade.class, method = "doSth")
        public NonFinalString compute(NonFinalString str) {
            return new NonFinalString(this.toString() + " did " + str.toString() + " easily.");
        }
        
    }
    
    public static class StringFailsToDoSth extends NonFinalString {
        
        public StringFailsToDoSth(String string) {
            super(string);
        }
        
        @Delegate(facade = StringFacade.class, method = "doSth")
        public NonFinalString doSth(AtomicLong num) {
            num.incrementAndGet();
            throw new UnsupportedOperationException("Failed");
        }
        
    }
    
    public static class StringFailsToDoSthLoudly extends NonFinalString {
        
        public StringFailsToDoSthLoudly(String string) {
            super(string);
        }
        
        @Delegate(facade = StringFacade.class, method = "doSth")
        public NonFinalString doSth(AtomicLong num) throws IOException {
            num.incrementAndGet();
            throw new IOException("Failed");
        }
        
    }
    
    public static class StringDoSthSecretly extends NonFinalString {

        public StringDoSthSecretly(String string) {
            super(string);
        }
        
        private NonFinalString secretMethod() {
            return null;
        }
        
    }
    
    /**
     * Java reflection would include two instance of get() method in the following
     * example. One returns object coming from Supplier, and one returns Integer
     * coming from SpecificSupplier. Both leads to the same implementation. 
     * Delegate engine should cater this by choosing the more specific one, by 
     * determining if one return type is the sub-class of the other. If two return
     * types are siblings, though no way in Java to code it explicitly, exception
     * would occur.
     */
    @Facade(NonFinalString.class)
    public interface SpecificSupplier extends Supplier<Integer> {

        @Override
        public Integer get();
        
    }
    
    public static class ConcreteDelegate extends NonFinalString implements SpecificSupplier {

        public ConcreteDelegate(String string) {
            super(string);
        }

        @Override
        @Delegate(facade = SpecificSupplier.class, method = "get")
        public Integer get() {
            return 1337;
        }
        
    }
    
    public static class AmbiguousDelegate extends NonFinalString {
        
        public AmbiguousDelegate(String string) {
            super(string);
        }
        
        @Delegate(facade = StringFacade.class, method = "doSth")
        public String callMe(double i) {
            return "Call Me did " + i + ".";
        }
        
        @Delegate(facade = StringFacade.class, method = "doSth")
        public NonFinalString noCallMe(double i) {
            return new NonFinalString("Call Me did " + i + ".");
        }
    }
    
    public static class DelegateToNonFacade extends NonFinalString {
        
        public DelegateToNonFacade(String string) {
            super(string);
        }
        
        @Delegate(facade = NotAFacade.class, method = "iNeedImpl")
        public NonFinalString dontDelegateToPureInterface(int i) {
            return new NonFinalString("Call Me did " + i + ".");
        }
        
        @Delegate(facade = StringFacade.class, method = "doSth")
        public NonFinalString doSth(int num) throws IOException {
            return new NonFinalString("I can");
        }
    }
    
    public static class DelegateToMatrixFacade extends NonFinalString {
        
        public DelegateToMatrixFacade(String string) {
            super(string);
        }
        
        @Delegate(facade = MismatchFacadeVariable.class, method = "doSth")
        public NonFinalString doSth(int num) throws IOException {
            return new NonFinalString("I fails");
        }
    }
    
    public static class DelegateToNonExistantMethod extends NonFinalString {
        
        public DelegateToNonExistantMethod(String string) {
            super(string);
        }
        
        @Delegate(facade = StringFacade.class, method = "doSth")
        public NonFinalString doSth(Function<?, ?> f) {
            return new NonFinalString("Delegate to what?");
        }
        
        @Delegate(facade = StringFacade.class, method = "doSth")
        public NonFinalString doSth(int i) {
            return new NonFinalString("I fails too.");
        }
    }
    
    public static class MutatingDelegate extends NonFinalString {
        
        public MutatingDelegate(String string) {
            super(string);
        }
        
        @Immutate
        @Delegate(facade = ImmutateStringFacade.class, method = "doSth")
        public NonFinalString doSth(String str) {
            this.string = str;
            return new NonFinalString("I trusted you.");
        }
    }
    
}
