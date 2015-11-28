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

package jacobi.benchmark.core;

import java.util.function.Function;
import java.util.function.IntFunction;

/**
 *
 * @author Y.K. Chan
 */
public class Suite<U, V> {

    public Suite(IntFunction<U> supplier, Function<U, V> consumer) {
        this.supplier = supplier;
        this.consumer = consumer;
    }
    
    public V run(int size, Timer timer) {
        U input = this.supplier.apply(size);
        long t0 = System.nanoTime();
        try {
            return this.consumer.apply(input);
        } finally {
            timer.used(System.nanoTime() - t0);
        }
    }
    
    private IntFunction<U> supplier;
    private Function<U, V> consumer;
}
