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

import jacobi.api.Matrix;
import jacobi.api.annotations.Facade;
import jacobi.api.annotations.Implementation;
import jacobi.api.annotations.NonPerturbative;
import jacobi.core.impl.CopyOnWriteMatrix;
import jacobi.core.impl.ImmutableMatrix;
import jacobi.core.util.Throw;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Detect and invoke the implementation class given a facade method.
 * 
 * This class will cache the implementation class with correct invocator
 * so duplicate searching of implementation method for multiple invocation
 * of the same facade method is avoided.
 * 
 * @author Y.K. Chan
 */
public class FacadeEngine {

    /**
     * Get singleton instance.
     * @return  Instance of facade engine.
     */
    public static FacadeEngine getInstance() {
        return INSTANCE;
    }

    /**
     * Construct a new Facade engine.
     */
    public FacadeEngine() {
        this.invocators = Collections.synchronizedMap(new HashMap<>());
    }
        
    /**
     * Clear the cached methods.
     * Cleanup is advised for using in a application container.
     */
    public void clearCache() {
        this.invocators.clear();
    }
    
    /**
     * Invoke a facade method.
     * @param method  Facade method
     * @param target  Facade argument
     * @param args  Facade method argument(s)
     * @return   Method result returned.
     */
    public Object invoke(Method method, Object target, Object[] args) {
        Invocator invocator = this.invocators.get(method);
        if(invocator == null){
            // getting invocator multiple times yields the same result
            // whole class synchronization is omitted
            Facade facade = method.getDeclaringClass().getAnnotation(Facade.class);
            invocator = this.createInvocator(facade, method);
            this.invocators.put(method, invocator);
        }
        return invocator.invoke(target, args);
    }
    
    /**
     * Create invocator for a facade method.
     * @param facade  Facade annotation
     * @param method  Facade method.
     * @return   New instance of proper invocator
     * @throws IllegalArgumentException 
     *             if no Implementation annotation
     */
    private Invocator createInvocator(Facade facade, Method method) {
        Throw.when()
            .isFalse(
                () -> method.isAnnotationPresent(Implementation.class),
                () -> method.getDeclaringClass().getName()
                    + "::"
                    + method.getName()
                    + " is not implemented."
            );
        Class<?> implClass = method.getAnnotation(Implementation.class).value();
        Invocator func = new Functor(method, implClass);
        if(facade.value() != Matrix.class
        || (!method.isAnnotationPresent(NonPerturbative.class)
         && !method.getDeclaringClass().isAssignableFrom(NonPerturbative.class))){
            return func;
        }
        return implClass.isAssignableFrom(NonPerturbative.class)
            ? (target, args) -> 
                func.invoke(new ImmutableMatrix((Matrix) target), args)
            : (target, args) -> 
                func.invoke(new CopyOnWriteMatrix((Matrix) target), args)
            ;
    }
    
    private Map<Method, Invocator> invocators;
    
    private static final FacadeEngine INSTANCE = new FacadeEngine();
}
