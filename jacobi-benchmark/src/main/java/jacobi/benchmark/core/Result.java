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

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author Y.K. Chan
 */
public class Result {
    
    public Result() {
        this.hash = Double.doubleToRawLongBits(Math.random());
        this.entries = new TreeMap<>();
    }
    
    public void accept(Object object) {
        this.hash = (this.hash << 8) ^ ((object == null)? 0 : object.hashCode());
    }
    
    public void record(String name, int problem, Timer timer) {
        Map<String, Entry> map = this.entries.get(problem);
        if(map == null){
            map = new TreeMap<>();
            this.entries.put(problem, map);
        }
        map.put(name, new Entry(timer));
    }
    
    public Set<Integer> keySet() {
        return this.entries.keySet();
    }
    
    public Map<String, Entry> getEntries(int key) {
        return Collections.unmodifiableMap(this.entries.get(key));
    }

    private long hash;
    private Map<Integer, Map<String, Entry>> entries;
    
    public static class Entry {
        
        public Entry(Timer timer) {
            this.mean = timer.getElapsed() / (double) timer.getCounter() / 1000.0 / 1000.0;
        }

        public double getMean() {
            return mean;
        }
        
        private double mean;
    }
}
