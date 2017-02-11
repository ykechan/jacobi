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

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.api.annotations.Facade;
import jacobi.api.annotations.Implementation;
import jacobi.api.annotations.Immutate;
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
        Functor func = new Functor(method, implClass);
        if(facade.value() != Matrix.class
        || (!method.isAnnotationPresent(Immutate.class)
         && !method.getDeclaringClass().isAssignableFrom(Immutate.class))){
            return func;
        }
        return implClass.isAnnotationPresent(Immutate.class)
            || func.getMethod().isAnnotationPresent(Immutate.class)
            ? (target, args) -> 
                func.invoke(target, args)
            : (target, args) -> 
                func.invoke( Matrices.copy((Matrix) target), args)
            ;
    }
    
    private Map<Method, Invocator> invocators;
    
    private static final FacadeEngine INSTANCE = new FacadeEngine();
}
