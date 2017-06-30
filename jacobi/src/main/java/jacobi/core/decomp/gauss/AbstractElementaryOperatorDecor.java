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
package jacobi.core.decomp.gauss;

import jacobi.api.Matrix;
import jacobi.core.util.Throw;

/**
 * Decorator for interface ElementaryOperator.
 * 
 * @author Y.K. Chan
 */
public abstract class AbstractElementaryOperatorDecor implements ElementaryOperator {

    /**
     * Constructor.
     * @param op  Base operator to be decorated
     */
    public AbstractElementaryOperatorDecor(ElementaryOperator op) {
        Throw.when().isNull(() -> op, () -> "No base operator.");
        this.op = op;
    }

    @Override
    public void swapRows(int i, int j) {
        this.isValidRow(i).isValidRow(j).op.swapRows(i, j);
    }

    @Override
    public void rowOp(int i, double a, int j) {
        this.isValidRow(i).isValidRow(j).op.rowOp(i, a, j);
    }

    @Override
    public Matrix getMatrix() {
        return this.op.getMatrix();
    }
    
    /**
     * Validate if an integer is valid row index.
     * @param i  Row index
     * @return This 
     * @throws IllegalArgumentException if i is not valid
     */
    protected AbstractElementaryOperatorDecor isValidRow(int i) {
        Throw.when()
                .isTrue(
                    () -> i < 0 || i >= this.getMatrix().getRowCount(),
                    () -> "Invalid row index " + i 
                            + " for a " 
                            + this.getMatrix().getRowCount() 
                            + "x" 
                            + this.getMatrix().getColCount() 
                            + " matrix.");
        return this;
    }

    protected final ElementaryOperator op;
}
