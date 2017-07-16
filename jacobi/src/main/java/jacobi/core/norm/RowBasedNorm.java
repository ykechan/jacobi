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

package jacobi.core.norm;

import jacobi.api.Matrix;
import jacobi.core.util.MapReducer;
import java.util.function.DoubleBinaryOperator;
import java.util.function.ToDoubleFunction;
import java.util.stream.DoubleStream;

/**
 * 
 * @author Y.K. Chan
 */
public class RowBasedNorm { 
    
    /**
     * Max norm, aka 1-norm.
     */
    public static final class Max extends RowBasedNorm {

        /**
         * Constructor.
         */
        public Max() {
            super((r) -> DoubleStream.of(r).map((e) -> Math.abs(e)).max().orElse(0.0), (a, b) -> Math.max(a, b));
        }
        
    }
    
    /**
     * Frobenius norm, aka L2,2 norm.
     */
    public static final class Frobenius extends RowBasedNorm {

        /**
         * Constructor.
         */
        public Frobenius() {
            super((r) -> DoubleStream.of(r).map((e) -> e * e).sum(), (a, b) -> a + b);
        }

        @Override
        public double compute(Matrix matrix) {
            return Math.abs(super.compute(matrix));
        }
        
    }
    
    /**
     * L-Infinite norm.
     */
    public static final class LInf extends RowBasedNorm {

        /**
         * Constructor.
         */
        public LInf() {
            super((r) -> DoubleStream.of(r).map((e) -> Math.abs(e)).sum(), (a, b) -> Math.max(a, b));
        }
        
    }    

    /**
     * Constructor.
     * @param mapper Mapper function from a row vector to a value
     * @param reducer  Reducer function of values from row vectors.
     */
    public RowBasedNorm(ToDoubleFunction<double[]> mapper, DoubleBinaryOperator reducer) {
        this.mapper = mapper;
        this.reducer = reducer;
    }
    
    public double compute(Matrix matrix) {
        return this.serial(matrix, 0, matrix.getRowCount());
    }
    
    protected double parallel(Matrix matrix, int numFlops) {
        return MapReducer.of(0, matrix.getRowCount())
                .flop(numFlops)
                .map((begin, end) -> this.serial(matrix, begin, end))
                .reduce((a, b) -> this.reducer.applyAsDouble(a, b))
                .get();
    }
    
    protected double serial(Matrix matrix, int begin, int end) {
        if(end <= begin){
            return 0.0;
        }
        double ans = this.mapper.applyAsDouble(matrix.getRow(begin));
        for(int i = begin + 1; i < end; i++){
            ans = this.reducer.applyAsDouble(ans, this.mapper.applyAsDouble(matrix.getRow(i)));
        }
        return ans;
    }
    
    private ToDoubleFunction<double[]> mapper;
    private DoubleBinaryOperator reducer;
}
