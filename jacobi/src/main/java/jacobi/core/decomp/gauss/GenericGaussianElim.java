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
package jacobi.core.decomp.gauss;

import jacobi.api.Matrix;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * Perform Gaussian Elimination with given elementary operator.
 * 
 * @author Y.K. Chan
 */
public class GenericGaussianElim {
    
    /**
     * Perform Gaussian Elimination.
     * @param matrix   Matrix to be performed
     */
    public void compute(Matrix matrix) {
        this.compute(matrix, Function.identity());
    }
    
    /**
     * Perform Gaussian Elimination with potential listener
     * @param <T>  Type after decorating
     * @param matrix  Matrix to be performed
     * @param decor  Decorating function
     * @return  Elementary operator after elimination
     */
    public <T extends ElementaryOperator> T compute(Matrix matrix, Function<ElementaryOperator, T> decor) {
        T oper = decor.apply(new FrontElementEliminator(matrix));
        int end = Math.min(
                matrix.getRowCount(), 
                matrix.getColCount()) - 1;
        int nextPivot = -1;
        for(int i = 0; i < end; i++){
            if(nextPivot < 0){
                nextPivot = this.findPivot(matrix, i);
            }
            nextPivot = this.eliminate(oper, i, nextPivot);
        }
        return oper;
    }
    
    /**
     * Eliminate the frontal element of a row given a pivot row.
     * @param op  Elementary operator
     * @param from  Start row index of interest
     * @param pivotIndex  Pivot row index
     * @return   Next pivot row index, or negative if not computed.
     */
    protected int eliminate(ElementaryOperator op, int from, int pivotIndex) {
        op.swapRows(from, pivotIndex);
        double pivot = op.getMatrix().get(from, from);        
        if(Math.abs(pivot) < EPILSON){
            return -1;
        }
        double next = op.getMatrix().get(from, from + 1);
        return this.serial(op, from, pivot, next);
    }
    
    /**
     * Eliminate the frontal element of a row given a pivot row in serial.
     * @param op  Elementary operator
     * @param from  Start and pivot row index of interest
     * @param pivot  Pivot row frontal element
     * @param next  Pivot row column element after frontal
     * @return    Next pivot row index
     */
    protected int serial(ElementaryOperator op, int from, double pivot, double next) {
        int maxIndex = -1;
        double maxElem = 0.0;
        
        for(int i = from + 1; i < op.getMatrix().getRowCount(); i++){
            double elem = this.eliminateFront(op, from, i, pivot, next);
            if(elem > maxElem){
                maxElem = elem;
                maxIndex = i;
            }
        }
        return maxIndex;
    }
    
    /**
     * Eliminate the frontal element of a row given a pivot row in parallel by stream.
     * @param op  Elementary operator
     * @param from  Start and pivot row index of interest
     * @param pivot  Pivot row frontal element
     * @param next  Pivot row column element after frontal
     * @return    Next pivot row index
     */
    protected int stream(ElementaryOperator op, int from, double pivot, double next) {
        return IntStream.range(from + 1, op.getMatrix().getRowCount())
                .mapToObj((i) -> new Element(i, this.eliminateFront(op, from, i, pivot, next)))
                .reduce((a, b) -> a.getValue() > b.getValue() ? a : b)
                .map((e) -> e.getIndex())
                .get();
    }
    
    private double eliminateFront(ElementaryOperator op, int from, int target, double pivot, double next) {
        double factor = op.getMatrix().get(target, from) / pivot;
        double front = op.getMatrix().get(target, from + 1) - factor * next;
        op.rowOp(target, -factor, from);
        return Math.abs(front);
    }
    
    private int findPivot(Matrix matrix, int from) {
        int maxIndex = from;
        double maxElem = Math.abs(matrix.get(from, from));
        for(int i = from + 1; i < matrix.getRowCount(); i++){
            double elem = Math.abs(matrix.get(i, from));
            if(elem > maxElem){
                maxIndex = i;
                maxElem = elem;
            }
        }
        return maxIndex;
    }
    
    private static class Element {

        public Element(int index, double value) {
            this.index = index;
            this.value = value;
        }

        public int getIndex() {
            return index;
        }

        public double getValue() {
            return value;
        }
        
        private int index;
        private double value;
    }
    
    private static class FrontElementEliminator implements ElementaryOperator {

        public FrontElementEliminator(Matrix matrix) {
            this.matrix = matrix;
        }

        @Override
        public void swapRows(int i, int j) {
            this.matrix.swapRow(i, j);
        }

        @Override
        public void rowOp(int i, double a, int j) {
            double[] r0 = this.matrix.getRow(i);
            double[] r1 = this.matrix.getRow(j);
            r0[j] = 0.0;
            for(int k = j + 1; k < r0.length; k++){
                r0[k] += a * r1[k];
            }
            this.matrix.setRow(i, r0);
        }

        @Override
        public Matrix getMatrix() {
            return this.matrix;
        }
        
        private Matrix matrix;
    }
    
    private static final double EPILSON = 1e-10;
}
