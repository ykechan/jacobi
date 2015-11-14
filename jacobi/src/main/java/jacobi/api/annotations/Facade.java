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
package jacobi.api.annotations;

import jacobi.api.Matrix;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotated interface is a pure facade.
 * 
 * In this context, "Facade" refers to a interface that does not have an explicit
 * implementation. Instead, a proxy should be created in which each method is 
 * invoked and handled by the class annotated by @Implementation.
 * 
 * See also <link>jacobi.api.annotations.Implementation</link>.
 * 
 * @author Y.K. Chan
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Facade {
    
    /**
     * @return  Class of argument this facade takes.
     */
    public Class<?> value() default Matrix.class;
    
}
