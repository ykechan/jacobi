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
import java.util.function.Supplier;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 *
 * @author Y.K. Chan
 */
public class FacadeProxyTest {
    
    @Rule
    public ExpectedException expected;

    public FacadeProxyTest() {
        this.expected = ExpectedException.none();
    }
    
    @Test
    public void testNormalFluent() {
        TestFluentInterface facade = FacadeProxy.of(TestFluentInterface.class, "I am a string");
        Assert.assertEquals(new DoSthImpl().compute("I am a string", 0), facade.doSth(0).get());
    }
    
    @Test
    public void testFluentWrongSupplier() {
        this.expected.expect(RuntimeException.class);
        TestFluentWrongSupplier facade = FacadeProxy.of(TestFluentWrongSupplier.class, "I am a string");
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

    public static class DoSthImpl {
        
        public String compute(String str, int i) {
            return "String = " + str + ", i = " + i;
        }
        
    }
}
