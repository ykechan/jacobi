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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * Annotated method is implemented by given class.
 * An implementation class must:
 * 1. Have a no-argument constructor.
 * 2. Have a unique public method that takes the Facade's argument 
 *    as first parameter, and the rest of the annotated method as argument.
 * 3. The method return type either matches the annotated method, or if the
 *    annotated method returns the same Facade as fluent interface, matches
 *    the Facade argument type.
 * 
 * @author Y.K. Chan
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Implementation {
    
    /**
     * 
     * @return  Implementation class
     */
    public Class<?> value();
    
}
