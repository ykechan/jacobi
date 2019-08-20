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
import jacobi.core.util.Throw;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;

/**
 * A functor is a wrapper of an implementation class for invoking facade method.
 * 
 * <p>In this context, an implementation class is a functor, if this class</p>
 * 
 * <ul>
 *  <li>has a no-arg constructor</li>
 *  <li>has at least one public method</li>
 *  <li>a public method that has the signature of facade argument followed by 
 *    facade method argument</li>
 *  <li>no other public method has that same signature</li>
 *  <li>the return type should be the same as the facade method, or same as the 
 *    facade argument for chaining. (In case of chaining, the facade method 
 *    must have the same facade interface as return type.)</li>
 * </ul>
 * 
 * <p>The name of the method is not important.</p>
 * 
 * <p>For example, for the following facade</p>
 * 
 * <pre>
 * {@literal @}Facade(String.class)
 * public interface Foo {
 * 
 *     {@literal @}Implementation(Bar.class)
 *     public double bar(int i);
 * 
 * }
 * </pre>
 * 
 * <p>The implementation class Bar, being a functor, may looks like</p>
 * 
 * <pre>
 * public Bar {
 * 
 *     public double compute(String str, int i) {
 *         return Math.PI * str.length() + i;
 *     }
 * 
 * }
 * </pre>
 * 
 * <p>Additional private, protected or public method is allowed. </p>
 * 
 * <p>However, the following is not allowed</p>
 * 
 * <pre>
 * public Bar {
 * 
 *     public double doFoo(String str, int i) {
 *         return Math.PI * str.length() + i;
 *     }
 * 
 *     public double doBarz(String str, int i) {
 *         return Math.E * str.length() / i;
 *     }
 * 
 * }
 * </pre>
 * 
 * <p>Both methods have the same signature, thus which is the real implementation
 * is ambiguous.</p>
 * 
 * <p>Implementation classes are encouraged to be stateless and immutable since
 * the instance maybe used in a multi-threaded environment.</p>
 * 
 * @author Y.K. Chan
 */
public class Functor implements Invocator {
    
    /**
     * Construct a functor.
     * @param facadeMethod  Facade method
     * @param implClass   Java class of implementation
     */
    public Functor(Method facadeMethod, Class<?> implClass) {
        Throw.when()
            .isNull(() -> facadeMethod, () -> "No facade method.")
            .isNull(() -> implClass, () -> "No implementation.")
            .isFalse(
                () -> facadeMethod.getDeclaringClass().isAnnotationPresent(Facade.class),
                () -> "Method " + facadeMethod.getName() + " is not declared by a facade.");
        
        Facade facade = facadeMethod
                .getDeclaringClass()
                .getAnnotation(Facade.class);
        
        this.method = this.findImpl(implClass, facade, facadeMethod);
        this.inst = this.newInstance(implClass);
    }

    /**
     * Get the actual method to be invoked.
     * @return  Actual method
     */
    public Method getMethod() {
        return method;
    }

    @Override
    public Object invoke(Object target, Object[] args) {
        Object[] implArgs = new Object[1 + ((args == null) ? 0 : args.length)];
        
        implArgs[0] = target;
        if(args != null){
            System.arraycopy(args, 0, implArgs, 1, args.length);
        }
        
        try {
            return this.method.invoke(this.inst, implArgs);
        } catch (IllegalAccessException ex) {
            throw new UnsupportedOperationException(ex);
        } catch (InvocationTargetException ex) {
            if(ex.getTargetException() instanceof RuntimeException){
                throw (RuntimeException) ex.getTargetException();
            }
            throw new UnsupportedOperationException(ex.getTargetException()); // NOPMD
        }
    }
    
    /**
     * Construct a new instance of implementation class using no-arg constructor.
     * @param implClass  Implementation class
     * @return   New instance of implementation class
     * @throws   UnsupportedOperationException if failed
     */
    private Object newInstance(Class<?> implClass) {
        try {
            return implClass.newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new UnsupportedOperationException("Unable to new a " + implClass.getName(), ex);
        }
    }
    
    /**
     * Find implementation method that matches the facade method prototype.
     * @param implClass  Implementation class
     * @param facade  Facade annotation
     * @param facadeMethod  Facade method.
     * @return Implementation method.
     * @throws UnsupportedOperationException  
     *          if no method or multiple methods are found.
     */
    private Method findImpl(Class<?> implClass, Facade facade, Method facadeMethod) {
        return Arrays.asList(implClass.getMethods())
            .stream()
            .filter(m -> m.getDeclaringClass() != Object.class)
            .filter(m -> this.returnTypeMatches(m, facade, facadeMethod))
            .filter(m -> this.argumentMatches(m, facade.value(), facadeMethod.getParameterTypes())) 
            .reduce((a, b) -> {
                throw new UnsupportedOperationException(
                    "Ambiguous implementation method "
                    + implClass.getName() + "::" + a.getName()
                    + " and "
                    + implClass.getName() + "::" + b.getName()
                );
            })
            .orElseThrow(() -> 
                new UnsupportedOperationException("No implementation method found for " 
                		+ facadeMethod.getName())
            );
    }
    
    /**
     * Check if argument matches with facade arguments.
     * @param method  Candidate implementation method
     * @param facadeArg  Facade argument
     * @param argList  Facade method argument
     * @return   true if matches, false otherwise
     */
    private boolean argumentMatches(Method method, Class<?> facadeArg, Class[] argList) {
        if(method.getParameterCount() != 1 + argList.length
        || method.getParameterTypes()[0] != facadeArg){
            return false;
        }
        for(int i = 0; i < argList.length; i++){
            if(method.getParameterTypes()[i + 1] != argList[i]){
                return false;
            }
        }
        return true;
    }
    
    /**
     * Check if the return type of input method matches with the return type of facade method. 
     * @param method  Input method
     * @param facade  Facade annotation
     * @param facadeMethod  Facade method
     * @return  true if matches, false otherwise
     */
    private boolean returnTypeMatches(Method method, Facade facade, Method facadeMethod) {
    	if(method.getReturnType() == facadeMethod.getReturnType()) {
    		return true;
    	}
    	
    	if(method.getReturnType() == facade.value()){ 
    		Class<?> retType = facadeMethod.getReturnType();
    		return retType.isAnnotationPresent(Facade.class)
    			&& retType.getDeclaredAnnotation(Facade.class).value() == facade.value();
    	}
    	
    	return false;
    }
    
    private Method method;
    private Object inst;
}
