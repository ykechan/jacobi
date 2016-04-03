/*
 * Copyright (C) 2016 Y.K. Chan
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
 * only for this child-class of parameter.<p>
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
