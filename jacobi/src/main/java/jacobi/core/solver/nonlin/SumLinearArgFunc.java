/* 
 * The MIT License
 *
 * Copyright 2019 Y.K. Chan
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
package jacobi.core.solver.nonlin;

import java.util.Arrays;
import java.util.stream.IntStream;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.core.impl.ColumnVector;
import jacobi.core.op.MulT;
import jacobi.core.util.MapReducer;
import jacobi.core.util.Throw;

/**
 * A non-linear vector function f(X) with form specified as follows.
 * 
 * <p>
 * This class refers to a function that takes the form<br>
 * <br>
 * f(X) = Sum{ g<sub>k</sub>(&lt;A<sub>k</sub>,X&gt;) },<br> 
 * where {g<sub>k</sub>} are non-linear scalar functions,<br>
 *       {A<sub>k</sub>} are constant vector values, and<br>
 *       &lt;.,.&gt; is the vector dot product.<br>
 * </p>
 *       
 * <p>
 * The gradient and Hessian matrix of such function takes special form.<br>
 * grad(f) = [df/dX<sup>(i)</sup>] = [Sum{A<sub>k</sub><sup>(i)</sup> g'<sub>k</sub>}]<br>
 * 
 * hess(f) = [d<sup>2</sup>f/dX<sup>(i)</sup>dX<sup>(j)</sup>]
 *         = [ Sum{A<sub>k</sub><sup>(i)</sup> A<sub>k</sub><sup>(j)</sup> g''<sub>k</sub>} ]
 * </p>
 * 
 * 
 * @author Y.K. Chan
 *
 */
public abstract class SumLinearArgFunc<T> implements VectorFunction {
    
    /**
     * Constructor.
     * @param consts  Linear coefficients for variables
     */
    public SumLinearArgFunc(Matrix consts) {
        this(consts, new MulT(), MapReducer.DEFAULT_NUM_FLOP);
    }

    /**
     * Constructor.
     * @param consts  Linear coefficients for variables
     * @param mulT  Transpose multiplication implementation 
     * @param flopThres  Threshold of FLOP count to use parallelism
     */
    protected SumLinearArgFunc(Matrix consts, MulT mulT, int flopThres) {
        this.consts = consts;
        this.mulT = mulT;
        this.flopThres = flopThres;
    }

    @Override
    public double at(double[] pos) {
        Params<T> params = this.prepare(pos);
        double fx = 0.0;
        for(int i = 0; i < params.args.length; i++) {
            fx += this.valueAt(params.inter, i, params.args[i]);
        }
        return fx;
    }

    @Override
    public ColumnVector grad(double[] pos) {
        Params<T> params = this.prepare(pos);
        if(pos.length * this.consts.getRowCount() < this.flopThres){
            return new ColumnVector(this.grad(params, pos, 0, this.consts.getRowCount()));
        }        
        return new ColumnVector(MapReducer.of(0, this.consts.getRowCount())
            .flop(pos.length)
            .map((begin, end) -> this.grad(params, pos, begin, end))
            .reduce(this::merge)
            .get()
        );
    }

    @Override
    public Matrix hess(double[] pos) {
        Params<T> params = this.prepare(pos);
        
        int totalFlop = (pos.length * pos.length * this.consts.getRowCount()) / 2;
        
        Matrix mat = totalFlop > this.flopThres
             ? MapReducer.of(0, this.consts.getRowCount())
                  .flop((pos.length * pos.length) / 2)
                  .map((begin, end) -> this.hess(params, pos, begin, end))
                  .reduce(this::merge)
                  .get()
             : this.hess(params, pos, 0, this.consts.getRowCount());
        
        return this.symmetrize(mat);
    }
    
    /**
     * Compute the sum of gradient components of given range
     * @param params  Argument for input position
     * @param pos  Input position
     * @param begin  Begin index of range
     * @param end  End index of range
     * @return  Sum of gradient components
     */
    protected double[] grad(Params<T> params, double[] pos, int begin, int end) {
        double[] vector = new double[pos.length];
        for(int i = begin; i < end; i++) {
            double dx = this.slopeAt(params.inter, i, params.args[i]);
            double[] row = this.consts.getRow(i);
            for(int j = 0; j < vector.length; j++) {
                vector[j] += dx * row[j];
            }
        }
        return vector;
    }

    /**
     * Compute the sum of Hessian components of given range. Since Hessian is symmetric, only
     * the upper part is computed.
     * @param params  Argument for input position
     * @param pos  Input position
     * @param begin  Begin index of range
     * @param end  End index of range
     * @return  Sum of Hessian components
     */
    protected Matrix hess(Params<T> params, double[] pos, int begin, int end) {
        Matrix mat = Matrices.zeros(pos.length);
        
        for(int k = begin; k < end; k++) {
            double[] coeff = this.consts.getRow(k);
            double d2x = this.convexityAt(params.inter, k, params.args[k]);
            
            this.hess(mat, d2x, coeff);
        }
        
        return mat;
    }
    
