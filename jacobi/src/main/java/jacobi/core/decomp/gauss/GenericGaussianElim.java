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

package jacobi.core.decomp.gauss;

import jacobi.api.Matrix;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * Perform Gaussian Elimination with given elementary operator.
 * 
 * @author Y.K. Chan
 * @param <T>
 */
public class GenericGaussianElim<T extends ElementaryOperator> {
    
    /**
     * Constructor.
     * @param matrix  Matrix to be transformed to upper triangular
     * @param decors  Decorators to elementary operator
     */
    public GenericGaussianElim(Matrix matrix, Function<ElementaryOperator, T> decors) {
        this.oper = decors.apply(new FrontElementEliminator(matrix));
    }
    
    /**
     * Perform Gaussian Elimination.
     * @param arg  Dummy argument to avoid being an entry point
     * @return  Elementary operator after elimination
     */
    public T compute(Void arg) {
        int end = Math.min(
                this.oper.getMatrix().getRowCount(), 
                this.oper.getMatrix().getColCount()) - 1;
        int nextPivot = -1;
        for(int i = 0; i < end; i++){
            if(nextPivot < 0){
                nextPivot = this.findPivot(this.oper.getMatrix(), i);
            }
            nextPivot = this.eliminate(oper, i, nextPivot);
        }
        return this.oper;
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
    
    private T oper;
    
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
