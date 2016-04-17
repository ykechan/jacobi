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

import jacobi.core.util.Throw;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Determines the maximum number of element and number of element to pre-allocate
 * for a series of appending, prepending, insertion and extraction.
 * 
 * This class is specifically designed to perform vector transformation with
 * the said operations in linear time. Outside usage is not advised.
 * 
 * @author Y.K. Chan
 */
class PaddingPlan {
    
    /**
     * Create a Builder for this class.
     * @param  initLength  Initial length of the vector
     * @return  Builder for padding.
     */
    public static Builder builder(int initLength) {
        return new Builder(initLength);
    }

    /**
     * Constructor. Intended to be use by Builder. If you want to instantiate
     * this class, use Builder instead.
     * @param supplier
     * @param list 
     */
    protected PaddingPlan(int resultLength, Supplier<Buffer> supplier, List<Consumer<Buffer>> list) {
        this.resultLength = resultLength;
        this.supplier = supplier;
        this.list = list;
    }    
    
    /**
     * Create a buffer for padding.
     * @return   Buffer for padding.
     */
    public Buffer createBuffer() {
        return this.supplier.get();
    }
    
    /**
     * Apply the padding operations on a vector
     * @param buffer  Buffer for operation
     * @param values  Vector values
     * @return  Resultant vector
     */
    public double[] apply(Buffer buffer, double[] values) {
        buffer.fill(values);
        for(Consumer<Buffer> c : list){
            c.accept(buffer);
        }
        return buffer.getArray();
    }
    
    private int resultLength;
    private Supplier<Buffer> supplier;
    private List<Consumer<Buffer>> list;
    
    /**
     * Builder class for PaddingPlan
     */
    public static class Builder {
        
        /**
         * Constructor.
         */
        protected Builder(int initLength) {
            this.maxLength = initLength;
            this.maxPrepend = 0;
            this.currentLength = initLength;
            this.currentPrepend = 0;
            this.funcs = new ArrayList<>();
        }
        
        /**
         * Add a appending operation.
         * @param func  Function to obtain new element
         * @return  This object.
         */
        public Builder append(Function<List<Double>, Double> func) {
            this.currentLength++;
            this.funcs.add((buf) -> buf.insert(Integer.MAX_VALUE, func.apply(buf)) );
            return this.update();
        }
        
        /**
         * Add a prepending operation.
         * @param func  Function to obtain new element
         * @return  This object.
         */
        public Builder prepend(Function<List<Double>, Double> func) {
            this.currentLength++;
            this.currentPrepend++;
            this.funcs.add((buf) -> buf.insert(0, func.apply(buf)) );
            return this.update();
        }
        
        /**
         * Add an insertion operation.
         * @param at  Index of the new element
         * @param func  Function to obtain new element
         * @return  This object.
         */
        public Builder insert(int at, Function<List<Double>, Double> func) {
            if (at < 0 || at >= this.currentLength) {
                throw new IllegalArgumentException("Invalid index " + at);
            }
            this.currentLength++;
            this.funcs.add((buf) -> buf.insert(at, func.apply(buf)) );
            return this.update();
        }
        
        /**
         * Add a selection operation. Only selected columns will remain, others
         * will be discarded.
         * @param cols  Column indices
         * @return  This object.
         */
        @SuppressWarnings("null") // false positive
        public Builder select(int... cols) {
            Throw.when()
                .isTrue(
                    () -> cols == null || cols.length == 0, 
                    () -> "No column to select."
                );
            for(int i : cols){
                Throw.when()
                    .isTrue(
                        () -> i < 0 || i >= this.currentLength, 
                        () -> "Invalid column " + i);
            }
            this.funcs.add((buf) -> buf.select(cols));
            this.maxLength = Math.max(this.maxLength, cols.length + this.maxPrepend);
            this.currentPrepend = 0;
            this.currentLength = cols.length;            
            return this.update();
        }
        
        /**
         * Build the padding plan.
         * @return  A padding plan
         */
        public PaddingPlan build() {
            int prep = this.maxPrepend;
            int max = this.maxLength;
            return new PaddingPlan(this.currentLength, () -> new Buffer(prep, max), this.funcs);
        }
        
        /**
         * Copy the state of this builder and retain the original.
         * @return  New builder
         */
        public Builder copy() {
            Builder builder = new Builder(this.currentLength);
            builder.maxPrepend = this.maxPrepend;
            builder.currentPrepend = this.currentPrepend;
            builder.funcs = new ArrayList<>(this.funcs);
            return builder;
        }
        
        private Builder update() {
            this.maxLength = Math.max(this.maxLength, this.currentLength);
            this.maxPrepend = Math.max(this.maxPrepend, this.currentPrepend);
            return this;
        }
        
        private int maxPrepend, maxLength;
        private int currentPrepend, currentLength;
        private List<Consumer<Buffer>> funcs;
    }

}
