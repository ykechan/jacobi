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
package jacobi.core.data;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.api.annotations.Delegate;
import jacobi.api.annotations.Immutate;
import jacobi.api.ext.Data;
import jacobi.core.impl.ImmutableMatrix;
import java.util.List;
import java.util.function.Function;

/**
 * Implementation for builder of augmented structure on matrix.
 * 
 * @author Y.K. Chan
 */
public class Augmented extends ImmutableMatrix implements Data {        
    
    /**
     * Append a column to the end of each rows.
     */
    @Immutate
    public static class Append {
        
        /**
         * Get another builder with new column at the end of each rows.
         * @param matrix  Input augmented matrix
         * @param func  Function to compute the value of the new column
         * @return   Augmented with new column at the end of each rows
         */
        public Data compute(Matrix matrix, Function<List<Double>, Double> func) {
            return new Augmented(matrix).append(func);
        }
        
    }
    
    /**
     * Prepend a column to the start of each rows.
     */
    @Immutate
    public static class Prepend {
        
        /**
         * Get another builder with new column at the start of each rows.
         * @param matrix  Input augmented matrix
         * @param func  Function to compute the value of the new column
         * @return   Augmented with new column at the start of each rows
         */
        public Data compute(Matrix matrix, Function<List<Double>, Double> func) {
            return new Augmented(matrix).prepend(func);
        }
        
    }
    
    /**
     * Insert a column to each rows.
     */
    @Immutate
    public static class Insert {
        
        /**
         * Get another builder with new column inserted to each rows.
         * @param matrix  Input augmented matrix
         * @param at  Column index to be inserted
         * @param func  Function to compute the value of the new column
         * @return   Augmented with new column inserted to each rows
         */
        public Data compute(Matrix matrix, int at, Function<List<Double>, Double> func) {
            return new Augmented(matrix).insert(at, func);
        }
        
    }
    
    /**
     * Retain only selected column to each rows.
     */
    @Immutate
    public static class Select {
        
        /**
         * Get another builder with columns selected from each rows.
         * @param matrix  Input augmented matrix
         * @param cols  Column indices to be selected
         * @return  Augmented with columns selected from each rows
         */
        public Data compute(Matrix matrix, int... cols) {
            return new Augmented(matrix).select(cols);
        }
        
    }

    /**
     * Constructor.
     * @param base  Base matrix
     */
    public Augmented(Matrix base) {
        this.base = base;
        this.builder = PaddingPlan.builder(base.getColCount());        
    }

    /**
     * Constructor upon another augmented matrix.
     * @param aug  Augmented matrix
     * @param builder  Builder for padding plan.
     */
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

    /**
     * Append a column to the end of each rows.
     * @param func  Function to compute the value of the new column
     * @return   Augmented with new column at the end of each rows
     */
    @Override
    @Immutate
    @Delegate(facade = Data.class, method = "append")
    public Data append(Function<List<Double>, Double> func) {
        return new Augmented(this, this.builder.copy().append(func));
    }

    /**
     * Prepend a column to the start of each rows.
     * @param func  Function to compute the value of the new column
     * @return  Augmented with new column at the start of each rows
     */
    @Override
    @Immutate
    @Delegate(facade = Data.class, method = "prepend")
    public Data prepend(Function<List<Double>, Double> func) {
        return new Augmented(this, this.builder.copy().prepend(func));
    }
    
    /**
     * Insert a column to each rows.
     * @param at  Column index to be inserted
     * @param func  Function to compute the value of the new column
     * @return  Augmented with new column inserted to each rows
     */
    @Override
    @Immutate
    @Delegate(facade = Data.class, method = "insert")
    public Data insert(int at, Function<List<Double>, Double> func) {
        return new Augmented(this, this.builder.copy().insert(at, func));
    }

    /**
     * Retain only selected column to each rows.
     * @param cols  Column indices
     * @return  Augmented with columns selected from each rows
     */
    @Override
    @Immutate
    @Delegate(facade = Data.class, method = "select")
    public Data select(int... cols) {
        return new Augmented(this, this.builder.copy().select(cols));
    }

    /**
     * Build the augmented matrix.
     * @return  The augmented matrix.
     */
    @Immutate
    @Delegate(facade = Data.class, method = "get")
    public Matrix build() {
        if(this.base.getRowCount() == 0){
            return Matrices.zeros(0);
        }
        PaddingPlan plan = this.builder.build();
        Buffer buffer = plan.createBuffer();
        double[] recon = plan.apply(buffer, this.base.getRow(0));
        Matrix matrix = Matrices.zeros(this.base.getRowCount(), recon.length);
        matrix.setRow(0, recon);
        for(int i = 1; i < this.base.getRowCount(); i++){
            matrix.setRow(i, plan.apply(buffer, this.base.getRow(i)));
        }
        return matrix;
    }

    @Override
    public Matrix get() {
        return this.build();
    }    

    private Matrix base;
    private PaddingPlan.Builder builder;
}
