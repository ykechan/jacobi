package jacobi.core.solver.nonlin;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.IntFunction;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.core.impl.ColumnVector;
import jacobi.core.op.MulT;
import jacobi.core.util.MapReducer;

/**
 * A non-linear vector function f(X) 
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
 * @author Y.K. Chan
 *
 */
public class SumLinearArgFunc implements VectorFunction {
    
    /**
     * Constructor
     * @param consts  Argument coefficients 
     * @param funcFact  Function factory
     */
    public SumLinearArgFunc(Matrix consts, Function<double[], FunctionSeries> funcFact) {
        this(consts, funcFact, new MulT(), MapReducer.DEFAULT_NUM_FLOP);
    }

    /**
     * Constructor
     * @param consts  Argument coefficients
     * @param funcFact  Function factory
     * @param mulT  Transpose multiplication implementation
     * @param flopThres  Threshold of number of FLOP to use parallelism
     */
    protected SumLinearArgFunc(Matrix consts, 
            Function<double[], FunctionSeries> funcFact, 
            MulT mulT,
            int flopThres) {
        this.consts = consts;
        this.funcFact = funcFact;
        this.mulT = mulT;
        this.flopThres = flopThres;
    }

    @Override
    public double at(double[] pos) {
        Cache cache = this.getEntry(pos);
        double value = 0.0;
        for(int i = 0; i < cache.args.length; i++) {
            value += cache.series.valueAt(i, cache.args[i]);
        }
        return value;
    }

    @Override
    public ColumnVector grad(double[] pos) {        
        Cache entry = this.getEntry(pos);
        if(pos.length * this.consts.getRowCount() > this.flopThres) {
            double[] ans = MapReducer.of(0, this.consts.getRowCount())
                .flop(pos.length)
                .map((begin, end) -> this.grad(entry.series, entry.args, begin, end))
                .reduce((u, v) -> {
                    for(int i = 0; i < u.length; i++) {
                        u[i] += v[i];
                    }
                    return u;
                })
                .get();
            return new ColumnVector(ans);
        }        
        double[] ans = this.grad(entry.series, entry.args, 0, this.consts.getRowCount());
        return new ColumnVector(ans);
    }

    @Override
    public Matrix hess(double[] pos) {
        Cache entry = this.getEntry(pos);
        int totalFlop = (pos.length * pos.length * this.consts.getRowCount()) / 2;
        if(totalFlop > this.flopThres) {
            Matrix mat = MapReducer.of(0, entry.args.length)
                .flop((pos.length * pos.length) / 2)
                .map((begin, end) -> this.hess(entry.series, entry.args, begin, end))
                .reduce((a, b) -> {
                    return a;
                })
                .get();
            return this.symmetrize(mat);
        }
        return this.symmetrize(this.hess(entry.series, entry.args, 0, entry.args.length));
    }
    
    /**
     * Compute sum of the gradient for a range of functions
     * @param series  Function series
     * @param args  Function arguments
     * @param begin  Begin index of range
     * @param end  End index of range
     * @return  Sum of the gradients of the range of functions
     */
    protected double[] grad(FunctionSeries series, double[] args, int begin, int end) {
        double[] vector = new double[this.consts.getColCount()];
        for(int i = begin; i < end; i++) {
            double df = series.slopeAt(i, args[i]);
            double[] coeff = this.consts.getRow(i);
            for(int j = 0; j < vector.length; j++) {
                vector[j] += df * coeff[j];
            }
        }
        return vector;
    }
    
    /**
     * Compute sum of the upper Hessian for a range of functions
     * @param series  Function series
     * @param args  Function arguments
     * @param begin  Begin index of range
     * @param end  End index of range
     * @return  Sum of the upper Hessian matrix of the range of functions
     */
    protected Matrix hess(FunctionSeries series, double[] args, int begin, int end) {
        Matrix mat = Matrices.zeros(this.consts.getColCount());
        for(int k = begin; k < end; k++) {
            double[] coeff = this.consts.getRow(k);
            double conv = series.convexityAt(k, args[k]);
            for(int i = 0; i < mat.getRowCount(); i++) {
                double[] row = mat.getRow(i);
                for(int j = i; j < row.length; j++) {
                    row[j] += conv * coeff[i] * coeff[j];
                }
                mat.setRow(i, row);
            }
        }
        return mat;
    }
    
    /**
     * Turn the matrix into a symmetric matrix by copying the upper part to the lower part.
     * @param mat  Input matrix
     * @return  Symmetrized matrix
     */
    protected Matrix symmetrize(Matrix mat) {
        for(int i = 0; i < mat.getRowCount(); i++){
            double[] row = mat.getRow(i);
            for(int j = 0; j < i; j++) {
                row[j] = mat.get(j, i);
            }
            mat.setRow(i, row);
        }
        return mat;
    }
    
    /**
     * Get cache entry which includes function series and function arguments at a given position.
     * @param pos  Input position
     * @return  Cache entry
     */
    protected Cache getEntry(double[] pos) {
        Cache entry = this.prevEntry;
        if(entry == null || this.deepEquals(entry.pos, pos)) {
            double[] args = this.linearArgs(pos);
            FunctionSeries series = this.funcFact.apply(args);
            Cache currEntry = new Cache(
                Arrays.copyOf(pos, pos.length),
                args,
                series
            );
            this.prevEntry = currEntry;
            return currEntry;
        }
        return entry;
    }
    
    /**
     * Compare if two double arrays are exactly equal.
     * @param u  First array
     * @param v  Second array
     * @return  True if equals, false otherwise
     */
    protected boolean deepEquals(double[] u, double[] v) {
        for(int i = 0; i < u.length; i++) {
            if(u[i] != v[i]) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Compute the function arguments &lt;A, x&gt;
     * @param pos
     * @return
     */
    protected double[] linearArgs(double[] pos) {
        Matrix result = this.mulT.compute(this.consts, Matrices.wrap(new double[][] {pos}));
        if(result instanceof ColumnVector) {
            return ((ColumnVector) result).getVector();
        }
        double[] column = new double[result.getRowCount()];
        for(int i = 0; i < column.length; i++){
            column[i] = result.get(i, 0);
        }
        return column;
    }
    
    protected Matrix consts;    
    protected Function<double[], FunctionSeries> funcFact;
    protected MulT mulT;       
    protected int flopThres;
    protected volatile Cache prevEntry;    
    
    /**
     * Class for cached data including function series and function arguments.
     */
    protected static class Cache {
        
        /**
         * Input position
         */
        public final double[] pos;
        
        /**
         * Function arguments
         */
        public final double[] args;
        
        /**
         * Series of functions
         */
        public final FunctionSeries series;

        /**
         * Constructor.
         * @param pos  Input position
         * @param args  Function arguments
         * @param series  Series of functions
         */
        public Cache(double[] pos, double[] args, FunctionSeries series) {
            this.pos = pos;
            this.args = args;
            this.series = series;
        }
        
    }
}
