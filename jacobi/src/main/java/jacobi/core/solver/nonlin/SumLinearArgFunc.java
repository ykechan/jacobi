package jacobi.core.solver.nonlin;

import java.util.Arrays;
import java.util.function.IntFunction;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.core.impl.ColumnVector;
import jacobi.core.util.MapReducer;
import jacobi.core.util.Real;

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
    
    public SumLinearArgFunc(Matrix coeffs, IntFunction<ScalarFunction> funcs) {
        this.coeffs = coeffs;
        this.funcs = funcs;
    }

    @Override
    public ColumnVector grad(double[] pos) {
        int totalFlop = this.gradRowFlop() * this.coeffs.getRowCount();
        if(totalFlop < MapReducer.DEFAULT_NUM_FLOP) {
            return new ColumnVector(this.grad(pos, 0, this.coeffs.getRowCount()));
        }
        return new ColumnVector(MapReducer.of(0, this.coeffs.getRowCount())
            .flop(this.gradRowFlop())
            .map((begin, end) -> this.grad(pos, begin, end))
            .reduce(this::sum)
            .get());
    }

    @Override
    public Matrix hess(double[] pos) {
        int totalFlop = this.hessRowFlop() * this.coeffs.getRowCount();
        if(totalFlop < MapReducer.DEFAULT_NUM_FLOP) {
            return this.symmetrize(this.hess(pos, 0, this.coeffs.getRowCount()));
        }
        return this.symmetrize(MapReducer.of(0, this.coeffs.getRowCount())
            .flop(this.hessRowFlop())
            .map((begin, end) -> this.hess(pos, begin, end))
            .reduce(this::sum)
            .get());
    }
    
    protected double[] grad(double[] pos, int begin, int end) {
        double[] vector = new double[pos.length];
        for(int k = begin; k < end; k++) {
            double[] coeff = this.coeffs.getRow(k);
            double df = this.funcs.apply(k).slopeAt(this.dot(coeff, pos));
            System.out.println("df = " + df);
            for(int j = 0; j < vector.length; j++) {
                vector[j] += df * coeff[j];
            }
        }
        return vector;
    }
    
    protected Matrix hess(double[] pos, int begin, int end) {
        Matrix mat = Matrices.zeros(pos.length);
        for(int k = begin; k < end; k++) {
            double[] coeff = this.coeffs.getRow(k);
            double d2f = this.funcs.apply(k).convexityAt(this.dot(coeff, pos));
            
            for(int i = 0; i < mat.getRowCount(); i++) {
                double[] row = mat.getRow(i); 
                double left = coeff[i];
                for(int j = i; j < row.length; j++) {
                    row[j] += d2f * left * coeff[j];
                }
                mat.setRow(i, row);
            }
        }
        return mat;
    }
    
    protected Matrix symmetrize(Matrix mat) {
        for(int i = 1; i < mat.getRowCount(); i++) {
            double[] row = mat.getRow(i);
            for(int j = 0; j < i; j++) {
                row[j] = mat.get(j, i);
            }
            mat.setRow(i, row);
        }
        return mat;
    }
    
    protected double dot(double[] u, double[] v) {
        double ans = 0.0;
        for(int i = 0; i < u.length; i++) {
            ans += u[i] * v[i];
        }
        return ans;
    }
    
    protected double[] sum(double[] u, double[] v) {
        for(int i = 0; i < u.length; i++) {
            u[i] += v[i];
        }
        return u;
    }
    
    protected Matrix sum(Matrix a, Matrix b) {
        for(int i = 0; i < a.getRowCount(); i++) {
            double[] aRow = a.getRow(i);
            double[] bRow = b.getRow(i);
            for(int j = i; j < aRow.length; j++) {
                aRow[j] += bRow[j];
            }
            a.setRow(i, aRow);
        }
        return a;
    }
    
    protected int gradRowFlop() {
        return this.coeffs.getColCount();
    }
    
    protected int hessRowFlop() {
        return (this.coeffs.getColCount() * (this.coeffs.getColCount() + 1)) / 2;
    }

    private Matrix coeffs;
    private IntFunction<ScalarFunction> funcs;
}
