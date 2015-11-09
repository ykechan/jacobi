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
package jacobi.core.util;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 *
 * @author Edwin
 */
public final class Throw {
    
    private Throw() {        
    }
    
    public static Throw when() {
        return INSTANCE;
    }
    
    public Throw isNull(Supplier<?> supplier, Supplier<String> message) {
        if(supplier.get() == null){
            throw new IllegalArgumentException(message.get());
        }
        return this;
    }
    
    public Throw isTrue(BooleanSupplier cond, Supplier<String> message) {
        if(cond.getAsBoolean()){
            throw new IllegalArgumentException(message.get());
        }
        return this;
    }
    
    public Throw isFalse(BooleanSupplier cond, Supplier<String> message) {
        if(!cond.getAsBoolean()){
            throw new IllegalArgumentException(message.get());
        }
        return this;
    }
    
    private static final Throw INSTANCE = new Throw();
}
