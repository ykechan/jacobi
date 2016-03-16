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
import jacobi.core.util.Throw;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * A functor is a wrapper of an implementation class for invoking facade method.
 * 
 * In this context, an implementation class is a functor, if this class
 *  - has a no-arg constructor
 *  - has at least one public method
 *  - a public method that has the signature of facade argument followed by 
 *    facade method argument
 *  - no other public method has that same signature
 *  - the return type should be the same as the facade method, or same as the 
 *    facade argument for chaining. (In case of chaining, the facade method 
 *    must have the same facade interface as return type.)
 * 
 * The name of the method is not important.
 * 
 * For example, for the following facade
 * 
 * @Facade(String.class)
 * public interface Foo {
 * 
 *     @Implementation(Bar.class)
 *     public double bar(int i);
 * 
 * }
 * 
 * The implementation class Bar, being a functor, may looks like
 * 
 * public Bar {
 * 
 *     public double compute(String str, int i) {
 *         return Math.PI * str.length() + i;
 *     }
 * 
 * }
 * 
 * Additional private, protected or public method is allowed. 
 * 
 * However, the following is not allowed
 * 
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
 * 
 * Both methods have the same signature, thus which is the real implementation
 * is ambiguous.
 * 
 * Implementation classes are encouraged to be stateless and immutable since
 * the instance maybe used in a multi-threaded environment.
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
        Object[] implArgs = new Object[1 + args.length];
        
        implArgs[0] = target;
        System.arraycopy(args, 0, implArgs, 1, args.length);
        
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
            .filter((m) -> m.getDeclaringClass() != Object.class)
            .filter((m) -> m.getReturnType() == facadeMethod.getReturnType()
                        || m.getReturnType() == facade.value() )
            .filter((m) -> this.argumentMatches(m, facade.value(), facadeMethod.getParameterTypes())) 
            .reduce((a, b) -> {
                throw new UnsupportedOperationException(
                    "Ambiguous implementation method "
                    + implClass.getName() + "::" + a.getName()
                    + " and "
                    + implClass.getName() + "::" + b.getName()
                );
            })
            .orElseThrow(() -> 
                new UnsupportedOperationException("No implementation method found.")
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
    
    private Method method;
    private Object inst;
}
