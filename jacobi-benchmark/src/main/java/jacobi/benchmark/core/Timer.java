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

/**
 *
 * @author Y.K. Chan
 */
public class Timer {

    public Timer() {
        this.elapsed = 0L;
        this.counter = 0L;
    }
    
    public void used(long time) {
        if(time >= 0){
            this.elapsed += time;
            this.counter++;
        }
    }
    
    public void reset() {
        this.elapsed = 0L;
        this.counter = 0L;
    }
    
    public long getElapsed() {
        return this.elapsed;
    }
    
    public long getCounter() {
        return this.counter;
    }

    private long elapsed, counter;
}
