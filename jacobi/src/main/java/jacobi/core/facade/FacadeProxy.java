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
     * Create a proxy implementation of a facade interface.
     * This would use the common FacadeEngine
     * @param <T>  Facade interface type
     * @param facadeClass  Facade interface class
     * @param target  Facade interface argument
     * @return Proxy object.
     */
    public static <T> T of(Class<T> facadeClass, Object target) {
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
        
        InvocationHandler handler = new FacadeProxy(FacadeEngine.getInstance(), facadeClass, target);
        return newProxy(facadeClass, handler);
    }        

    /**
     * Construct a new Facade proxy.
     * @param engine  Facade engine for invocation.
     * @param facadeClass  Facade interface class
     * @param facadeArg   Facade argument
     */
    public FacadeProxy(FacadeEngine engine, Class<?> facadeClass, Object facadeArg) {
        this.facadeClass = facadeClass;
        this.facadeArg = facadeArg;
        this.engine = engine;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        if(GET_METHOD.equals(method.getName())
        && method.getParameterCount() == 0){
            return this.facadeArg;
        }
        Object result = this.engine.invoke(method, facadeArg, args);
        if(method.getReturnType() == this.facadeClass){
            if(!this.facadeClass.getAnnotation(Facade.class).value().isInstance(result)){
                throw new UnsupportedOperationException("Unable to create chain with result " + result);
            }
            InvocationHandler handler = new FacadeProxy(engine, facadeClass, result);
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
    private Object facadeArg;
    private FacadeEngine engine;
    
    private static final String GET_METHOD = Supplier.class.getMethods()[0].getName();
}
