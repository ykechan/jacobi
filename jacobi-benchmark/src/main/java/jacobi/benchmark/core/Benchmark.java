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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *
 * 
 * 
 * @author Y.K. Chan
 */
public class Benchmark {
    
    public static Builder builder() {
        return new Builder(new Benchmark());
    }

    protected Benchmark() {
        this.limit = 1;
        this.skip = 16;
        this.rep = 32;
        this.problems = () -> IntStream.range(0, 1);
        this.suites = new HashMap<>();
        this.heartbeat = (i, j) -> {};
    }
    
    public Result run() {
        Result result = new Result();
        Map<String, Timer> timers = this.suites.keySet() // NOPMD - False Positive
                .stream()  
                .collect(Collectors.toMap(
                    (s) -> s,
                    (s) -> new Timer()));
        List<Map.Entry<String, Suite<?, ?>>> list = new ArrayList<>(suites.entrySet());
        AtomicLong ticker = new AtomicLong(0);
        long total = limit * rep * list.size();
        this.problems.get().limit(limit).sequential().forEach((k) -> { 
            for(int i = 0; i < rep; i++){
                //Collections.shuffle(list);
                if(i == skip){
                    timers.values().forEach((t) -> t.reset());
                }
                int prob = k;
                list.stream().sequential().forEach( (e) -> {
                    e.getValue().run(prob, timers.get(e.getKey()));
                    this.heartbeat.accept(ticker.incrementAndGet(), total);
                });
            }
        });
        this.heartbeat.accept(total, total);
        return result;
    }    

    private int limit, skip, rep;
    private Supplier<IntStream> problems;
    private Map<String, Suite<?, ?>> suites;
    private BiConsumer<Long, Long> heartbeat;
    
    public static class Builder {

        protected Builder(Benchmark benchmark) {
            this.benchmark = benchmark;
        }
        
        public Builder setProblemsLimit(int limit) {
            benchmark.limit = limit;
            return this;
        }
        
        public Builder setProblemsGenerator(Supplier<IntStream> problems) {
            benchmark.problems = problems;
            return this;
        }
        
        public Builder setNumberOfIteration(int number) {
            benchmark.rep = number;
            return this;
        }
        
        public Builder setNumberOfWarmups(int warmups) {
            benchmark.skip = warmups;
            return this;
        }
        
        public Builder setHeartbeat(BiConsumer<Long, Long> func) {
            benchmark.heartbeat = func;
            return this;
        }
        
        public Builder add(String name, Suite<?, ?> suite) {
            benchmark.suites.put(name, suite);
            return this;
        }
        
        public Benchmark get() {            
            return this.benchmark;
        }
        
        private Benchmark benchmark;
    }
}
