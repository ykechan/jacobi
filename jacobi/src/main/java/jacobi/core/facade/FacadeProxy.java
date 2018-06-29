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
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.function.Supplier;

/**
 * Invocation handler for creating Java proxy object.
 * 
 * @author Y.K. Chan
 */
public class FacadeProxy implements InvocationHandler {
    
    /**
     * Create a proxy implementation of a facade interface, which would return
     * the argument value for Supplier interfaces.
     * This would use the common FacadeEngine
     * @param <T>  Facade interface type
     * @param facadeClass  Facade interface class
     * @param target  Facade interface argument
     * @return Proxy object.
     */
    public static <T> T of(Class<T> facadeClass, Object target) {
        return FacadeProxy.of(facadeClass, target, target);
    }
    
    /**
     * Create a proxy implementation of a facade interface.
     * This would use the common FacadeEngine
     * @param <T>  Facade interface type
     * @param facadeClass  Facade interface class
     * @param target  Facade interface argument
     * @param returnValue  Returned object for supplier interface
     * @return Proxy object.
     */
    public static <T> T of(Class<T> facadeClass, Object target, Object returnValue) {
        Throw.when()
           .isNull(() -> facadeClass, () -> "No facade.")
           .isNull(() -> target, () -> "No facade argument.")
           .isFalse(
                () -> facadeClass.isAnnotationPresent(Facade.class),
                () -> facadeClass.getName() + " is not a facade."
           )
           .isFalse(
                () -> facadeClass.getAnnotation(Facade.class).value().isInstance(target),
                () -> "Invalid argument " + target + ".")
           .isFalse(
                () -> isCorrectSupplierType(facadeClass),
                () -> "Invalid supplier type argument.");
        InvocationHandler handler = new FacadeProxy(
                DelegateEngine.getInstance(), 
                facadeClass, 
                target, 
                returnValue
        );
        return newProxy(facadeClass, handler);
    }

    /**
     * Construct a new Facade proxy.
     * @param engine  Facade engine for invocation.
     * @param facadeClass  Facade interface class
     * @param facadeArg   Facade argument
     */
    public FacadeProxy(FacadeEngine engine, Class<?> facadeClass, Object facadeArg) {
        this(engine, facadeClass, facadeArg, facadeArg);
    }
    
    /**
     * Construct a new Facade proxy.
     * @param engine  Facade engine for invocation.
     * @param facadeClass  Facade interface class
     * @param facadeArg   Facade argument
     * @param returnValue   Return value for Supplier get method
     */
    public FacadeProxy(FacadeEngine engine, Class<?> facadeClass, Object facadeArg, Object returnValue) {
        this.facadeClass = facadeClass;
        this.facadeArg = facadeArg;
        this.engine = engine;
        this.returnValue = returnValue;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        if(method.getDeclaringClass() == Supplier.class
        && GET_METHOD.equals(method.getName())
        && method.getParameterCount() == 0){
            return this.returnValue;
        }
        Object result = this.engine.invoke(method, facadeArg, args);
        if(method.getReturnType() == this.facadeClass){
            if(!this.facadeClass.getAnnotation(Facade.class).value().isInstance(result)){
                throw new UnsupportedOperationException("Unable to create chain with result " + result);
            }
            InvocationHandler handler = new FacadeProxy(engine, facadeClass, result,
                    result == facadeArg ? returnValue : result);
            return newProxy(this.facadeClass, handler);
        }
        return result;
    }
    
    /**
     * Check if the facade interface extends Supplier, the return type of Supplier
     * is the same as the facade argument.
     * @param facadeClazz  Facade interface class
     * @return  True if correct, false otherwise
     */
    private static boolean isCorrectSupplierType(Class<?> facadeClazz) {
        if(!Supplier.class.isAssignableFrom(facadeClazz)){
            return true;
        }
        Facade facade = facadeClazz.getAnnotation(Facade.class);
        return Arrays.stream(facadeClazz.getGenericInterfaces())
                .filter((t) -> t instanceof ParameterizedType)
                .map((t) -> ParameterizedType.class.cast(t))
                .filter((t) -> t.getRawType() == Supplier.class)
                .map((t) -> t.getActualTypeArguments()[0])
                .filter((t) -> t == facade.value())
                .findAny()
                .isPresent();
    }

    /**
     * Create new proxy object with given invocation handler.
     * @param <T>  Proxy interface
     * @param clazz  Proxy interface class
     * @param handler  Invocation handler
     * @return   Proxy
     */
    private static <T> T newProxy(Class<T> clazz, InvocationHandler handler) {
        Class<?> proxyClass = Proxy.getProxyClass(clazz.getClassLoader(), new Class[] { clazz });
        try {
            return (T) proxyClass
                    .getConstructor(new Class[] { InvocationHandler.class })
                    .newInstance(new Object[] { handler });
        } catch (NoSuchMethodException
               | SecurityException
               | InstantiationException
               | IllegalAccessException ex) {
            throw new UnsupportedOperationException(ex);
        } catch (InvocationTargetException ex) {
            if(ex.getTargetException() instanceof RuntimeException){
                throw (RuntimeException) ex.getTargetException();
            }
            throw new UnsupportedOperationException(ex.getTargetException()); // NOPMD
        }
    }
    
    private Class<?> facadeClass;
    private Object facadeArg, returnValue;
    private FacadeEngine engine;
    
    private static final String GET_METHOD = Supplier.class.getMethods()[0].getName();
}
