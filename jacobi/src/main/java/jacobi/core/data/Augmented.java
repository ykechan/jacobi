/* 
 * The MIT License
 *
 * Copyright 2016 Y.K. Chan
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
package jacobi.core.data;

import jacobi.api.Matrix;
import jacobi.api.annotations.Delegate;
import jacobi.api.annotations.Immutate;
import jacobi.api.ext.Data;
import jacobi.core.impl.DefaultMatrix;
import jacobi.core.impl.ImmutableMatrix;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 *
 * @author Y.K. Chan
 */
public class Augmented extends ImmutableMatrix implements Data {        
    
    @Immutate
    public static class Append {
        
        public Data compute(Matrix matrix, Function<List<Double>, Double> func) {
            return new Augmented(matrix).append(func);
        }
        
    }
    
    @Immutate
    public static class Prepend {
        
        public Data compute(Matrix matrix, Function<List<Double>, Double> func) {
            return new Augmented(matrix).prepend(func);
        }
        
    }
    
    @Immutate
    public static class Insert {
        
        public Data compute(Matrix matrix, int at, Function<List<Double>, Double> func) {
            return new Augmented(matrix).insert(at, func);
        }
        
    }
    
    @Immutate
    public static class Select {
        
        public Data compute(Matrix matrix, int... cols) {
            return new Augmented(matrix).select(cols);
        }
        
    }

    public Augmented(Matrix base) {
        this.base = base;
        this.builder = PaddingPlan.builder(base.getColCount());
    }

    public Augmented(Augmented aug, PaddingPlan.Builder builder) {
        this.base = aug.base;
        this.builder = builder;
    }
    
    @Override
    public int getRowCount() {
        return this.base.getRowCount();
    }

    @Override
    public int getColCount() {
        throw new UnsupportedOperationException("Invalid usage.");
    }

    @Override
    public double[] getRow(int index) {
        throw new UnsupportedOperationException("Invalid usage.");
    }

    @Override
    @Immutate
    @Delegate(facade = Data.class, method = "append")
    public Data append(Function<List<Double>, Double> func) {
        return new Augmented(this, this.builder.copy().append(func));
    }

    @Override
    @Immutate
    @Delegate(facade = Data.class, method = "prepend")
    public Data prepend(Function<List<Double>, Double> func) {
        return new Augmented(this, this.builder.copy().prepend(func));
    }
    
    @Override
    @Immutate
    @Delegate(facade = Data.class, method = "insert")
    public Data insert(int at, Function<List<Double>, Double> func) {
        return new Augmented(this, this.builder.copy().insert(at, func));
    }

    @Override
    @Immutate
    @Delegate(facade = Data.class, method = "select")
    public Data select(int... cols) {
        return new Augmented(this, this.builder.copy().select(cols));
    }

    @Immutate
    @Delegate(facade = Data.class, method = "get")
    public Matrix build() {
        PaddingPlan plan = this.builder.build();
        Buffer buffer = plan.createBuffer();
        List<double[]> rows = new ArrayList<>();
        ((ArrayList<?>) rows).ensureCapacity(this.base.getRowCount());
        for(int i = 0; i < this.base.getRowCount(); i++){
            rows.add(plan.apply(buffer, this.base.getRow(i)));
        }
        return new DefaultMatrix(rows.toArray(new double[rows.size()][]));
    }

    @Override
    public Matrix get() {
        return this.build();
    }

    private Matrix base;
    private PaddingPlan.Builder builder;
}
