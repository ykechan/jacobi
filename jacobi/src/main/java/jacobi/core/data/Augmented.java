/*
 * Copyright (C) 2016 Y.K. Chan
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

package jacobi.core.data;

import jacobi.api.Matrix;
import jacobi.api.annotations.Delegate;
import jacobi.api.annotations.NonPerturbative;
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
    
    @NonPerturbative
    public static class Append {
        
        public Data compute(Matrix matrix, Function<List<Double>, Double> func) {
            return new Augmented(matrix).append(func);
        }
        
    }
    
    @NonPerturbative
    public static class Prepend {
        
        public Data compute(Matrix matrix, Function<List<Double>, Double> func) {
            return new Augmented(matrix).prepend(func);
        }
        
    }
    
    @NonPerturbative
    public static class Insert {
        
        public Data compute(Matrix matrix, int at, Function<List<Double>, Double> func) {
            return new Augmented(matrix).insert(at, func);
        }
        
    }
    
    @NonPerturbative
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
    @NonPerturbative
    @Delegate(facade = Data.class, method = "append")
    public Data append(Function<List<Double>, Double> func) {
        return new Augmented(this, this.builder.copy().append(func));
    }

    @Override
    @NonPerturbative
    @Delegate(facade = Data.class, method = "prepend")
    public Data prepend(Function<List<Double>, Double> func) {
        return new Augmented(this, this.builder.copy().prepend(func));
    }
    
    @Override
    @NonPerturbative
    @Delegate(facade = Data.class, method = "insert")
    public Data insert(int at, Function<List<Double>, Double> func) {
        return new Augmented(this, this.builder.copy().insert(at, func));
    }

    @Override
    @NonPerturbative
    @Delegate(facade = Data.class, method = "select")
    public Data select(int... cols) {
        return new Augmented(this, this.builder.copy().select(cols));
    }

    @NonPerturbative
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
