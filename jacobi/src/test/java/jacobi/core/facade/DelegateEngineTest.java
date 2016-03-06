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

package jacobi.core.facade;

import jacobi.api.annotations.Delegate;
import jacobi.api.annotations.Facade;
import jacobi.api.annotations.Implementation;
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
    }
    
    public static class NonFinalString {

        public NonFinalString(String string) {
            this.string = string;
        }        

        @Override
        public String toString() {
            return this.string;
        }
        
        private String string;
    }        
    
    @Facade(NonFinalString.class)
    public interface StringFacade {
        
        @Implementation(DoSomethingImpl.class)
        public NonFinalString doSth(NonFinalString str);
        
        @Implementation(DoSomethingImpl.class)
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
}
