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
    protected PaddingPlan(Supplier<Buffer> supplier, List<Consumer<Buffer>> list) {
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
            if (at < 0 || at > this.currentLength) {
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
            return new PaddingPlan(() -> new Buffer(prep, max), this.funcs);
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
