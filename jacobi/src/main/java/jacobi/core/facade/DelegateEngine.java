/* 
 * The MIT License
 *
 * Copyright 2016 Y.K. Chan
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
import jacobi.core.util.Throw;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Extension of Facade Engine which supports Delegate annotation.
 * 
 * See also jacobi.api.annotations.Delegate
 * 
 * @author Y.K. Chan
 */
public class DelegateEngine extends FacadeEngine {

    public static DelegateEngine getInstance() {
        return INSTANCE;
    }

    public DelegateEngine() {
        this.delegates = Collections.synchronizedMap(new HashMap<>());
    }

    @Override
    public void clearCache() {
        this.delegates.clear();
        super.clearCache();
    }

    @Override
    public Object invoke(Method method, Object target, Object[] args) {
        if(!this.delegates.containsKey(new Key(target.getClass()))){
            this.addDelegates(target.getClass());
        }
        Invocator invocator = this.delegates.get(new Key(target.getClass(), method));    
        return invocator == null
                ? super.invoke(method, target, args)
                : invocator.invoke(target, args);
    }
    
    /**
     * Add methods annotated with @Delegate and corresponding facade method
     * @param clazz  Concrete class of target object
     */
    private void addDelegates(Class<?> clazz) {        
        this.delegates.putAll(
            Arrays.asList(clazz.getMethods())
                .stream()
                .filter((m) -> m.isAnnotationPresent(Delegate.class))
                .map((m) -> this.toKey(m))
                .filter((k) -> k.isValid())
                .collect(Collectors.toMap(
                    Function.identity(), 
                    (k) -> new Delegator(k.method),
                    (k0, k1) -> {
                        if(k0.getReturnType().isAssignableFrom(k1.getReturnType())){
                            return k1;
                        }
                        if(k1.getReturnType().isAssignableFrom(k0.getReturnType())){
                            return k0;
                        }
                        throw new UnsupportedOperationException(
                            "Unable to resolve between " 
                            + this.toString(k0.getMethod())
                            + " and "
                            + this.toString(k1.getMethod())
                        );
                    }
                ))
        );
        this.delegates.put(new Key(clazz), NULL);
    }
    
    /**
     * Create a key composed by facade interface and facade method from 
     * concrete delegate implementation method.
     * @param concreteMethod  Concrete method
     * @return A key composed by facade interface and facade method
     */
    private Key toKey(Method concreteMethod) {
        Method facadeMethod = this.findFacadeMethod(concreteMethod);
        Facade facade = facadeMethod.getDeclaringClass().getAnnotation(Facade.class);
        Throw.when()
            .isNull(
                () -> facade, 
                () -> "Marked facade " 
                    + facadeMethod.getDeclaringClass().getName()
                    + " is not annotated with @" + Facade.class.getName())
            .isFalse(
                () -> facade.value().isAssignableFrom(concreteMethod.getDeclaringClass()), 
                () -> "Facade parameter " 
                    + facade.value()
                    + " is not assignable from "
                    + concreteMethod.getDeclaringClass() );
        return new Key(concreteMethod.getDeclaringClass(), concreteMethod, facadeMethod);
    }
    
    /**
     * Find corresponding facade method defined in the delegate annotation.
     * @param concreteMethod  Concrete method
     * @return Facade method in facade interface
     */
    private Method findFacadeMethod(Method concreteMethod) {
        Delegate delegate = concreteMethod.getAnnotation(Delegate.class);
        Class<?> facade = delegate.facade();        
        if(!facade.isAnnotationPresent(Facade.class)){
            throw new IllegalArgumentException("Marked facade " 
                    + facade.getName() 
                    + " is not annotated with @Facade.");
        }
        String name = delegate.method();
        try {            
            return facade.getMethod(name, concreteMethod.getParameterTypes());
        } catch (NoSuchMethodException | SecurityException ex) {
            throw new IllegalArgumentException(ex);
        }
    }
    
    private String toString(Method method) {
        return method.getDeclaringClass().getName() 
            + "::"
            + method.getName()
            + '(' + Arrays.asList(method.getParameterTypes()) + ')'
            + " returns "
            + method.getReturnType().getName();
    }
    
    private Map<Key, Invocator> delegates;
    
    private static final Invocator NULL = (target, args) -> { 
        throw new UnsupportedOperationException("Invalid invocator."); 
    };
    
    private static final DelegateEngine INSTANCE = new DelegateEngine();

    private static class Key {
        
        public Key(Class<?> clazz) {
            this(clazz, null);
        }

        public Key(Class<?> clazz, Method facade) {
            this.clazz = clazz;
            this.facade = facade;
        }
        
        public Key(Class<?> clazz, Method method, Method facade) {
            this.clazz = clazz;
            this.method = method;
            this.facade = facade;
        }
        
        public boolean isValid() {
            return !this.facade.isAnnotationPresent(Immutate.class)
                || this.facade.getDeclaringClass().isAnnotationPresent(Immutate.class)
                == this.method.isAnnotationPresent(Immutate.class); 
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof Key){
                Key other = (Key) obj;
                return Objects.equals(this.clazz, other.clazz)
                    && Objects.equals(this.facade, other.facade);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.clazz, this.facade);
        }

        @Override
        public String toString() {
            return (this.facade == null)
                    ? this.clazz.getName()
                    : this.clazz.getName() 
                    + "::" 
                    + this.facade.getName()
                    + "("
                    + Arrays.asList(this.facade.getParameterTypes())
                    + ")";
        }
        
        private Class<?> clazz;
        private Method method, facade;
    }
}