    protected Matrix hess(Matrix upper, double d2x, double[] coeff) {
        for(int i = 0; i < upper.getRowCount(); i++) {
            double[] row = upper.getRow(i);
            for(int j = i; j < row.length; j++) {
                
                row[j] += d2x * coeff[i] * coeff[j];
            }
            upper.setRow(i, row);
        }
        return upper;
    }
    
    /**
     * Merge two vectors to the first vector as sum
     * @param u  First vector
     * @param v  Second vector
     * @return  First vector with sum as values
     */
    protected double[] merge(double[] u, double[] v) {
        for(int i = 0; i < u.length; i++) {
            u[i] += v[i];
        }
        return u;
    }
    
    /**
     * Merge two matrices to the first matrix as sum of upper part.
     * @param left  First matrix
     * @param right  Second matrix
     * @return  First matrix with sum of upper part
     */
    protected Matrix merge(Matrix left, Matrix right) {
        for(int i = 0; i < left.getRowCount(); i++) {            
            double[] leftRow = left.getRow(i);
            double[] rightRow = right.getRow(i);
            
            for(int j = i; j < leftRow.length; j++) {
                leftRow[j] += rightRow[j];
            }
            left.setRow(i, leftRow);
        }
        return left;
    }
    
    /**
     * Make a symmetric matrix by copying the upper part to the lower part.
     * @param mat  Input matrix
     * @return  Input matrix with lower part copied
     */
    protected Matrix symmetrize(Matrix mat) {
        for(int i = 0; i < mat.getRowCount(); i++) {
            double[] row = mat.getRow(i);
            for(int j = 0; j < i; j++) {
                row[j] = mat.get(j, i);
            }
            mat.setRow(i, row);
        }
        return mat;
    }
    
    /**
     * Prepare function arguments. If the position is exactly equals to the previous 
     * position, a cached is returned.
     * @param pos  Input position
     * @return  Function arguments
     */
    protected Params<T> prepare(double[] pos) {
        Throw.when()
            .isNull(() -> pos, () -> "No function argument")
            .isTrue(
                () -> pos.length != this.consts.getColCount(), 
                () -> "Invalid argument count. Expected " + this.consts.getColCount()
                    + " but given " + pos.length
            ); 
        Params<T> prev = this.params;
        if(prev != null && this.equals(pos, prev.pos)) {
            return prev;
        }
        Matrix argsMat = this.mulT.compute(this.consts, Matrices.wrap(new double[][] {pos}));
        double[] args = argsMat instanceof ColumnVector
            ? ((ColumnVector) argsMat).getVector()
            : IntStream
                .range(0, argsMat.getRowCount())
                .mapToDouble(i -> argsMat.get(i, 0))
                .toArray();
        Params<T> newParam = new Params<>(
            Arrays.copyOf(pos, pos.length), 
            args, 
            this.prepare(pos, args)
        );
        this.params = newParam;
        return newParam;
    }
    
    /**
     * Determine if two vectors are exactly equal to each other
     * @param u  First vector
     * @param v  Second vector
     * @return  True if exactly equals, false otherwise
     */
    protected boolean equals(double[] u, double[] v) {
        if(u.length != v.length) {
            return false;
        }
        for(int i = 0; i < u.length; i++) {
            if(u[i] != v[i]) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Value of the underlying non-linear function in the series of functions
     * @param inter  Intermediate computational result
     * @param index  Function index of the series of functions
     * @param x  Function argument
     * @return  Value of the underlying non-linear function
     */
    protected abstract double valueAt(T inter, int index, double x);
    
    /**
     * Slope of the underlying non-linear function in the series of functions, i.e.
     * the value of first derivative.
     * @param inter  Intermediate computational result
     * @param index  Function index of the series of functions
     * @param x  Function argument
     * @return  Slope of the underlying non-linear function
     */
    protected abstract double slopeAt(T inter, int index, double x);
    
    /**
     * Convexity of the underlying non-linear function in the series of functions, i.e.
     * the value of second derivative.
     * @param inter  Intermediate computational result
     * @param index  Function index of the series of functions
     * @param x  Function argument
     * @return  Convexity of the underlying non-linear function
     */
    protected abstract double convexityAt(T inter, int index, double x);
    
    /**
     * Prepare intermediate computational result given input position and 
     * the linear combinations with coefficients.
     * @param pos  Input position
     * @param args  Linear combinations with coefficients
     * @return  Intermediary computation result
     */
    protected abstract T prepare(double[] pos, double[] args);
    
    protected final Matrix consts;
    protected final MulT mulT;
    protected final int flopThres;
    
    protected volatile Params<T> params;
    
    /**
     * Data object for function arguments.
     * 
     * @author Y.K. Chan
     * @param <T>  Type of intermediate computational result
     */
    protected static class Params<T> {
        
        private double[] pos;
        
        private double[] args;
        
        private T inter;

        /**
         * Constructor.
         * @param pos  Input position
         * @param args  Function arguments
         * @param inter  Intermediate computational result
         */
        public Params(double[] pos, double[] args, T inter) {
            this.pos = pos;
            this.args = args;
            this.inter = inter;
        }
        
    }
}
