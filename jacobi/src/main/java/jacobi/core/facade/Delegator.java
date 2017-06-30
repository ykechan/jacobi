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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * A delegator is an method wrapped as an Invocator.
 * 
 * @author Y.K. Chan
 */
public class Delegator implements Invocator {

    /**
     * Constructor.
     * @param method  Method to be invoked
     */
    public Delegator(Method method) {
        this.method = method;
    }

    /**
     * Get the method to be invoked.
     * @return  Method object
     */
    public Method getMethod() {
        return method;
    }
    
    /**
     * Get the return type of the method.
     * @return  Class of return type
     */
    public Class<?> getReturnType() {
        return method.getReturnType();
    }

    @Override
    public Object invoke(Object target, Object[] args) {
        try {
            return this.method.invoke(target, args);
        } catch (IllegalAccessException | IllegalArgumentException ex) {
            throw new UnsupportedOperationException(ex);
        } catch (InvocationTargetException ex) {
            if(ex.getTargetException() instanceof RuntimeException){
                throw (RuntimeException) ex.getTargetException();
            }
            throw new UnsupportedOperationException(ex.getTargetException()); // NOPMD - intended
        }
    }

    private Method method;
}
