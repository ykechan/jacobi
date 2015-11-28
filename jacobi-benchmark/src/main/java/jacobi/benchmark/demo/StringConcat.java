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

package jacobi.benchmark.demo;

import jacobi.benchmark.core.Benchmark;
import jacobi.benchmark.core.Result;
import jacobi.benchmark.core.Suite;
import jacobi.benchmark.core.Ticker;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *
 * Benchmark of different ways in concatenating a string.
 * This is an example of how to build a simple benchmark.
 * 
 * Each strategy would go through the task of finding the result of concatenating
 * n strings, each in the length of n. The n strings are randomly generated base-64
 * data, so no way of optimizing. The computational result is returned to the 
 * framework for processing to prevent dead-code elimination.
 * 
 * The following approach is under benchmark:
 * - + operator
 * - StringBuffer
 * - StringBuilder
 * 
 * @author Y.K. Chan
 */
public class StringConcat implements Supplier<Result> {
    
    /**
     * Base-64 characters.
     */
    public static final String BASE64 = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_";
    
    /**
     * Function of generating an array of n random strings all in length n.
     */
    public static final IntFunction<String[]> RAND = (n) -> IntStream.generate(() -> n)
            .limit(n)
            .mapToObj((k) -> IntStream.range(0, k)
                .map((i) -> (int) Math.floor(Math.random() * BASE64.length()))
                .mapToObj((i) -> BASE64.charAt(i))
                .collect(() -> new StringBuilder(),
                        (buf, ch) -> buf.append(ch),
                        (left, right) -> left.append(right.toString())) 
                .toString()
            )
            .collect(Collectors.toList())
            .toArray(new String[0]);

    /**
     * Config the benchmark and run and get the result.
     * @return 
     */
    @Override
    public Result get() {
        return Benchmark.builder()
            .setProblemsGenerator(() -> IntStream.iterate(100, (i) -> i + 100))
            .setProblemsLimit(1)
            .setNumberOfWarmups(32)
            .setNumberOfIteration(128)
            .setHeartbeat(new Ticker(System.out))
            .add("By Operator", new Suite<>(RAND, StringConcat::byOperator) )
            .add("By Concat", new Suite<>(RAND, StringConcat::byConcat) )
            .add("By String Buffer", new Suite<>(RAND, StringConcat::byStringBuffer) )
            .add("By String Builder", new Suite<>(RAND, StringConcat::byStringBuilder) )
            .get()
            .run();
    }
    
    private static String byOperator(String[] args) {
        String str = "";
        for(String s : args){
            str += s; // NOPMD - For benchmarking
        }
        return str;
    }
    
    private static String byConcat(String[] args) {
        String str = "";
        for(String s : args){
            str = str.concat(s);
        }
        return str;
    }
    
    private static String byStringBuffer(String[] args) {
        @SuppressWarnings("StringBufferMayBeStringBuilder")
        StringBuffer buf = new StringBuffer(); // NOPMD - For benchmarking
        for(String s : args){
            buf.append(s);
        }
        return buf.toString();
    }
    
    private static String byStringBuilder(String[] args) {
        StringBuilder buf = new StringBuilder(); 
        for(String s : args){
            buf.append(s);
        }
        return buf.toString();
    }
}
