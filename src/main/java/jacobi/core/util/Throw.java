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
package jacobi.core.util;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * Utility class for preconditions checking.
 * @author Y.K. Chan
 */
public final class Throw {
    
    private Throw() { 
    }
    
    /**
     * Get an instance of Throw.
     * @return An instance of Throw
     */
    public static Throw when() {
        return INSTANCE;
    }
    
    /**
     * Throw exception if the result of a given lambda evaluates to null.
     * @param supplier  given lambda expression
     * @param message   lambda expression for getting error message
     * @return This
     * @throws IllegalArgumentException  if expression evaluates to null
     */
    public Throw isNull(Supplier<?> supplier, Supplier<String> message) {
        if(supplier.get() == null){
            throw new IllegalArgumentException(message.get());
        }
        return this;
    }
    
    /**
     * Throw exception if the result of a given lambda evaluates to true.
     * @param cond      given lambda expression
     * @param message   lambda expression for getting error message
     * @return This
     * @throws IllegalArgumentException  if expression evaluates to true
     */
    public Throw isTrue(BooleanSupplier cond, Supplier<String> message) {
        if(cond.getAsBoolean()){
            throw new IllegalArgumentException(message.get());
        }
        return this;
    }
    
    /**
     * Throw exception if the result of a given lambda evaluates to false.
     * @param cond      given lambda expression
     * @param message   lambda expression for getting error message
     * @return This
     * @throws IllegalArgumentException  if expression evaluates to false
     */
    public Throw isFalse(BooleanSupplier cond, Supplier<String> message) {
        if(!cond.getAsBoolean()){ 
            throw new IllegalArgumentException(message.get());
        }
        return this;
    }
    
    
    /**
     * Singleton instance. 
     */
    private static final Throw INSTANCE = new Throw();
}
