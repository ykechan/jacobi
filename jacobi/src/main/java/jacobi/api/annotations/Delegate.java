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
package jacobi.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate a delegate method. 
 * 
 * <p>A delegate method is a special implementation 
 * for certain facade method, that is usually more efficient than general
 * implementation specified by the Implementation annotation, but applicable
 * only for this child-class of parameter.</p>
 * 
 * <p>The facade engine should match by annotated facade interface and method
 * name, and find the method with the same signature in case of overloading.</p>
 * 
 * <p>It is advised that though it may be tempting to implement all
 * trivial operations, this may turn the matrix implementation into an God 
 * object. Moreover, some silly cases with no actual practical usage,
 * such as finding determinant of an identity matrix, are not recommended to be
 * implemented.</p>
 * 
 * @author Y.K. Chan
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Delegate {
    
    /**
     * Facade interface of the delegating method
     * @return  Facade interface
     */
    public Class<?> facade();
    
    /**
     * Method name of the delegating method
     * @return  Method name
     */
    public String method();
    
}
