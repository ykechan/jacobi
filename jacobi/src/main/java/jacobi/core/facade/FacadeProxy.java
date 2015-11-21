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
import java.lang.reflect.Proxy;

/**
 * Invocation handler for creating Java proxy object.
 * 
 * @author Y.K. Chan
 */
public class FacadeProxy implements InvocationHandler {
    
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
                () -> "Invalid argument " + target + ".");
        
        InvocationHandler handler = new FacadeProxy(FacadeEngine.getInstance(), target);
        Class<?> proxyClass = Proxy.getProxyClass(facadeClass.getClassLoader(), new Class[] { facadeClass });
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

    public FacadeProxy(FacadeEngine engine, Object facadeArg) {
        this.facadeArg = facadeArg;
        this.engine = engine;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        return this.engine.invoke(method, facadeArg, args);
    }
    
    private Object facadeArg;
    private FacadeEngine engine;
}
